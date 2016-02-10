package server.map;

import dto.map.MtTransferReq;
import dto.map.MtTransferRes;
import dto.map.ResultStatus;

public class SimpleExampleClientDataProcessor extends ClientMtMapRequestAdapter {
    @Override
    public MtTransferRes process(MtTransferReq mtTransferRes) {
        System.out.println("RCV: " + mtTransferRes.entry);
        return new MtTransferRes(mtTransferRes.processId, ResultStatus.OK, 0);
    }
}
