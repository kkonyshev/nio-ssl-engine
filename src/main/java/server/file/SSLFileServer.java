package server.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SSLFileServer {

    public static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    public static final String HOST = System.getProperty("host", "127.0.0.1");

    private Logger LOG = LogManager.getLogger();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public SSLFileServer start(ChannelInitializer serverObjectChannelInitializer, int port, boolean isSync) {
        LOG.info("Starting server...");

        this.bossGroup   = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(4);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(serverObjectChannelInitializer);

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
