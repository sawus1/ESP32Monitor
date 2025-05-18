#include <Arduino.h>
namespace systemdata
{
    struct system_info
    {
        String systemName;
        String networkHostname;
        String kernelVersion;
        String kernelRelease;
        String hardwareArchitecture;
    }sysinfo;
    
}