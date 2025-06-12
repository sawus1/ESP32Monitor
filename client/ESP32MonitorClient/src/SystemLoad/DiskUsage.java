package SystemLoad;

import com.google.gson.annotations.SerializedName;

public class DiskUsage {
    private String filesystem;
    private long size;
    private long used;
    private long available;

    @SerializedName("use_percent")
    private int usePercent;

    @SerializedName("mount_point")
    private String mountPoint;

    public String getFilesystem() { return filesystem; }
    public long getSize() { return size; }
    public long getUsed() { return used; }
    public long getAvailable() { return available; }
    public int getUsePercent() { return usePercent; }
    public String getMountPoint() { return mountPoint; }
}
