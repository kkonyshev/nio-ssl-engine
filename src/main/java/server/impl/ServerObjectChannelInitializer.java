package server.impl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ServerObjectChannelInitializer extends ChannelInitializer<SocketChannel> {

    private SSLContext sslContext;

    public ServerObjectChannelInitializer(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);

        p.addLast("ssl", new SslHandler(engine));
        p.addLast(new ObjectEncoder());
        p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

        p.addLast(new SimpleClientRequestAdapter());
    }
}
