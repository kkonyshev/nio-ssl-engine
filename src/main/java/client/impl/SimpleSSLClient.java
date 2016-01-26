package client.impl;

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

public class SimpleSSLClient<RequestDto, ResponseDto> {

    protected Logger LOG = LogManager.getLogger();

    private EventLoopGroup group;
    private Bootstrap b;

    private int port;
    private String host;

    public static void main(String[] args) {
        //System.setProperty("javax.net.debug","all");
    }

    public SimpleSSLClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public SimpleSSLClient<RequestDto, ResponseDto> init(ClientObjectChannelInitializer<ResponseDto> channelInitializer) {
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

    public void call(RequestDto requestDto) {
        call(requestDto, false);
    }

    public void call(RequestDto requestDto, boolean sync) {
        try {
            ChannelFuture f = b.connect(this.host, this.port).sync();
            Channel channel = f.channel();

            processCall(requestDto, channel);

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

    protected void processCall(RequestDto requestDto, Channel channel) {
        channel.write(requestDto);
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
}
