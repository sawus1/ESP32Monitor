#include <string>
#include <vector>
#include <map>
#include <cstring>
#include <cJSON.h>
#include "esp_log.h"
#include "esp_system.h"
#include "esp_event.h"
#include "esp_wifi.h"
#include "esp_flash.h"
#include "nvs_flash.h"
#include "sdkconfig.h"
#include "driver/gpio.h"
#include "driver/uart.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"

#include "mbedtls/net_sockets.h"
#include "mbedtls/ssl.h"
#include "mbedtls/entropy.h"
#include "mbedtls/ctr_drbg.h"
#include "mbedtls/x509_crt.h"
#include "mbedtls/pk.h"

#include "certs/dev/server_cert.h"
#include "certs/dev/server_key.h"
#include "headers/request_handler.h"

#define LED_GPIO GPIO_NUM_2
#define UART_PORT UART_NUM_0
#define BUF_SIZE 4096
#define RX_BUF_SIZE 512
#define SERVER_PORT "4433"
#define WIFI_CONNECTED_BIT BIT0

static const char *TAG = "wifi_uart";
static const char *TAG_SERVER = "TLS_SERVER";
static EventGroupHandle_t s_wifi_event_group;
static TaskHandle_t monitor_task_handle = NULL;
static mbedtls_ssl_context* monitor_ssl = NULL;
static SemaphoreHandle_t ssl_mutex = NULL;

extern "C" void app_main(void);

// Forward declarations
static void wifi_event_handler(void*, esp_event_base_t, int32_t, void*);
static esp_err_t wifi_init_sta(const char*, const char*);
static void uart_init(void);
static int uart_readline(char*, int);
static void tls_server_task(void*);
static const char* message_handler(const std::string& msg, mbedtls_ssl_context* ssl);
int ssl_write_all(mbedtls_ssl_context *ssl, const unsigned char *buf, size_t len);

// Main Application
extern "C" void app_main(void)
{
    ESP_ERROR_CHECK(nvs_flash_init());
    uart_init();
    gpio_set_direction(LED_GPIO, GPIO_MODE_OUTPUT);
    ssl_mutex = xSemaphoreCreateMutex();
    if (ssl_mutex == NULL) {
        ESP_LOGE(TAG_SERVER, "Failed to create mutex");
        vTaskDelete(NULL);
    }
    char line[256];
    while (true) {
        int len = uart_readline(line, sizeof(line));
        if (len > 0) {
            ESP_LOGI(TAG, "Received line: %s", line);
            char *ssid = strtok(line, ",");
            char *password = strtok(NULL, ",");

            if (wifi_init_sta(ssid, password) == ESP_OK) {
                ESP_LOGI(TAG, "Connected to Wi-Fi");
                break;
            } else {
                ESP_LOGI(TAG, "Wi-Fi connection failed");
            }
        }
        vTaskDelay(pdMS_TO_TICKS(10));
    }

    gpio_set_level(LED_GPIO, 1);
    xTaskCreate(&tls_server_task, "tls_server_task", 8192, NULL, 5, NULL);
}

static void monitor_task(void* arg)
{
    mbedtls_ssl_context* ssl = (mbedtls_ssl_context*)arg;

    while (1) {
        monitoringdata::SystemLoadData data = getSystemLoadData();
        cJSON *root = cJSON_CreateObject();
        data.serializeMonitoringData(root);
        char *json_str = cJSON_PrintUnformatted(root);
        cJSON_Delete(root);
        if (xSemaphoreTake(ssl_mutex, portMAX_DELAY) == pdTRUE) {
            int ret = ssl_write_all(ssl, (const unsigned char*)json_str, strlen(json_str));
            xSemaphoreGive(ssl_mutex);

            if (ret <= 0) {
                ESP_LOGW(TAG_SERVER, "Failed to send monitoring data or connection closed");
                free(json_str);
                break;
            }
        }
        free(json_str);
        vTaskDelay(pdMS_TO_TICKS(5000));
    }

    monitor_task_handle = NULL;
    monitor_ssl = NULL;
    vTaskDelete(NULL);
}

