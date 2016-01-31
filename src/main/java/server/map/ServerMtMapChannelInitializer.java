package server.map;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@ChannelHandler.Sharable
public class ServerMtMapChannelInitializer extends ChannelInitializer<SocketChannel> {

    private SSLContext sslContext;
    private ChannelHandler clientMtMapRequestAdapter;

    public ServerMtMapChannelInitializer(SSLContext sslContext, ClientMtMapRequestAdapter clientMtMapRequestAdapter) {
        this.sslContext = sslContext;
        this.clientMtMapRequestAdapter = clientMtMapRequestAdapter;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);

        p.addLast("ssl", new SslHandler(engine));
        //p.addLast(new LoggingHandler(LogLevel.INFO));
        p.addLast(new ObjectEncoder());
        p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

        p.addLast(clientMtMapRequestAdapter);
    }
}
