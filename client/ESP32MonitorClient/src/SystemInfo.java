import java.util.List;

public class SystemInfo {
    public String hostName;
    public String osName;
    public String osVersion;
    public String osManufacturer;
    public String osConfiguration;
    public String osBuildType;
    public String registeredOwner;
    public String registeredOrganization;
    public String productId;
    public String originalInstallDate;
    public String systemBootTime;
    public String systemManufacturer;
    public String systemModel;
    public String systemType;
    public int processorCount;
    public String[] processorDetails;
    public String biosVersion;
    public String windowsDirectory;
    public String systemDirectory;
    public String bootDevice;
    public String systemLocale;
    public String inputLocale;
    public String timeZone;
    public String totalPhysicalMemory;
    public String availablePhysicalMemory;
    public String virtualMemoryMaxSize;
    public String virtualMemoryAvailable;
    public String virtualMemoryInUse;
    public String pageFileLocation;
    public String domain;
    public String logonServer;
    public List<String> hotfixes; // This can also be a list
    public List<String> networkCard; // Or list if multiple

    // Optionally: add constructor, getters/setters, toString, etc.
}
