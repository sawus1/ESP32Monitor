#include <Arduino.h>
#include "headers/systemdata.h"
#include "headers/monitoringdata.h"
#include <bits/stdc++.h>
std::string executeCommand(String command)
{
    Serial.println(command);

    // Wait for response with timeout
    unsigned long timeout = millis() + 3000;  // 3 seconds
    while (!Serial.available() && millis() < timeout) {
        delay(1);
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

monitoringdata::CPUStat getCpuStat()
{
    monitoringdata::CPUStat stats;
    std::string result = executeCommand("cat /proc/stat | grep -E 'cpu '");

    if(result.find("ERROR") == 0)
    {
        stats.guest = 0;
        stats.nice = 0;
        stats.system = 0;
        stats.idle = 0;
        stats.iowait = 0;
        stats.irq = 0;
        stats.softirq = 0;
        stats.steal = 0;
        stats.guest = 0;
        stats.guest_nice = 0;
    }

    unsigned long params[10];
    std::stringstream s(result);
    std::string tmp;
    char del = ' ';
    byte i = 0;
    s >> tmp;  // read and discard "cpu"
    for (int i=0; i<10; i++) {
        s >> params[i];
    }

    stats.user = params[0];
    stats.nice = params[1];
    stats.system = params[2];
    stats.idle = params[3];
    stats.iowait = params[4];
    stats.irq = params[5];
    stats.softirq = params[6];
    stats.steal = params[7];
    stats.guest = params[8];
    stats.guest_nice = params[9];

    return stats;
}

monitoringdata::MemoryInfo getMemInfo()
{
    monitoringdata::MemoryInfo info;
    std::string result = executeCommand("cat /proc/meminfo | grep -E 'MemTotal|SwapFree|Buffers|^Cached|MemFree|SwapTotal'");

    if(result.find("ERROR") == 0 ){
        info.Buffers = 0;
        info.MemFree = 0;
        info.MemTotal = 0;
        info.Cached =0;
        info.SwapTotal = 0;
        info.SwapFree = 0;

        return info;
    }

    std::stringstream stream(result);
    std::string line;
    std::map<std::string, unsigned long> parsed;

    while(std::getline(stream, line)){
        size_t dl = line.find(":");
        if(dl != std::string::npos)
        {
            std::string key = line.substr(0, dl);
            unsigned long value = stoul(line.substr(dl + 1));

            parsed[key] = value;

        }
    }

    info.Buffers = parsed.count("Buffers") ? parsed["Buffers"] : 0;
    info.MemTotal = parsed.count("MemTotal") ? parsed["MemTotal"] : 0;
    info.MemFree = parsed.count("MemFree") ? parsed["MemFree"] : 0;
    info.SwapFree = parsed.count("SwapFree") ? parsed["SwapFree"] : 0;
    info.SwapTotal = parsed.count("SwapTotal") ? parsed["SwapTotal"] : 0;
    info.Cached = parsed.count("Cached") ? parsed["Cached"] : 0;

    return info;
}

std::vector<monitoringdata::DiskUsage> getDiskUsage()
{
    std::string response = executeCommand("df -k --output=source,size,used,avail,pcent,target");
    std::vector<monitoringdata::DiskUsage> usage;

    if (response.find("ERROR") == 0 || response.empty()) {
        usage.push_back(monitoringdata::DiskUsage{
            .filesystem = "unknown",
            .size = 0,
            .used = 0,
            .available = 0,
            .use_percent = 0,
            .mount_point = "unknown"
        });
        return usage;
    }

    std::stringstream stream(response);
    std::string line;

    // Skip the header line
    std::getline(stream, line);

    while (std::getline(stream, line)) {
        if (line.empty()) continue;

        std::istringstream linestream(line);
        std::string filesystem, size_str, used_str, avail_str, use_str, mount;

        linestream >> filesystem >> size_str >> used_str >> avail_str >> use_str >> mount;

        try {
            monitoringdata::DiskUsage dusage;
            dusage.filesystem = filesystem;
            dusage.size = std::stoll(size_str);
            dusage.used = std::stoll(used_str);
            dusage.available = std::stoll(avail_str);

            // Strip '%' from use_str
            use_str.erase(std::remove(use_str.begin(), use_str.end(), '%'), use_str.end());
            dusage.use_percent = std::stoi(use_str);

            dusage.mount_point = mount;

            usage.push_back(dusage);
        } catch (...) {
            // Skip line if any conversion fails
            continue;
        }
    }

    return usage;
}

monitoringdata::ProcessInfo getProcessInfo(String pid){
    monitoringdata::ProcessInfo info;
    std::string result = executeCommand("ls /proc/" + pid + "/status | grep -E 'Name|State|Pid|PPid|Threads|VmSize|VmRSS|VmSwap'");

    if(result.find("ERROR") == 0){
        info.name = "unknown";
        info.pid = 0;
        info.ppid = 0;
        info.state = "unknown";
        info.threads = 0;
        info.vmrss = 0;
        info.vmsize = 0;
        info.vmswap = 0;

        return info;
    }

    std::stringstream stream(result);
    std::string line;
    std::map<std::string, std::string> parsed;

    while(std::getline(stream, line)){
        size_t dl = line.find(':');
        if(dl != std::string::npos){
            std::string key = line.substr(0, dl);
            std::string value = line.substr(dl + 1);

            if(!value.empty() && value.front() == '"') value = value.substr(1);
            if(!value.empty() && value.back() == '"') value.pop_back();

            parsed[key] = value;
        }
    }

    info.name = parsed.count("Name") ? parsed["Name"] : "unknown";
    info.state = parsed.count("State") ? parsed["State"] : "unknown";
    info.pid = parsed.count("Pid") ? stoi(parsed["Pid"]) : 0; 
    info.ppid = parsed.count("Ppid") ? stoi(parsed["Pid"]) : 0;
    info.threads = parsed.count("Threads") ? stoi(parsed["Threads"]) : 0;
    info.vmsize = parsed.count("VmSize") ? stoul(parsed["VmSize"]) : 0;
    info.vmrss = parsed.count("VmRSS") ? stoul(parsed["VmRSS"]) : 0;
    info.vmswap = parsed.count("VmSwap") ? stoul(parsed["VmSwap"]) : 0;

    return info;
}

bool killProcess(String pid)
{
    std::string result = executeCommand("sudo kill " + pid);
    if(result.find("ERROR") == 0)
    {
        return false;
    }
    return true;
}

std::map<int, std::string> getProcesses()
{
    std::map<int, std::string> processes;
    // Use ps to get PID and command name
    std::string result = executeCommand("ps -eo pid,comm --no-headers");
    if (result.empty())
    {
        return processes;
    }

    std::stringstream stream(result);
    std::string line;
    while (std::getline(stream, line))
    {
        if (line.empty())
        {
            continue;
        }

        std::istringstream linestream(line);
        int pid;
        std::string name;
        if (linestream >> pid >> name)
        {
            processes[pid] = name;
        }
    }
    return processes;
}

std::vector<monitoringdata::NetworkInterface> getNetworkInfo()
{
    std::string response = executeCommand("cat /proc/net/dev");
    std::vector<monitoringdata::NetworkInterface> interfaces;

    if (response.find("ERROR") == 0 || response.empty()) {
        interfaces.push_back(monitoringdata::NetworkInterface{
            .name = "unknown",
            .receive_bytes = 0,
            .transmit_bytes = 0
        });
        return interfaces;
    }

    std::stringstream stream(response);
    std::string line;

    // Skip first two header lines
    std::getline(stream, line);
    std::getline(stream, line);

    while (std::getline(stream, line)) {
        if (line.empty()) continue;

        // Format: iface: <stats...>
        auto colon_pos = line.find(':');
        if (colon_pos == std::string::npos) continue;

        std::string iface_name = line.substr(0, colon_pos);
        // Trim spaces
        iface_name.erase(0, iface_name.find_first_not_of(" \t"));
        iface_name.erase(iface_name.find_last_not_of(" \t") + 1);

        std::string stats_str = line.substr(colon_pos + 1);
        std::istringstream statsstream(stats_str);

        unsigned long receive_bytes = 0;
        unsigned long transmit_bytes = 0;

        // Fields after iface:
        // receive_bytes (1), receive_packets (2), ..., transmit_bytes (9), ...
        statsstream >> receive_bytes;
        for (int i = 0; i < 7; ++i) {
            unsigned long skip;
            statsstream >> skip;
        }
        statsstream >> transmit_bytes;

        interfaces.push_back(monitoringdata::NetworkInterface{
            .name = iface_name,
            .receive_bytes = receive_bytes,
            .transmit_bytes = transmit_bytes
        });
    }

    return interfaces;
}

monitoringdata::SystemLoadData getSystemLoadData()
{
    monitoringdata::SystemLoadData data;
    data.memory = getMemInfo();
    data.cpu_stat = getCpuStat();
    data.network_interfaces = getNetworkInfo();
    data.processes = getProcesses();
    data.disk_usage = getDiskUsage();
    return data;
}