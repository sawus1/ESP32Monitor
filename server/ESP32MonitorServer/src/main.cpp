#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_flash.h"
#include "nvs_flash.h"
#include "sdkconfig.h"
#include "esp_event.h"


#include "modules/wifi/wifi_manager.hpp"
#include "modules/uart/uart_handler.hpp"
#include "modules/tls/tls_server.hpp"

#define LED_GPIO GPIO_NUM_2
#define RX_BUF_SIZE 512

TaskHandle_t monitor_task_handle = NULL;
mbedtls_ssl_context* monitor_ssl = NULL;
SemaphoreHandle_t ssl_mutex = NULL;
EventGroupHandle_t s_wifi_event_group;

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