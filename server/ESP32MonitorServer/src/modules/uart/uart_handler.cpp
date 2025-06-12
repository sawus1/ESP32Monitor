#include "uart_handler.hpp"

void uart_init(void)
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

int uart_readline(char* buf, int max_len)
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

void init_vbus_gpio(void)
{
    gpio_config_t io_conf = {
        .pin_bit_mask = 1ULL << VBUS_GPIO,
        .mode = GPIO_MODE_INPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };
    gpio_config(&io_conf);
}

bool uart_check_connection()
{
    int level = gpio_get_level(VBUS_GPIO);
    return level == 1 ? true : false;
}

