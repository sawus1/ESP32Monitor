import SystemInfo.*;
import java.util.List;

public class SystemInfo {
    private String datatype;
    private Kernel kernel;
    private List<Processor> processors;
    private OS os;

    public String getDatatype() { return datatype; }
    public void setDatatype(String datatype) { this.datatype = datatype; }

    public Kernel getKernel() { return kernel; }
    public void setKernel(Kernel kernel) { this.kernel = kernel; }

    public List<Processor> getProcessors() { return processors; }
    public void setProcessors(List<Processor> processors) { this.processors = processors; }

    public OS getOs() { return os; }
    public void setOs(OS os) { this.os = os; }
}
