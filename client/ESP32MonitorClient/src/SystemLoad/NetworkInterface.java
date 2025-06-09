package SystemLoad;

import com.google.gson.annotations.SerializedName;

public class NetworkInterface {
    private String name;

    @SerializedName("receive_bytes")
    private long receiveBytes;

    @SerializedName("transmit_bytes")
    private long transmitBytes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getReceiveBytes() { return receiveBytes; }
    public void setReceiveBytes(long receiveBytes) { this.receiveBytes = receiveBytes; }

    public long getTransmitBytes() { return transmitBytes; }
    public void setTransmitBytes(long transmitBytes) { this.transmitBytes = transmitBytes; }
}
