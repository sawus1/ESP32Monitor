import com.google.gson.Gson;
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
    public Memory getMemory() { return memory; }
    public CpuStat getCpuStat() { return cpuStat; }
    public List<NetworkInterface> getNetworkInterfaces() { return networkInterfaces; }
    public Map<String, String> getProcesses() { return processes; }
    public List<DiskUsage> getDiskUsage() { return diskUsage; }
    
    public SystemLoadData(String json)
    {
    	Gson gson = new Gson();
    	SystemLoadData dt = gson.fromJson(json, SystemLoadData.class);
    	this.datatype = dt.datatype;
    	this.memory = dt.memory;
    	this.cpuStat = dt.cpuStat;
    	this.networkInterfaces = dt.networkInterfaces;
    	this.processes = dt.processes;
    	this.diskUsage = dt.diskUsage;
    }
    
    public String serializeJson()
    {
    	Gson gson = new Gson();
    	String json = gson.toJson(this);
    	return json;
    }
}
