package dto.object;

import java.io.Serializable;
import java.util.zip.CRC32;

public class ResponseObject implements Serializable {
    public final RequestObject request;
    public final int sum;
    public final long checksum;
    public ResponseObject(RequestObject request) {
        this.request = request;
        this.sum = request.d1 + request.d2;
        CRC32 crc = new CRC32();
        crc.update(request.data);
        this.checksum = crc.getValue();
    }
}
