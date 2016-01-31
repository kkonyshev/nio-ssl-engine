package client.map;

import client.object.AbstractClientObjectContextChannelInitializer;
import client.object.SSLClientObjectContextChannelInitializer;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MtMapClient {

    protected Logger LOG = LogManager.getLogger();

    private EventLoopGroup group;
    private Bootstrap b;

    private int port;
    private String host;
    private AbstractClientObjectContextChannelInitializer<MtTransferRes> channelInitializer;
    private int maxConnections = 1;
    private final Deque<Channel> queue = new LinkedList<>();
    private int currentConnections = 0;
    private ExecutorService taskExecutor;

    public MtMapClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public MtMapClient init(AbstractClientObjectContextChannelInitializer<MtTransferRes> channelInitializer, int threadCount) {
        if (group!=null) {
            shutdown();
        }

        this.channelInitializer = channelInitializer;

        group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .handler(channelInitializer);

            this.maxConnections = threadCount;
            this.taskExecutor = Executors.newFixedThreadPool(threadCount);

        } catch (Exception e){
            shutdown();
        }

        return this;
    }

    public void start(Map<Object, Object> sourceMap) throws InterruptedException {
        synchronized (this.channelInitializer.getMonitor()) {
            final String processId = UUID.randomUUID().toString();
            sourceMap.forEach((key,value)->{
                taskExecutor.execute(()->{
                    Map<Object, Object> iMap = new HashMap<>();
                    iMap.put(key, value);
                    MtTransferReq dto = new MtTransferReq(processId, TransferEvent.PROCESS, iMap, sourceMap.size());
                    sendMessage(dto);
                });
            });
            this.channelInitializer.getMonitor().wait();
        }
    }

    // wait for, or create a channel
    private Channel acquireChan() throws InterruptedException {
        synchronized(queue) {
            do {
                while (queue.isEmpty()) {
                    if (currentConnections < maxConnections) {
                        // no idle channels, and we have space for another
                        // create a channel, and add it to the queue.
                        // hopefully it will be there when we go around the loop.
                        Channel newChan = b.connect(this.host, this.port).sync().channel();
                        LOG.debug("connection created: " + newChan.localAddress());
                        currentConnections++;
                        queue.add(newChan);
                    } else {
                        // otherwise wait a second, and loop again.
                        // if a channel is returned in the interim, we will be notified.
                        queue.wait(1000);
                    }
                }
                // OK, there's an available channel, make sure it is usable.
                Channel toUse = queue.removeFirst();
                if (toUse.isActive()) {
                    // great, good to go, use it.
                    return toUse;
                }
                // the toUse connection is dead, throw it away.
                currentConnections--;
            } while (true);
        }
    }

    private void returnChan(Channel chan) {
        if (chan!=null) {
            synchronized (queue) {
                queue.addLast(chan);
                queue.notifyAll();
            }
        }
    }

    public void sendMessage(final MtTransferReq message)  {
        Channel ch = null;
        try {
            ch = acquireChan();
            LOG.trace("Sending to Server using Channel: " + ch.localAddress() + " data: " + message);
            ChannelFuture future = ch.writeAndFlush(message);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        } finally {
            returnChan(ch);
        }
    }

    public void shutdown() {
        // Shut down the event loop to terminate all threads.
        LOG.info("Shutting down client bootstrap");
        if(group!=null) {
            group.shutdownGracefully();
        }

        LOG.info("Shutting down client pool");
        queue.forEach(c->c.close());

        LOG.info("Shutting down thread executor");
        if (taskExecutor!=null) {
            taskExecutor.shutdown();
        }
    }

    public void finalize() throws Throwable {
        super.finalize();
        shutdown();
    }


}
