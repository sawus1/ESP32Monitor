#include "client_message_handler.hpp"

std::string execute_command(const std::string& command){
    uint32_t timeout_ms = 3000;
    std::string response;
    TickType_t start_tick = xTaskGetTickCount();
    TickType_t timeout_ticks = pdMS_TO_TICKS(timeout_ms);

    if (!conn_timeout && xSemaphoreTake(serial_mutex, portMAX_DELAY)) {
        ESP_LOGI(COMM_TAG, "%s", (command + '\n').c_str());
        char line[512];
        while (true) {
            if ((xTaskGetTickCount() - start_tick) > timeout_ticks) {
                cJSON *root = cJSON_CreateObject();
                cJSON_AddStringToObject(root, "datatype", "device_message");
                cJSON_AddStringToObject(root, "message", "System not responding");
                char *json_str = cJSON_PrintUnformatted(root);
                cJSON_Delete(root);

                if (xSemaphoreTake(ssl_mutex, pdMS_TO_TICKS(1000)) == pdTRUE) {
                    ssl_write_all(monitor_ssl, (const unsigned char*)json_str, strlen(json_str));
                    xSemaphoreGive(ssl_mutex);
                }
                free(json_str);
                break;
            }

            int len = uart_readline(line, sizeof(line));    
            if (len > 0) {
                response += line;
                response += '\n';
                if (response.find('$') != std::string::npos) {
                    response.erase(response.find('$'));
                    break;
                }
            }

            vTaskDelay(pdMS_TO_TICKS(1)); 
        }
        xSemaphoreGive(serial_mutex);
    }

    return response.empty() ? "ERROR" : response;
}


char* message_handler(const std::string& msg, mbedtls_ssl_context* ssl)
{
    if (msg.find("/get-sys-info") != std::string::npos) {
        systemdata::system_info info = get_system_info();
        cJSON *root = cJSON_CreateObject();
        info.serializeSystemInfo(root);
        char *json_str = cJSON_PrintUnformatted(root);
        cJSON_Delete(root);
        return json_str;
    }
    if (msg.find("/get-proc-info") != std::string::npos) {
        std::string pid = msg.substr(msg.find("/get-proc-info") + 15);
        monitoringdata::ProcessInfo info = get_process_info(pid);
        cJSON *root = cJSON_CreateObject();
        info.serializeProcessInfo(root);
        char *json_str = cJSON_PrintUnformatted(root);
        cJSON_Delete(root);
        return json_str;
    }
    if (msg.find("/start-monitor") != std::string::npos) {
        if (monitor_task_handle == NULL) {
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
            return strdup("Monitoring stopped");
        } else {
            return strdup("No monitoring task running");
        }
    }
    if (msg.find("/kill-proc") != std::string::npos) {
        std::string pid = msg.substr(msg.find("/kill-proc") + 11);
        execute_command("sudo kill " + pid);
        return strdup("killed");
    }
    if (msg.find("/reboot-system") != std::string::npos) {
        execute_command("sudo system reboot");
    }
    if(msg.find("/reset-conn") != std::string::npos) {
        usb_disconnected = false;
        conn_timeout = false;
    }
    if(msg.find("/execute") != std::string::npos) {
        std::string path = msg.substr(msg.find("/execute " + 9));
        std::string result = execute_command("sudo sh " + path).find("ERROR") != std::string::npos
                            ? "Script executed successfully"
                            : "Error while running script" + path;
        cJSON *root = cJSON_CreateObject();
        cJSON_AddStringToObject(root, "datatype", "script_execution");
        cJSON_AddStringToObject(root, "result", result.c_str());
        char *json_str = cJSON_PrintUnformatted(root);
        cJSON_Delete(root);

        if (xSemaphoreTake(ssl_mutex, pdMS_TO_TICKS(1000)) == pdTRUE) {
            ssl_write_all(monitor_ssl, (const unsigned char*)json_str, strlen(json_str));
            xSemaphoreGive(ssl_mutex);
        }
        free(json_str);
    }

    return strdup(" ");
}

void monitor_task(void* arg)
{
    mbedtls_ssl_context* ssl = (mbedtls_ssl_context*)arg;

    while (1) {
        monitoringdata::SystemLoadData data = get_system_load_data();
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
    vTaskDelete(NULL);
}
