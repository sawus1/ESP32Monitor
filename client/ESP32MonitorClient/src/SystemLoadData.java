import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import SystemLoad.*;

public class SystemLoadData {
    private String datatype;
    private Memory memory;

    @SerializedName("cpu_stat")
    private CpuStat cpuStat;

    @SerializedName("network_interfaces")
    private List<NetworkInterface> networkInterfaces;

    private Map<String, String> processes;

    @SerializedName("disk_usage")
    private List<DiskUsage> diskUsage;

    public String getDatatype() { return datatype; }
    public void setDatatype(String datatype) { this.datatype = datatype; }

    public Memory getMemory() { return memory; }
    public void setMemory(Memory memory) { this.memory = memory; }

    public CpuStat getCpuStat() { return cpuStat; }
    public void setCpuStat(CpuStat cpuStat) { this.cpuStat = cpuStat; }

    public List<NetworkInterface> getNetworkInterfaces() { return networkInterfaces; }
    public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) { this.networkInterfaces = networkInterfaces; }

    public Map<String, String> getProcesses() { return processes; }
    public void setProcesses(Map<String, String> processes) { this.processes = processes; }

    public List<DiskUsage> getDiskUsage() { return diskUsage; }
    public void setDiskUsage(List<DiskUsage> diskUsage) { this.diskUsage = diskUsage; }
}
