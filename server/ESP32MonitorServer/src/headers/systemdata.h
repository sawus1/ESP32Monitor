#include <vector>
#include <string>
#include <map>

namespace systemdata
{
    struct kernel_info{
        std::string version;
        std::string architecture;
    };

    struct cpu_info {
        std::string model_name;
        int cores;
        int cache_size;
        double cpu_mhz;
        std::string vendor;
    };

    struct os_info {
        std::string name;
        std::string version;
        std::string id;
        std::string id_like;
        std::string pretty_name;
        std::string version_id;
    };

    struct system_info {
        kernel_info kernel;
        std::vector<cpu_info> cpu;
        os_info os;

        void serializeSystemInfo(JsonDocument& doc) {
            doc["datatype"] = "system_info";
            // kernel
            JsonObject Jkernel = doc.createNestedObject("kernel");
            Jkernel["version"] = kernel.version.c_str();
            Jkernel["architecture"] = kernel.architecture.c_str();

            // cpu
            JsonArray Jcpus = doc.createNestedArray("processors");
            for (const auto& cpu : this->cpu) {
                JsonObject Jcpu = Jcpus.createNestedObject();
                Jcpu["model_name"] = cpu.model_name.c_str();
                Jcpu["cores"] = cpu.cores;
                Jcpu["cache_size"] = cpu.cache_size;
                Jcpu["cpu_mhz"] = cpu.cpu_mhz;
                Jcpu["vendor"] = cpu.vendor.c_str();
            }

            // os
            JsonObject Jos = doc.createNestedObject("os");
            Jos["name"] = os.name.c_str();
            Jos["version"] = os.version.c_str();
            Jos["id"] = os.id.c_str();
            Jos["id_like"] = os.id_like.c_str();
            Jos["pretty_name"] = os.pretty_name.c_str();
            Jos["version_id"] = os.version_id.c_str();
}
    };
}




