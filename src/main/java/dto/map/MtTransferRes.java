package dto.map;

import java.io.Serializable;

public class MtTransferRes implements Serializable {

    public final String processId;
    public final ResultStatus status;

    public MtTransferRes(String processId, ResultStatus status) {
        this.processId = processId;
        this.status = status;
    }
}
