import com.google.gson.Gson;

public class SystemInfo {
    public String system_name;
    public String network_hostname;
    public String kernel_version;
    public String kernel_release;
    public String hardware_architecture;
    
    public SystemInfo(String s)
    {
    	Gson gson = new Gson();
    	SystemInfo info = gson.fromJson(s, SystemInfo.class);
    	this.system_name = info.system_name;
    	this.network_hostname = info.network_hostname;
    	this.kernel_version = info.kernel_version;
    	this.kernel_release = info.kernel_release;
    	this.hardware_architecture = info.hardware_architecture;
    }
}
