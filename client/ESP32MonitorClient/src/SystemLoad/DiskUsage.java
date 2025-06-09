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
    public void setFilesystem(String filesystem) { this.filesystem = filesystem; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public long getUsed() { return used; }
    public void setUsed(long used) { this.used = used; }

    public long getAvailable() { return available; }
    public void setAvailable(long available) { this.available = available; }

    public int getUsePercent() { return usePercent; }
    public void setUsePercent(int usePercent) { this.usePercent = usePercent; }

    public String getMountPoint() { return mountPoint; }
    public void setMountPoint(String mountPoint) { this.mountPoint = mountPoint; }
}
