package SystemLoad;

import com.google.gson.annotations.SerializedName;

public class NetworkInterface {
    private String name;

    @SerializedName("receive_bytes")
    private long receiveBytes;

    @SerializedName("transmit_bytes")
    private long transmitBytes;

    public String getName() { return name; }
    public long getReceiveBytes() { return receiveBytes; }
    public long getTransmitBytes() { return transmitBytes; }
    
}
