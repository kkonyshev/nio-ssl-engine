package dto;

import java.nio.ByteBuffer;

public class FileTransferReq {
    public final String fileName;
    public final ByteBuffer buffer;
    public FileTransferReq(String fileName, ByteBuffer buffer) {
        this.fileName = fileName;
        this.buffer = buffer;
    }
}
