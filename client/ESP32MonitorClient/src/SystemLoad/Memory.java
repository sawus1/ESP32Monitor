package SystemLoad;

import com.google.gson.annotations.SerializedName;

public class Memory {
    @SerializedName("mem_total")
    private long memTotal;

    @SerializedName("mem_free")
    private long memFree;

    private long buffers;
    private long cached;

    @SerializedName("swap_total")
    private long swapTotal;

    @SerializedName("swap_free")
    private long swapFree;

    public long getMemTotal() { return memTotal; }
    public long getMemFree() { return memFree; }
    public long getBuffers() { return buffers; }
    public long getCached() { return cached; }
    public long getSwapTotal() { return swapTotal; }
    public long getSwapFree() { return swapFree; }
    
    public double getUsedMemoryPercent() {
        long used = memTotal - memFree - buffers - cached;
        return memTotal > 0 ? 100.0 * used / memTotal : 0;
    }

    public double getUsedSwapPercent() {
        long usedSwap = swapTotal - swapFree;
        return swapTotal > 0 ? 100.0 * usedSwap / swapTotal : 0;
    }
}
