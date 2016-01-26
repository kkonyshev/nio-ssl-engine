package dto;

import java.net.SocketAddress;

public class FileTransferRes {
    public final SocketAddress serverAddress;
    public final int bytesCount;
    public FileTransferRes(SocketAddress serverAddress, int bytesCount) {
        this.serverAddress = serverAddress;
        this.bytesCount = bytesCount;
    }
}
