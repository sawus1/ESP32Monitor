#include <string>
#include <ArduinoJson.h>
#include <vector>
#include <map>

namespace monitoringdata
{

    struct MemoryInfo {
        unsigned long MemTotal;
        unsigned long MemFree;
        unsigned long Buffers;
        unsigned long Cached;
        unsigned long SwapTotal;
        unsigned long SwapFree;
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
        std::string state;
        int ppid;
        int threads;
        unsigned long vmsize;
        unsigned long vmrss;
        unsigned long vmswap;

        void serializeProcessInfo(JsonDocument& doc)
        {
            doc["datatype"] = "process_info";

            JsonObject JProc = doc.createNestedObject("process");
            JProc["pid"] = pid;
            JProc["name"] = name;
            JProc["state"] = state;
            JProc["ppid"] = ppid;
            JProc["threads"] = threads;
            JProc["vmsize"] = vmsize;
            JProc["vmrss"] = vmrss;
            JProc["vmswap"] = vmswap;
        }
    };

    struct DiskUsage {
        std::string filesystem;
        unsigned long size;
        unsigned long used;
        unsigned long available;
        unsigned short use_percent;
        std::string mount_point;
    };

    struct SystemLoadData {
        MemoryInfo memory;
        CPUStat cpu_stat;
        std::vector<NetworkInterface> network_interfaces;
        std::map<int, std::string> processes;
        std::vector<DiskUsage> disk_usage;

        void serializeMonitoringData(JsonDocument& doc) {
            doc["datatype"] = "system_load_data";

            // Memory info
            JsonObject Jmemory = doc.createNestedObject("memory");
            Jmemory["MemTotal"] = memory.MemTotal;
            Jmemory["MemFree"] = memory.MemFree;
            Jmemory["Buffers"] = memory.Buffers;
            Jmemory["Cached"] = memory.Cached;
            Jmemory["SwapTotal"] = memory.SwapTotal;
            Jmemory["SwapFree"] = memory.SwapFree;

            // CPU stat
            JsonObject Jcpu = doc.createNestedObject("cpu_stat");
            Jcpu["user"] = cpu_stat.user;
            Jcpu["nice"] = cpu_stat.nice;
            Jcpu["system"] = cpu_stat.system;
            Jcpu["idle"] = cpu_stat.idle;
            Jcpu["iowait"] = cpu_stat.iowait;
            Jcpu["irq"] = cpu_stat.irq;
            Jcpu["softirq"] = cpu_stat.softirq;
            Jcpu["steal"] = cpu_stat.steal;
            Jcpu["guest"] = cpu_stat.guest;
            Jcpu["guest_nice"] = cpu_stat.guest_nice;

            // Network interfaces array
            JsonArray Jnetifs = doc.createNestedArray("network_interfaces");
            for (const auto& iface : network_interfaces) {
                JsonObject Jiface = Jnetifs.createNestedObject();
                Jiface["name"] = iface.name.c_str();
                Jiface["receive_bytes"] = iface.receive_bytes;
                Jiface["transmit_bytes"] = iface.transmit_bytes;
            }

            // Processes map (pid -> name)
            JsonObject Jprocs = doc.createNestedObject("processes");
            for (const auto& [pid, name] : processes) {
                Jprocs[String(pid).c_str()] = name.c_str();
            }

            // Disk usage array
            JsonArray Jdisks = doc.createNestedArray("disk_usage");
            for (const auto& disk : disk_usage) {
                JsonObject Jdisk = Jdisks.createNestedObject();
                Jdisk["filesystem"] = disk.filesystem.c_str();
                Jdisk["size"] = disk.size;
                Jdisk["used"] = disk.used;
                Jdisk["available"] = disk.available;
                Jdisk["use_percent"] = disk.use_percent;
                Jdisk["mount_point"] = disk.mount_point.c_str();
            }
        }
    };
}