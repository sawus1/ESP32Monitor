#include <string>
#include <vector>
#include <map>

struct MemoryInfo {
    std::string MemTotal;
    std::string MemFree;
    std::string Buffers;
    std::string Cached;
};

struct CPUStat {
    unsigned long user;
    unsigned long nice;
    unsigned long system;
    unsigned long idle;
    unsigned long iowait;
    unsigned long irq;
    unsigned long softirq;
    unsigned long steal;
    unsigned long guest;
    unsigned long guest_nice;
};

struct NetworkInterface {
    std::string name;
    unsigned long receive_bytes;
    unsigned long transmit_bytes;
};

struct ProcessInfo {
    int pid;
    std::string name;
};

struct DiskUsage {
    std::string filesystem;
    std::string size;
    std::string used;
    std::string available;
    std::string use_percent;
    std::string mount_point;
};

struct SystemLoadData {
    MemoryInfo memory;
    CPUStat cpu_stat;
    std::vector<NetworkInterface> network_interfaces;
    std::vector<ProcessInfo> processes;
    std::vector<DiskUsage> disk_usage;
};