// UART Setup
static void uart_init(void)
{
    const uart_config_t uart_config = {
        .baud_rate = 115200,
        .data_bits = UART_DATA_8_BITS,
        .parity    = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE
    };

    uart_param_config(UART_PORT, &uart_config);
    uart_set_pin(UART_PORT, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE,
                 UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    uart_driver_install(UART_PORT, BUF_SIZE * 2, 0, 0, NULL, 0);
}

static int uart_readline(char* buf, int max_len)
{
    int len = 0;
    while (len < max_len - 1) {
        uint8_t ch;
        int rx_bytes = uart_read_bytes(UART_PORT, &ch, 1, pdMS_TO_TICKS(100));
        if (rx_bytes > 0) {
            if (ch == '\n' || ch == '\r') break;
            buf[len++] = (char)ch;
        } else {
            break; // Timeout or no data
        }
    }
    buf[len] = '\0';
    return len > 0 ? len : 0;
}


// Wi-Fi Setup
static void wifi_event_handler(void* arg, esp_event_base_t event_base,
                               int32_t event_id, void* event_data)
{
    if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_START) {
        esp_wifi_connect();
    } else if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_DISCONNECTED) {
        ESP_LOGI(TAG, "Reconnecting to Wi-Fi...");
        esp_wifi_connect();
    } else if (event_base == IP_EVENT && event_id == IP_EVENT_STA_GOT_IP) {
        ip_event_got_ip_t* event = (ip_event_got_ip_t*)event_data;
        ESP_LOGI(TAG, "Got IP: " IPSTR, IP2STR(&event->ip_info.ip));
        xEventGroupSetBits(s_wifi_event_group, WIFI_CONNECTED_BIT);
    }
}

static esp_err_t wifi_init_sta(const char* ssid, const char* password)
{
    if (!ssid || !password || strlen(ssid) == 0) return ESP_FAIL;

    if (!s_wifi_event_group)
        s_wifi_event_group = xEventGroupCreate();

    esp_netif_init();
    esp_event_loop_create_default();
    esp_netif_create_default_wifi_sta();

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    esp_wifi_init(&cfg);

    esp_event_handler_instance_t instance_any_id;
    esp_event_handler_instance_t instance_got_ip;
    esp_event_handler_instance_register(WIFI_EVENT, ESP_EVENT_ANY_ID, &wifi_event_handler, NULL, &instance_any_id);
    esp_event_handler_instance_register(IP_EVENT, IP_EVENT_STA_GOT_IP, &wifi_event_handler, NULL, &instance_got_ip);

    wifi_config_t wifi_config = {};
    strncpy((char*)wifi_config.sta.ssid, ssid, sizeof(wifi_config.sta.ssid) - 1);
    strncpy((char*)wifi_config.sta.password, password, sizeof(wifi_config.sta.password) - 1);

    esp_wifi_set_mode(WIFI_MODE_STA);
    esp_wifi_set_config(WIFI_IF_STA, &wifi_config);
    esp_wifi_start();

    EventBits_t bits = xEventGroupWaitBits(s_wifi_event_group, WIFI_CONNECTED_BIT, pdFALSE, pdTRUE, pdMS_TO_TICKS(10000));
    return (bits & WIFI_CONNECTED_BIT) ? ESP_OK : ESP_FAIL;
}


