package server.map;

import dto.map.MtTransferReq;
import dto.map.MtTransferRes;
import dto.map.ResultStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientMtMapRequestAdapter extends ChannelInboundHandlerAdapter {

    private Logger LOG = LogManager.getLogger();

    private static ConcurrentHashMap<String, List<Map<Object, Object>>> holder = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException {
        if (msg instanceof MtTransferReq) {
            MtTransferRes result = process((MtTransferReq) msg);
            ctx.channel().write(result);
        }
    }

    public MtTransferRes process(MtTransferReq mtTransferRes) {
        switch (mtTransferRes.event) {
            case START:
                LOG.info("starting process: " + mtTransferRes.processId);
                holder.put(mtTransferRes.processId, new ArrayList<>());
                break;
            case PROCESS:
                LOG.info("putting object: " + mtTransferRes.entry);
                holder.get(mtTransferRes.processId).add(mtTransferRes.entry);
                break;
            case END:
                LOG.info("finishing process: " + mtTransferRes.processId);
                List<Map<Object, Object>> result = holder.remove(mtTransferRes.processId);
                LOG.info("result map: " + result);
                break;
        }

        return new MtTransferRes(mtTransferRes.processId, ResultStatus.OK);
    }
}
