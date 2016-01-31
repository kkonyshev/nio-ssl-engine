package server.map;

import dto.map.MtTransferReq;
import dto.map.MtTransferRes;
import dto.map.ResultStatus;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class ClientMtMapRequestAdapter extends ChannelInboundHandlerAdapter {

    private Logger LOG = LogManager.getLogger();

    public ClientMtMapRequestAdapter() {
        super();
        LOG.info("new instance");
    }

    private final Map<String, ConcurrentHashMap<Object, Object>> holder = new HashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException {
        if (msg instanceof MtTransferReq) {
            MtTransferRes result = process((MtTransferReq) msg);
            ctx.channel().writeAndFlush(result);
        }
    }

    public MtTransferRes process(MtTransferReq mtTransferRes) {
        synchronized (holder) {
            if (holder.get(mtTransferRes.processId)==null) {
                LOG.info("starting process: " + mtTransferRes.processId);
                holder.put(mtTransferRes.processId, new ConcurrentHashMap<>());
            }
        }
        LOG.trace("input object: " + mtTransferRes.entry);

        ConcurrentHashMap<Object, Object> processMap = holder.get(mtTransferRes.processId);
        Map.Entry<Object, Object> next = mtTransferRes.entry.entrySet().iterator().next();
        processMap.put(next.getKey(), next.getValue());
        LOG.trace("size: " + processMap.size());

        if (holder.get(mtTransferRes.processId).size()==mtTransferRes.limit) {
            ConcurrentHashMap<Object, Object> result = holder.remove(mtTransferRes.processId);
            LOG.debug("dumping: " + result);
            return new MtTransferRes(mtTransferRes.processId, ResultStatus.DONE, result.size());
        } else {
            return new MtTransferRes(mtTransferRes.processId, ResultStatus.OK, processMap.size());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.warn(cause.getMessage(), cause);
    }
}
