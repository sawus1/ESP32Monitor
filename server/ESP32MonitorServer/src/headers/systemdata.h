#include <Arduino.h>
#include <vector>
#include <map>



struct KernelInfo{
    std::string version;
    std::string architecture;
};

struct CPUInfo {
    std::string model_name;
    int cores;
    int threads_per_core;
    double cpu_mhz;
    std::vector<std::string> flags;
};

struct OSInfo {
    std::string name;
    std::string version;
    std::string id;
    std::string id_like;
    std::string pretty_name;
    std::string version_id;
};

struct SystemInfo {
    KernelInfo kernel;
    CPUInfo cpu_info;
    OSInfo os;
};



