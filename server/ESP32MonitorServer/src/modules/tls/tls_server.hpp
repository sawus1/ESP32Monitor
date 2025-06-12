#pragma once
#include "mbedtls/net_sockets.h"
#include "mbedtls/ssl.h"
#include "mbedtls/entropy.h"
#include "mbedtls/ctr_drbg.h"
#include "mbedtls/x509_crt.h"
#include "mbedtls/pk.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "string"
#include "esp_log.h"
#include "cstring"
#include "certs/dev/server_cert.h"
#include "certs/dev/server_key.h"
#include "esp_system.h"

#define SERVER_PORT "4433"
#define TAG_SERVER "TLS_SERVER"

extern SemaphoreHandle_t ssl_mutex;
extern mbedtls_ssl_context* monitor_ssl;
extern char *message_handler(const std::string &msg, mbedtls_ssl_context *ssl);
int ssl_write_all(mbedtls_ssl_context *ssl, const unsigned char *buf, size_t len);

bool setup_tls_config(mbedtls_ssl_config* conf,
                      mbedtls_ctr_drbg_context* ctr_drbg,
                      mbedtls_x509_crt* cert,
                      mbedtls_pk_context* pkey);
bool setup_listening_socket(mbedtls_net_context* listen_fd, const char* port);
void handle_tls_client(mbedtls_net_context* listen_fd,
                       mbedtls_ssl_context* ssl,
                       mbedtls_ssl_config* conf);
void client_communication_loop(mbedtls_ssl_context* ssl);
void cleanup_tls_context();
void tls_server_task(void* pvParameters);