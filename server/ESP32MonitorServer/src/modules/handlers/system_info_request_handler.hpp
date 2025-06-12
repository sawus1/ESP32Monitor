#pragma once
#include "headers/systemdata.h"
#include <string>
#include <vector>
#include <sstream>

extern std::string execute_command(const std::string& command);
systemdata::kernel_info get_kernel_info();
std::vector<systemdata::cpu_info> get_cpu_info();
systemdata::os_info get_os_info();
systemdata::system_info get_system_info();