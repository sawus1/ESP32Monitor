#pragma once
#include "driver/gpio.h"
#include "driver/uart.h"

#define VBUS_GPIO GPIO_NUM_4
#define UART_PORT UART_NUM_0
#define BUF_SIZE 4096

extern bool uart_connected;
extern TickType_t last_uart_activity;

void uart_init(void);
void init_vbus_gpio(void);
int uart_readline(char* buf, int max_len);
bool uart_check_connection();