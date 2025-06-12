#pragma once
#include<string>
#include<cJSON.h>
#include<vector>
#include<map>

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

        void serializeProcessInfo(cJSON* root)
        {
            cJSON_AddStringToObject(root, "datatype", "process_info");

            cJSON* JProc = cJSON_CreateObject();
            
            cJSON_AddItemToObject(root, "process", JProc);
            cJSON_AddNumberToObject(JProc, "pid", pid);
            cJSON_AddStringToObject(JProc, "name", name.c_str());
            cJSON_AddStringToObject(JProc, "state", state.c_str());
            cJSON_AddNumberToObject(JProc, "ppid", ppid);
            cJSON_AddNumberToObject(JProc, "threads", threads);
            cJSON_AddNumberToObject(JProc, "vmsize", vmsize);
            cJSON_AddNumberToObject(JProc, "vmrss", vmrss);
            cJSON_AddNumberToObject(JProc, "vmswap", vmswap);
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

        void serializeMonitoringData(cJSON* root) {
            cJSON_AddStringToObject(root, "datatype", "system_load_data");
            // Memory info
            cJSON* JMemory = cJSON_CreateObject();
            cJSON_AddItemToObject(root, "memory", JMemory);

            cJSON_AddNumberToObject(JMemory, "mem_total", memory.MemTotal);
            cJSON_AddNumberToObject(JMemory, "mem_free", memory.MemFree);
            cJSON_AddNumberToObject(JMemory, "buffers", memory.Buffers);
            cJSON_AddNumberToObject(JMemory, "cached", memory.Cached);
            cJSON_AddNumberToObject(JMemory, "swap_total", memory.SwapTotal);
            cJSON_AddNumberToObject(JMemory, "swap_free", memory.SwapFree);

            // CPU stat
            cJSON* JCpu = cJSON_CreateObject();
            cJSON_AddItemToObject(root, "cpu_stat", JCpu);

            cJSON_AddNumberToObject(JCpu, "user", cpu_stat.user);
            cJSON_AddNumberToObject(JCpu, "nice", cpu_stat.nice);
            cJSON_AddNumberToObject(JCpu, "system", cpu_stat.system);
            cJSON_AddNumberToObject(JCpu, "idle", cpu_stat.idle);
            cJSON_AddNumberToObject(JCpu, "iowait", cpu_stat.iowait);
            cJSON_AddNumberToObject(JCpu, "irq", cpu_stat.irq);
            cJSON_AddNumberToObject(JCpu, "softirq", cpu_stat.softirq);
            cJSON_AddNumberToObject(JCpu, "steal", cpu_stat.steal);
            cJSON_AddNumberToObject(JCpu, "guest", cpu_stat.guest);
            cJSON_AddNumberToObject(JCpu, "guest_nice", cpu_stat.guest_nice);

            // Network interfaces array
            cJSON* JNetifs = cJSON_CreateArray();
            cJSON_AddItemToObject(root, "network_interfaces", JNetifs);
            for (const auto& iface : network_interfaces) {
                cJSON* JIface = cJSON_CreateObject();
                cJSON_AddItemToArray(JNetifs, JIface);
                cJSON_AddStringToObject(JIface, "name", iface.name.c_str());
                cJSON_AddNumberToObject(JIface, "receive_bytes", iface.receive_bytes);
                cJSON_AddNumberToObject(JIface, "transmit_bytes", iface.transmit_bytes);
            }

            // Processes (map of pid -> name)
            cJSON* Jprocs = cJSON_CreateObject();
            cJSON_AddItemToObject(root, "processes", Jprocs);
            for (const auto& [pid, name] : processes) {
                cJSON_AddStringToObject(Jprocs, std::to_string(pid).c_str(), name.c_str());
            }

            // Disk usage
            cJSON* Jdisks = cJSON_CreateArray();
            cJSON_AddItemToObject(root, "disk_usage", Jdisks);
            for (const auto& disk : disk_usage) {
                cJSON* Jdisk = cJSON_CreateObject();
                cJSON_AddStringToObject(Jdisk, "filesystem", disk.filesystem.c_str());
                cJSON_AddNumberToObject(Jdisk, "size", disk.size);
                cJSON_AddNumberToObject(Jdisk, "used", disk.used);
                cJSON_AddNumberToObject(Jdisk, "available", disk.available);
                cJSON_AddNumberToObject(Jdisk, "use_percent", disk.use_percent);
                cJSON_AddStringToObject(Jdisk, "mount_point", disk.mount_point.c_str());
                cJSON_AddItemToArray(Jdisks, Jdisk);
            }
        }
    };
}