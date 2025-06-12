#pragma once
#include "headers/monitoringdata.h"
#include "esp_log.h"
#include <string>
#include <sstream>
#include <vector>
#include <map>

extern std::string execute_command(const std::string& command);
monitoringdata::MemoryInfo get_mem_info();
std::vector<monitoringdata::DiskUsage> get_disk_usage();
monitoringdata::ProcessInfo get_process_info(std::string pid);
bool killProcess(std::string pid);
std::map<int, std::string> get_processes();
std::vector<monitoringdata::NetworkInterface> get_network_info();
monitoringdata::CPUStat get_cpu_stat();
monitoringdata::SystemLoadData get_system_load_data();