// TLS Server Task
static void tls_server_task(void* pvParameters)
{
    mbedtls_net_context listen_fd, client_fd;
    mbedtls_ssl_context ssl;
    mbedtls_ssl_config conf;
    mbedtls_x509_crt cert;
    mbedtls_pk_context pkey;
    mbedtls_entropy_context entropy;
    mbedtls_ctr_drbg_context ctr_drbg;

    const char *pers = "tls_server";

    mbedtls_net_init(&listen_fd);
    mbedtls_net_init(&client_fd);
    mbedtls_ssl_init(&ssl);
    mbedtls_ssl_config_init(&conf);
    mbedtls_x509_crt_init(&cert);
    mbedtls_pk_init(&pkey);
    mbedtls_entropy_init(&entropy);
    mbedtls_ctr_drbg_init(&ctr_drbg);

    mbedtls_ctr_drbg_seed(&ctr_drbg, mbedtls_entropy_func, &entropy,
                          (const unsigned char*)pers, strlen(pers));
    mbedtls_x509_crt_parse(&cert, (const unsigned char*)espmon_crt,
                           strlen((const char*)espmon_crt) + 1);
    mbedtls_pk_parse_key(&pkey, (const unsigned char*)priv_key,
                         strlen((const char*)priv_key) + 1, NULL, 0, NULL, NULL);

    mbedtls_ssl_config_defaults(&conf, MBEDTLS_SSL_IS_SERVER,
                                MBEDTLS_SSL_TRANSPORT_STREAM,
                                MBEDTLS_SSL_PRESET_DEFAULT);

    mbedtls_ssl_conf_rng(&conf, mbedtls_ctr_drbg_random, &ctr_drbg);
    mbedtls_ssl_conf_ca_chain(&conf, cert.next, NULL);
    mbedtls_ssl_conf_own_cert(&conf, &cert, &pkey);

    mbedtls_net_bind(&listen_fd, NULL, SERVER_PORT, MBEDTLS_NET_PROTO_TCP);
    ESP_LOGI(TAG_SERVER, "Listening on port %s", SERVER_PORT);

    while (true) {
        mbedtls_net_accept(&listen_fd, &client_fd, NULL, 0, NULL);
        mbedtls_ssl_setup(&ssl, &conf);
        mbedtls_ssl_set_bio(&ssl, &client_fd, mbedtls_net_send, mbedtls_net_recv, NULL);

        if (mbedtls_ssl_handshake(&ssl) == 0) {
            ESP_LOGI(TAG_SERVER, "Handshake successful");
            unsigned char buf[512];

            while (true) {
                int len = mbedtls_ssl_read(&ssl, buf, sizeof(buf) - 1);
                if (len <= 0) break;

                buf[len] = 0;
                std::string msg((char*)buf, len);
                ESP_LOGI(TAG_SERVER, "Received: %s", buf);

                if (msg.find("/disconnect") != std::string::npos) break;

                const char *response = message_handler(msg, &ssl);
                if (response) {
                    if (xSemaphoreTake(ssl_mutex, portMAX_DELAY) == pdTRUE) {
                        mbedtls_ssl_write(&ssl, (const unsigned char*)response, strlen(response));
                        xSemaphoreGive(ssl_mutex);
                    }
                    free((void*)response);
                }
            }
        } else {
            ESP_LOGW(TAG_SERVER, "TLS Handshake failed");
        }

        mbedtls_ssl_session_reset(&ssl);
        mbedtls_net_free(&client_fd);
    }

    mbedtls_net_free(&listen_fd);
    mbedtls_ssl_free(&ssl);
    mbedtls_ssl_config_free(&conf);
    mbedtls_ctr_drbg_free(&ctr_drbg);
    mbedtls_entropy_free(&entropy);
    mbedtls_x509_crt_free(&cert);
    mbedtls_pk_free(&pkey);

    vTaskDelete(NULL);
}

// Request handler
static const char* message_handler(const std::string& msg, mbedtls_ssl_context* ssl)
{
    if (msg.find("/get-sys-info") != std::string::npos) {
        systemdata::system_info info = getSystemInfo();
        cJSON *root = cJSON_CreateObject();
        info.serializeSystemInfo(root);
        char *json_str = cJSON_PrintUnformatted(root);
        cJSON_Delete(root);
        return json_str;
    }
    if(msg.find("/get-proc-info") != std::string::npos) {
        std::string pid = msg.substr(msg.find("/get-proc-info") + 15, msg.length()-1);
        monitoringdata::ProcessInfo info = getProcessInfo(pid);
        cJSON *root = cJSON_CreateObject();
        info.serializeProcessInfo(root);
        char *json_str = cJSON_PrintUnformatted(root);
        cJSON_Delete(root);
        return json_str;
    }
    if (msg.find("/start-monitor") != std::string::npos) {
        if (monitor_task_handle == NULL) {
            monitor_ssl = ssl;
            xTaskCreate(&monitor_task, "monitor_task", 16384, (void*)ssl, 5, &monitor_task_handle);
            return strdup("Monitoring started");
        } else {
            return strdup("Monitoring already running");
        }
    }

    if (msg.find("/stop-monitor") != std::string::npos) {
        if (monitor_task_handle != NULL) {
            vTaskDelete(monitor_task_handle);
            monitor_task_handle = NULL;
            monitor_ssl = NULL;
            return strdup("Monitoring stopped");
        } else {
            return strdup("No monitoring task running");
        }
    }
    if(msg.find("/kill-proc") != std::string::npos) {
        std::string pid = msg.substr(msg.find("/kill-proc") + 11);
        executeCommand("sudo kill " + pid);
        return "killed";
    }
    if(msg.find("/reboot-system") != std::string::npos) {
        executeCommand("sudo system reboot");
    }

    return strdup(" ");
}

int ssl_write_all(mbedtls_ssl_context *ssl, const unsigned char *buf, size_t len) {
    size_t written = 0;
    while (written < len) {
        int ret = mbedtls_ssl_write(ssl, buf + written, len - written);
        if (ret > 0) {
            written += ret;
        } else if (ret == MBEDTLS_ERR_SSL_WANT_READ || ret == MBEDTLS_ERR_SSL_WANT_WRITE) {
            vTaskDelay(pdMS_TO_TICKS(10)); // Yield a bit
            continue;
        } else {
            ESP_LOGE(TAG_SERVER, "ssl_write failed: -0x%x", -ret);
            return ret; // error
        }
    }
    return written;
}

