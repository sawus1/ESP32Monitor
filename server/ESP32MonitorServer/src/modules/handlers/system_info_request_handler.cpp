#include "system_info_request_handler.hpp"

systemdata::kernel_info get_kernel_info()
{
    std::string version = execute_command("uname -r");
    std::string architecture = execute_command("uname -m");

    systemdata::kernel_info info;
    info.version = version.find("ERROR") == 0 ? "unknown" : version;
    info.architecture = architecture.find("ERROR") == 0 ? "unknown" : architecture;
    return info;
}


std::vector<systemdata::cpu_info> get_cpu_info()
{
    std::string response = execute_command("cat /proc/cpuinfo");
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
        size_t colon = line.find(':');
        if (colon != std::string::npos) {
            std::string key = line.substr(0, colon);
            std::string value = line.substr(colon + 1);
            key.erase(0, key.find_first_not_of(" \t"));
            key.erase(key.find_last_not_of(" \t") + 1);
            value.erase(0, value.find_first_not_of(" \t"));
            value.erase(value.find_last_not_of(" \t") + 1);

            if (key == "processor" && !cpu_block.empty()) {
                // Finish previous block
                systemdata::cpu_info info;
                info.model_name = cpu_block["model name"];
                info.cpu_mhz = cpu_block.count("cpu MHz") ? std::stod(cpu_block["cpu MHz"]) : 0.0;
                info.cores = cpu_block.count("cpu cores") ? std::stoi(cpu_block["cpu cores"]) : 1;
                if (cpu_block.count("cache size")) {
                    std::string cache = cpu_block["cache size"];
                    size_t kb_pos = cache.find(" KB");
                    if (kb_pos != std::string::npos)
                        cache = cache.substr(0, kb_pos);
                    info.cache_size = std::stoi(cache);
                } else {
                    info.cache_size = 0;
                }
                info.vendor = cpu_block["vendor_id"];
                cpus.push_back(info);
                cpu_block.clear();
            }

            cpu_block[key] = value;
        }
    }

    // Handle final CPU block
    if (!cpu_block.empty()) {
        systemdata::cpu_info info;
        info.model_name = cpu_block["model name"];
        info.cpu_mhz = cpu_block.count("cpu MHz") ? std::stod(cpu_block["cpu MHz"]) : 0.0;
        info.cores = cpu_block.count("cpu cores") ? std::stoi(cpu_block["cpu cores"]) : 1;
        if (cpu_block.count("cache size")) {
            std::string cache = cpu_block["cache size"];
            size_t kb_pos = cache.find(" KB");
            if (kb_pos != std::string::npos)
                cache = cache.substr(0, kb_pos);
            info.cache_size = std::stoi(cache);
        } else {
            info.cache_size = 0;
        }
        info.vendor = cpu_block["vendor_id"];
        cpus.push_back(info);
    }

    return cpus;
}




systemdata::os_info get_os_info()
{
    std::string response = execute_command("cat /etc/os-release | grep -E '^ID=|^VERSION_ID=|^PRETTY_NAME=|^ID_LIKE=|^NAME=|^VERSION='");
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

systemdata::system_info get_system_info()
{
    systemdata::system_info info;
    info.cpu = get_cpu_info();
    info.kernel = get_kernel_info();
    info.os = get_os_info();
    return info;
}