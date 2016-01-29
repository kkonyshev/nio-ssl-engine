package client.map;

import client.SSLClient;
import client.object.ClientObjectChannelInitializer;
import dto.map.MtTransferReq;
import dto.map.MtTransferRes;
import dto.map.TransferEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MtMapClient {

    protected Logger LOG = LogManager.getLogger();

    private EventLoopGroup group;
    private Bootstrap b;

    private int port;
    private String host;
    private ClientObjectChannelInitializer<MtTransferRes> channelInitializer;

    public static void main(String[] args) {
        //System.setProperty("javax.net.debug","all");
    }


    public MtMapClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public MtMapClient init(ClientObjectChannelInitializer<MtTransferRes> channelInitializer) {
        this.channelInitializer = channelInitializer;

        group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(channelInitializer);
        } catch (Exception e){
            shutdown();
        }

        return this;
    }

    public void call(MtTransferReq requestDto, boolean sync) {
        try {
            ChannelFuture f = b.connect(this.host, this.port).sync();
            Channel channel = f.channel();

            channel.writeAndFlush(requestDto);

            // Wait until the connection is closed.
            ChannelFuture channelFuture = channel.closeFuture();
            if (sync) {
                LOG.info("in sync mode");
                channelFuture.sync();
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted!", e);
        }
    }

    public void shutdown() {
        // Shut down the event loop to terminate all threads.
        LOG.info("Shutting down client bootstrap");
        if(group!=null) {
            group.shutdownGracefully();
        }
    }

    public void finalize() {
        shutdown();
    }

    public void start(Map<Object,Object> sourceMap) throws InterruptedException {
        final String processId = UUID.randomUUID().toString();

        int threadCount = 3;
        Map<Integer, List<Map<Object, Object>>> threadMap = prepareMapForTransfering(threadCount, sourceMap);

        MtTransferReq initReq = new MtTransferReq(processId, TransferEvent.START, null);
        call(initReq, false);

        Thread.sleep(100);

        for (Map.Entry e: sourceMap.entrySet()) {
            Map<Object, Object> iMap = new HashMap<>();
            iMap.put(e.getKey(), e.getValue());
            MtTransferReq dto = new MtTransferReq(processId, TransferEvent.PROCESS, iMap);
            call(dto, false);
        }

        MtTransferReq stopSig = new MtTransferReq(processId, TransferEvent.END, null);
        call(stopSig, false);
    }

    private Map<Integer, List<Map<Object, Object>>> prepareMapForTransfering(int threadCount, Map<Object, Object> sourceMap) {
        List<List<Map<Object, Object>>> listMap = new ArrayList<>();

        int threshold = sourceMap.size()/threadCount + 1;
        Iterator<Map.Entry<Object, Object>> mI = sourceMap.entrySet().iterator();
        while (mI.hasNext()) {
            List<Map<Object, Object>> innerMapList = new ArrayList<>();
            for (int j=0; j<threshold && mI.hasNext(); j++) {
                Map<Object, Object> map = new HashMap<>();
                Map.Entry<Object, Object> entry = mI.next();
                map.put(entry.getKey(), entry.getValue());
                innerMapList.add(map);
            }
            if (!innerMapList.isEmpty()) {
                listMap.add(innerMapList);
            }
        }

        Map<Integer, List<Map<Object, Object>>> threadMap = new HashMap<>();

        for (int i=0; i< listMap.size(); i++) {
            threadMap.put(i, listMap.get(i));
        }
        return threadMap;
    }
}
