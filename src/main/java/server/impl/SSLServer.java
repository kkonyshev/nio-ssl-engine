package server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.RequestHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class SSLServer<RequestDto, ResponseDto> {

    public static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    public static final String HOST = System.getProperty("host", "127.0.0.1");

    private Logger LOG = LogManager.getLogger();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SSLContext serverSSLContext;
    private RequestHandler<RequestDto, ResponseDto> handler;

    public SSLServer start(SSLContext sslContext, int port, RequestHandler<RequestDto, ResponseDto> serverHandler, boolean isSync) {
        LOG.info("Starting server...");

        this.serverSSLContext = sslContext;
        this.handler = serverHandler;
        this.bossGroup   = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(4);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();

                            SSLEngine engine = serverSSLContext.createSSLEngine();
                            engine.setUseClientMode(false);
                            engine.setNeedClientAuth(true);

                            p.addLast("ssl", new SslHandler(engine));
                            p.addLast(new ObjectEncoder());
                            p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

                            p.addLast(new ClientRequestAdapter(handler));
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind(port).sync();
            // Wait until the server socket is closed.
            ChannelFuture channelFuture = f.channel().closeFuture();
            if (isSync) {
                LOG.debug("in sync mode");
                channelFuture.sync();
            }
            LOG.info("Server started!");
        } catch (Exception e) {
            LOG.warn("Interrupted", e);
            stop();
        }

        return this;
    }

    public void stop() {
        LOG.info("Shutting down server event loops...");
        if (bossGroup!=null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup!=null) {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        //System.setProperty("javax.net.debug","all");
        // Configure the server.
        //new SSLServer().start(true);
    }
}
