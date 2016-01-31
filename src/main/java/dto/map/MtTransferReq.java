package dto.map;

import java.io.Serializable;
import java.util.Map;

public class MtTransferReq implements Serializable {
    public final String processId;
    public final TransferEvent event;
    public final Map<Object, Object> entry;
    public final int limit;

    public MtTransferReq(String processId, TransferEvent event, Map<Object, Object> entry, int limit) {
        this.processId = processId;
        this.event = event;
        this.entry = entry;
        this.limit = limit;
    }
}
