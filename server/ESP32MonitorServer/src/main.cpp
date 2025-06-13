#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_flash.h"
#include "nvs_flash.h"
#include "sdkconfig.h"
#include "esp_event.h"
#include <cJSON.h>


#include "modules/wifi/wifi_manager.hpp"
#include "modules/uart/uart_handler.hpp"
#include "modules/tls/tls_server.hpp"

#define LED_GPIO GPIO_NUM_2
#define RX_BUF_SIZE 512

TaskHandle_t monitor_task_handle = NULL;
mbedtls_ssl_context* monitor_ssl = NULL;
SemaphoreHandle_t ssl_mutex = NULL;
SemaphoreHandle_t serial_mutex = NULL;
EventGroupHandle_t s_wifi_event_group;
bool conn_timeout = false;
bool usb_disconnected = false;
bool system_timeout = false;

void device_state_task(void* arg);

// Main Application
extern "C" void app_main(void)
{
    ESP_ERROR_CHECK(nvs_flash_init());
    uart_init();
    gpio_set_direction(LED_GPIO, GPIO_MODE_OUTPUT);
    ssl_mutex = xSemaphoreCreateMutex();
    serial_mutex = xSemaphoreCreateMutex();
    if (ssl_mutex == NULL) {
        ESP_LOGE(TAG_SERVER, "Failed to create mutex");
        vTaskDelete(NULL);
    }
    xTaskCreate(&device_state_task, "device_state_task", 8192, NULL, 5, NULL);
    while (true) {
        if(check_connection()) break;
        vTaskDelay(pdMS_TO_TICKS(10));
    }

    gpio_set_level(LED_GPIO, 1);
    xTaskCreate(&tls_server_task, "tls_server_task", 8192, NULL, 1, NULL);
}

void device_state_task(void* arg)
{
    char line[256];
    while (true) {
        int len = 0;
        if(xSemaphoreTake(serial_mutex, portMAX_DELAY)){
            len = uart_readline(line, sizeof(line));
            xSemaphoreGive(serial_mutex);
        }
        if (len > 0) {
            if (strcmp(line, "check_conn") == 0) {
                const char *response = check_connection()
                                       ? "WiFiConnected\n"
                                       : "WiFiDisconnected\n";
                uart_write_bytes(UART_PORT, response, strlen(response));
            }
            if((std::string(line).find(",") != std::string::npos) && !check_connection())
            {
                ESP_LOGI(TAG, "Received line: %s", line);
                char *ssid = strtok(line, ",");
                char *password = strtok(NULL, ",");

                if (wifi_init_sta(ssid, password) == ESP_OK) {
                    ESP_LOGI(TAG, "Connected to Wi-Fi");
                } else {
                    ESP_LOGI(TAG, "Wi-Fi connection failed");
                }
            }
        }
        //if(!uart_check_connection()) ESP_LOGW("POWER", "NO POWER ON GPIO4");
        if (!uart_check_connection() &&(monitor_ssl != NULL && ssl_mutex != NULL) && !usb_disconnected) {
                cJSON *root = cJSON_CreateObject();
                cJSON_AddStringToObject(root, "datatype", "device_message");
                cJSON_AddStringToObject(root, "message", "Device not powered from USB. Is system running?");
                char *json_str = cJSON_PrintUnformatted(root);
                cJSON_Delete(root);

                if (xSemaphoreTake(ssl_mutex, pdMS_TO_TICKS(1000)) == pdTRUE) {
                    ssl_write_all(monitor_ssl, (const unsigned char*)json_str, strlen(json_str));
                    xSemaphoreGive(ssl_mutex);
                }
                free(json_str);
                usb_disconnected = true;
        }
        vTaskDelay(pdMS_TO_TICKS(500));
    }
}
