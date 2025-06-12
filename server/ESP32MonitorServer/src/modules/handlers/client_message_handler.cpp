#include "client_message_handler.hpp"

std::string execute_command(const std::string& command)
{
    ESP_LOGI(COMM_TAG, "%s", (command + '\n').c_str());
    std::string response;
    char line[512];

    while (true) {
        int len = uart_readline(line, sizeof(line));
        if (len > 0) {
            ESP_LOGI("LINE", "line: %s", line);
            response += line;
            response += '\n';
            if(response.find("$") != std::string::npos) {
                response.erase(response.find("$"));
                break;
            }
        }
        vTaskDelay(pdMS_TO_TICKS(10));
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
