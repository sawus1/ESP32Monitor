package SystemInfo;
import java.util.List;

import com.google.gson.Gson;

public class SystemInfo {
    private String datatype;
    private Kernel kernel;
    private List<Processor> processors;
    private OS os;

    public String getDatatype() { return datatype; }

    public Kernel getKernel() { return kernel; }

    public List<Processor> getProcessors() { return processors; }

    public OS getOs() { return os; }
    
    public SystemInfo(String json)
    {
    	Gson gson = new Gson();
    	SystemInfo i = gson.fromJson(json, SystemInfo.class);
    	this.datatype = i.datatype;
    	this.kernel = i.kernel;
    	this.processors = i.processors;
    	this.os = i.os;
    }
    
    public String serializeJson()
    {
    	Gson gson = new Gson();
    	String json = gson.toJson(this);
    	return json;
    }
}
