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

        void serializeSystemInfo(cJSON* root) {
            cJSON_AddStringToObject(root, "datatype", "system_info");

            // Kernel
            cJSON* Jkernel = cJSON_CreateObject();
            cJSON_AddItemToObject(root, "kernel", Jkernel);
            cJSON_AddStringToObject(Jkernel, "version", kernel.version.c_str());
            cJSON_AddStringToObject(Jkernel, "architecture", kernel.architecture.c_str());

            // CPU
            cJSON* Jcpus = cJSON_CreateArray();
            cJSON_AddItemToObject(root, "processors", Jcpus);
            for (const auto& cpu : this->cpu) {
                cJSON* Jcpu = cJSON_CreateObject();
                cJSON_AddStringToObject(Jcpu, "model_name", cpu.model_name.c_str());
                cJSON_AddNumberToObject(Jcpu, "cores", cpu.cores);
                cJSON_AddNumberToObject(Jcpu, "cache_size", cpu.cache_size);
                cJSON_AddNumberToObject(Jcpu, "cpu_mhz", cpu.cpu_mhz);
                cJSON_AddStringToObject(Jcpu, "vendor", cpu.vendor.c_str());
                cJSON_AddItemToArray(Jcpus, Jcpu);
            }

            // OS
            cJSON* Jos = cJSON_CreateObject();
            cJSON_AddItemToObject(root, "os", Jos);
            cJSON_AddStringToObject(Jos, "name", os.name.c_str());
            cJSON_AddStringToObject(Jos, "version", os.version.c_str());
            cJSON_AddStringToObject(Jos, "id", os.id.c_str());
            cJSON_AddStringToObject(Jos, "id_like", os.id_like.c_str());
            cJSON_AddStringToObject(Jos, "pretty_name", os.pretty_name.c_str());
            cJSON_AddStringToObject(Jos, "version_id", os.version_id.c_str());
        }
    };
}




