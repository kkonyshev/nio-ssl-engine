package dto;

import java.io.Serializable;

public class RequestObject implements Serializable {
    public final int d1;
    public final int d2;
    public final byte[] data;
    public RequestObject(int d1, int d2, byte[] data) {
        this.d1 = d1;
        this.d2 = d2;
        this.data = data;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("d1=").append(d1).append("; d2=").append(d2).toString();
    }
}
