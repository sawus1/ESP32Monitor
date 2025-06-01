#include <Arduino.h>
#include "headers/systemdata.h"
#include <bits/stdc++.h>
std::string executeCommand(String command)
{
    Serial.println(command);

    // Wait for response with timeout
    unsigned long timeout = millis() + 3000;  // 3 seconds
    while (!Serial.available() && millis() < timeout) {
        delay(5);
    }

    if (!Serial.available()) {
        return "ERROR: No response from serial";
    }

    // Read response until delimiter
    String result = Serial.readStringUntil('$');
    if (result.length() == 0) {
        return "ERROR: Empty response";
    }

    // Convert to std::string
    return std::string(result.c_str());
}


systemdata::kernel_info getKernelInfo()
{
    std::string version = executeCommand("uname -r");
    std::string architecture = executeCommand("uname -m");

    systemdata::kernel_info info;
    info.version = version.find("ERROR") == 0 ? "unknown" : version;
    info.architecture = architecture.find("ERROR") == 0 ? "unknown" : architecture;
    return info;
}


std::vector<systemdata::cpu_info> getCpuInfo()
{
    std::string response = executeCommand("cat /proc/cpuinfo");
    std::vector<systemdata::cpu_info> cpus;

    if (response.find("ERROR") == 0 || response.empty()) {
        cpus.push_back(systemdata::cpu_info{
            .model_name = "unknown",
            .cores = 0,
            .cache_size = 0,
            .cpu_mhz = 0.0,
            .vendor = "unknown"
        });
        return cpus;
    }

    std::stringstream stream(response);
    std::string line;
    std::map<std::string, std::string> cpu_block;
    while (std::getline(stream, line)) {
        if (line.empty()) {
            // Process one CPU block
            systemdata::cpu_info info;
            info.model_name = cpu_block["model name"];
            info.cpu_mhz = cpu_block.count("cpu MHz") ? std::stod(cpu_block["cpu MHz"]) : 0.0;
            info.cores = cpu_block.count("cpu cores") ? std::stoi(cpu_block["cpu cores"]) : 1;
            info.cache_size = cpu_block.count("cache size") ? std::stoi(cpu_block["cache size"]) : 0;
            info.vendor = cpu_block["vendor_id"];
            cpus.push_back(info);
            cpu_block.clear();
        } else {
            size_t colon = line.find(':');
            if (colon != std::string::npos) {
                std::string key = line.substr(0, colon);
                std::string value = line.substr(colon + 1);
                // trim
                key.erase(0, key.find_first_not_of(" \t"));
                key.erase(key.find_last_not_of(" \t") + 1);
                value.erase(0, value.find_first_not_of(" \t"));
                value.erase(value.find_last_not_of(" \t") + 1);
                cpu_block[key] = value;
            }
        }
    }

    // Handle last block (in case file doesn't end with empty line)
    if (!cpu_block.empty()) {
        systemdata::cpu_info info;
        info.model_name = cpu_block["model name"];
        info.cpu_mhz = cpu_block.count("cpu MHz") ? std::stod(cpu_block["cpu MHz"]) : 0.0;
        info.cores = cpu_block.count("cpu cores") ? std::stoi(cpu_block["cpu cores"]) : 1;
        info.cache_size = cpu_block.count("cache size") ? std::stoi(cpu_block["cache size"]) : 0;
        info.vendor = cpu_block["vendor_id"];
        cpus.push_back(info);
    }

    return cpus;
}



systemdata::os_info getOSInfo()
{
    std::string response = executeCommand("cat /etc/os-release | grep -E '^ID=|^VERSION_ID=|^PRETTY_NAME=|^ID_LIKE=|^NAME=|^VERSION='");
    systemdata::os_info info;

    if (response.find("ERROR") == 0) {
        info.id = "unknown";
        info.pretty_name = "unknown";
        info.id_like = "unknown";
        info.name = "unknown";
        info.version = "unknown";
        info.version_id = "unknown";
        return info;
    }

    std::stringstream stream(response);
    std::string line;
    std::map<std::string, std::string> parsed;

    while (std::getline(stream, line)) {
        size_t eq = line.find('=');
        if (eq != std::string::npos) {
            std::string key = line.substr(0, eq);
            std::string value = line.substr(eq + 1);

            // Remove surrounding quotes if present
            if (!value.empty() && value.front() == '"') value = value.substr(1);
            if (!value.empty() && value.back() == '"') value.pop_back();

            parsed[key] = value;
        }
    }

    info.id = parsed.count("ID") ? parsed["ID"] : "unknown";
    info.pretty_name = parsed.count("PRETTY_NAME") ? parsed["PRETTY_NAME"] : "unknown";
    info.id_like = parsed.count("ID_LIKE") ? parsed["ID_LIKE"] : "unknown";
    info.name = parsed.count("NAME") ? parsed["NAME"] : "unknown";
    info.version = parsed.count("VERSION") ? parsed["VERSION"] : "unknown";
    info.version_id = parsed.count("VERSION_ID") ? parsed["VERSION_ID"] : "unknown";

    return info;
}

systemdata::system_info getSystemInfo()
{
    systemdata::system_info info;
    info.cpu = getCpuInfo();
    info.kernel = getKernelInfo();
    info.os = getOSInfo();
    return info;
}