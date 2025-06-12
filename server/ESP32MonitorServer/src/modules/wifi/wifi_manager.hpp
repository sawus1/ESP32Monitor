#pragma once
#include "esp_event.h"
#include "esp_wifi.h"
#include "cstring"
#include "esp_log.h"

#define WIFI_CONNECTED_BIT BIT0
#define TAG "WiFi"

extern EventGroupHandle_t s_wifi_event_group;

bool check_connection();
esp_err_t wifi_init_sta(const char*, const char*);
void wifi_event_handler(void* arg, esp_event_base_t event_base,
                               int32_t event_id, void* event_data);