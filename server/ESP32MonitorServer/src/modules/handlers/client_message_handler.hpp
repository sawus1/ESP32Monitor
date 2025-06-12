#pragma once
#include <string>
#include <cstring>
#include"mbedtls/ssl.h"
#include <esp_log.h>
#include <cJSON.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "headers/monitoringdata.h"
#include "headers/systemdata.h"
#include "system_data_request_handler.hpp"
#include "system_info_request_handler.hpp"
#include <freertos/semphr.h>

const char* COMM_TAG = "COMMAND";
#define TAG_SERVER "TLS_SERVER"
extern int uart_readline(char*, int);

extern TaskHandle_t monitor_task_handle;
extern mbedtls_ssl_context* monitor_ssl;
extern SemaphoreHandle_t ssl_mutex;

extern int ssl_write_all(mbedtls_ssl_context *ssl, const unsigned char *buf, size_t len);
void monitor_task(void* arg);
char* message_handler(const std::string& msg, mbedtls_ssl_context* ssl);
std::string executeCommand(const std::string& command);
