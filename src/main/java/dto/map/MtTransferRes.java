package dto.map;

import java.io.Serializable;

public class MtTransferRes implements Serializable {

    public final String processId;
    public final ResultStatus status;
    public final int size;

    public MtTransferRes(String processId, ResultStatus status, int size) {
        this.processId = processId;
        this.status = status;
        this.size = size;
    }
}
