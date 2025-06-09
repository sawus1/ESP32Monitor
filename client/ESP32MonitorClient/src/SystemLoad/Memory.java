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
    public void setMemTotal(long memTotal) { this.memTotal = memTotal; }

    public long getMemFree() { return memFree; }
    public void setMemFree(long memFree) { this.memFree = memFree; }

    public long getBuffers() { return buffers; }
    public void setBuffers(long buffers) { this.buffers = buffers; }

    public long getCached() { return cached; }
    public void setCached(long cached) { this.cached = cached; }

    public long getSwapTotal() { return swapTotal; }
    public void setSwapTotal(long swapTotal) { this.swapTotal = swapTotal; }

    public long getSwapFree() { return swapFree; }
    public void setSwapFree(long swapFree) { this.swapFree = swapFree; }
}
