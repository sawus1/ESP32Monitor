#include "tls_server.hpp"

bool init_tls_context(mbedtls_net_context *listen_fd, mbedtls_ssl_context *ssl,
                      mbedtls_ssl_config *conf, mbedtls_x509_crt *cert,
                      mbedtls_pk_context *pkey, mbedtls_entropy_context *entropy,
                      mbedtls_ctr_drbg_context *ctr_drbg) {
    const char *pers = "tls_server";

    mbedtls_net_init(listen_fd);
    mbedtls_ssl_init(ssl);
    mbedtls_ssl_config_init(conf);
    mbedtls_x509_crt_init(cert);
    mbedtls_pk_init(pkey);
    mbedtls_entropy_init(entropy);
    mbedtls_ctr_drbg_init(ctr_drbg);

    if (mbedtls_ctr_drbg_seed(ctr_drbg, mbedtls_entropy_func, entropy,
                               (const unsigned char *)pers, strlen(pers)) != 0) {
        return false;
    }

    if (mbedtls_x509_crt_parse(cert, (const unsigned char *)espmon_crt,
                                strlen((const char*)espmon_crt) + 1) != 0) {
        return false;
    }

    if (mbedtls_pk_parse_key(pkey, (const unsigned char *)priv_key,
                             strlen((const char*)priv_key) + 1, NULL, 0, NULL, NULL) != 0) {
        return false;
    }

    return true;
}

bool setup_tls_config(mbedtls_ssl_config *conf, mbedtls_ctr_drbg_context *ctr_drbg,
                      mbedtls_x509_crt *cert, mbedtls_pk_context *pkey) {
    if (mbedtls_ssl_config_defaults(conf, MBEDTLS_SSL_IS_SERVER,
                                    MBEDTLS_SSL_TRANSPORT_STREAM,
                                    MBEDTLS_SSL_PRESET_DEFAULT) != 0) {
        return false;
    }

    mbedtls_ssl_conf_rng(conf, mbedtls_ctr_drbg_random, ctr_drbg);
    mbedtls_ssl_conf_ca_chain(conf, cert->next, NULL);
    if (mbedtls_ssl_conf_own_cert(conf, cert, pkey) != 0) {
        return false;
    }

    return true;
}

bool setup_listening_socket(mbedtls_net_context *listen_fd, const char *port) {
    if (mbedtls_net_bind(listen_fd, NULL, port, MBEDTLS_NET_PROTO_TCP) != 0) {
        return false;
    }
    ESP_LOGI(TAG_SERVER, "Listening on port %s", port);
    return true;
}

void client_communication_loop(mbedtls_ssl_context *ssl) {
    unsigned char buf[512];

    while (true) {
        int len = mbedtls_ssl_read(ssl, buf, sizeof(buf) - 1);
        if (len <= 0) break;

        buf[len] = 0;
        std::string msg((char *)buf, len);
        ESP_LOGI(TAG_SERVER, "Received: %s", buf);

        if (msg.find("/disconnect") != std::string::npos) break;

        const char *result = message_handler(msg, ssl);
        if (result) {
            if (xSemaphoreTake(ssl_mutex, portMAX_DELAY) == pdTRUE) {
                ssl_write_all(ssl, (const unsigned char *)result, strlen(result));
                xSemaphoreGive(ssl_mutex);
            }
        }
    }
}

void handle_tls_client(mbedtls_net_context *listen_fd, mbedtls_ssl_context *ssl,
                       mbedtls_ssl_config *conf) {
    mbedtls_net_context client_fd;
    mbedtls_net_init(&client_fd);

    if (mbedtls_net_accept(listen_fd, &client_fd, NULL, 0, NULL) == 0) {
        mbedtls_ssl_setup(ssl, conf);
        mbedtls_ssl_set_bio(ssl, &client_fd, mbedtls_net_send, mbedtls_net_recv, NULL);

        if (mbedtls_ssl_handshake(ssl) == 0) {
            ESP_LOGI(TAG_SERVER, "Handshake successful");
            client_communication_loop(ssl);
        } else {
            ESP_LOGW(TAG_SERVER, "TLS Handshake failed");
        }

        mbedtls_ssl_session_reset(ssl);
        mbedtls_net_free(&client_fd);
    }
}

void cleanup_tls_context(mbedtls_net_context *listen_fd, mbedtls_ssl_context *ssl,
                         mbedtls_ssl_config *conf, mbedtls_entropy_context *entropy,
                         mbedtls_ctr_drbg_context *ctr_drbg, mbedtls_x509_crt *cert,
                         mbedtls_pk_context *pkey) {
    mbedtls_net_free(listen_fd);
    mbedtls_ssl_free(ssl);
    mbedtls_ssl_config_free(conf);
    mbedtls_ctr_drbg_free(ctr_drbg);
    mbedtls_entropy_free(entropy);
    mbedtls_x509_crt_free(cert);
    mbedtls_pk_free(pkey);
}

void tls_server_task(void *pvParameters) {
    mbedtls_net_context listen_fd;
    mbedtls_ssl_context ssl;
    mbedtls_ssl_config conf;
    mbedtls_x509_crt cert;
    mbedtls_pk_context pkey;
    mbedtls_entropy_context entropy;
    mbedtls_ctr_drbg_context ctr_drbg;

    if (!init_tls_context(&listen_fd, &ssl, &conf, &cert, &pkey, &entropy, &ctr_drbg) ||
        !setup_tls_config(&conf, &ctr_drbg, &cert, &pkey) ||
        !setup_listening_socket(&listen_fd, SERVER_PORT)) {
        ESP_LOGE(TAG_SERVER, "TLS server initialization failed");
        vTaskDelete(NULL);
    }

    monitor_ssl = &ssl;
    while (true) {
        handle_tls_client(&listen_fd, &ssl, &conf);
    }

    cleanup_tls_context(&listen_fd, &ssl, &conf, &entropy, &ctr_drbg, &cert, &pkey);
    vTaskDelete(NULL);
}

int ssl_write_all(mbedtls_ssl_context *ssl, const unsigned char *buf, size_t len) {
    size_t written = 0;
    char* msg = new char[len + 2];
    strcpy(msg, (char*)buf);
    msg[len] = '\n';
    msg[len+1] = '\0';

    while (written < len + 2) {
        int ret = mbedtls_ssl_write(ssl, (const unsigned char *)msg + written, len - written + 2);
        if (ret > 0) {
            written += ret;
        } else if (ret == MBEDTLS_ERR_SSL_WANT_READ || ret == MBEDTLS_ERR_SSL_WANT_WRITE) {
            vTaskDelay(pdMS_TO_TICKS(10));
            continue;
        } else {
            ESP_LOGE(TAG_SERVER, "ssl_write failed: -0x%x", -ret);
            delete[] msg;
            return ret;
        }
    }
    delete[] msg;
    return written;
}