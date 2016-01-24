package client.impl;

import client.ResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class SSLClient<RequestDto, ResponseDto> {

    private Logger LOG = LogManager.getLogger();

    private SSLContext sslContext;
    ResponseHandler<ResponseDto> handler;

    private EventLoopGroup group;
    private Bootstrap b;

    private int port;
    private String host;

    public static void main(String[] args) {
        //System.setProperty("javax.net.debug","all");
    }

    public SSLClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public SSLClient<RequestDto, ResponseDto> init(SSLContext clientContext, final ResponseHandler<ResponseDto> clientHandler) {
        this.sslContext = clientContext;
        this.handler = clientHandler;
        group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();

                            SSLEngine engine = sslContext.createSSLEngine();
                            engine.setUseClientMode(true);
                            engine.setNeedClientAuth(true);

                            p.addLast("ssl", new SslHandler(engine));
                            p.addLast(new ObjectEncoder());
                            p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                            p.addLast(new ServerResponseAdapter(handler));
                        }
                    });
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
            channel.write(requestDto);
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
}
