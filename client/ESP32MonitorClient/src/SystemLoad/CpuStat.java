package SystemLoad;

import com.google.gson.annotations.SerializedName;

public class CpuStat {
    private long user;
    private long nice;
    private long system;
    private long idle;
    private long iowait;
    private long irq;
    private long softirq;
    private long steal;
    private long guest;

    @SerializedName("guest_nice")
    private long guestNice;

    public long getUser() { return user; }
    public long getNice() { return nice; }
    public long getSystem() { return system; }
    public long getIdle() { return idle; }
    public long getIowait() { return iowait; }
    public long getIrq() { return irq; }
    public long getSoftirq() { return softirq; }
    public long getSteal() { return steal; }
    public long getGuest() { return guest; }
    public long getGuestNice() { return guestNice; }
    
    private long getIdleTime() {
        return idle + iowait;
    }

    private long getTotalTime() {
        return user + nice + system + idle + iowait + irq + softirq + steal;
    }

    public static double computeCpuUsage(CpuStat prev, CpuStat curr) {
        long idleDelta = curr.getIdleTime() - prev.getIdleTime();
        long totalDelta = curr.getTotalTime() - prev.getTotalTime();
        if (totalDelta == 0) return 0.0;
        return 100.0 * (totalDelta - idleDelta) / totalDelta;
    }
}
