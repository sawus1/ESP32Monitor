package SystemInfo;

public class Processor {
    private String model_name;
    private int cores;
    private int cache_size;
    private double cpu_mhz;
    private String vendor;

    public String getModel_name() { return model_name; }
    public void setModel_name(String model_name) { this.model_name = model_name; }

    public int getCores() { return cores; }
    public void setCores(int cores) { this.cores = cores; }

    public int getCache_size() { return cache_size; }
    public void setCache_size(int cache_size) { this.cache_size = cache_size; }

    public double getCpu_mhz() { return cpu_mhz; }
    public void setCpu_mhz(double cpu_mhz) { this.cpu_mhz = cpu_mhz; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
}

