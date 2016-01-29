package dto.map;

import java.io.Serializable;
import java.util.Map;

public class MtTransferReq implements Serializable {
    public final String processId;
    public final TransferEvent event;
    public final Map<Object, Object> entry;

    public MtTransferReq(String processId, TransferEvent event, Map<Object, Object> entry) {
        this.processId = processId;
        this.event = event;
        this.entry = entry;
    }
}
