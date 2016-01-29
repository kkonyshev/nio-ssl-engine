package dto.file;

import java.io.Serializable;

public class FileTransferReq implements Serializable {

    public final FileTransferReqStatus status;
    public final String lPath;
    public final String rPath;
    public final byte[] payload;
    public final Long lCRC;
    public final Long rCRC;

    public FileTransferReq(FileTransferReqStatus status, String lPath, String rPath, byte[] payload, Long lCRC, Long rCRC) {
        this.status = status;
        this.lPath = lPath;
        this.rPath = rPath;
        this.payload = payload;
        this.lCRC = lCRC;
        this.rCRC = rCRC;
    }

    public FileTransferReq(FileTransferReqStatus status, String lPath, String rPath, byte[] payload) {
        this.status = status;
        this.lPath = lPath;
        this.rPath = rPath;
        this.payload = payload;
        this.lCRC = null;
        this.rCRC = null;
    }
}
