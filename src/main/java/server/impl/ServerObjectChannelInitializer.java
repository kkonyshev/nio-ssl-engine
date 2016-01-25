package server.impl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;
import server.RequestHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ServerObjectChannelInitializer<RequestDto, ResponseDto> extends ChannelInitializer<SocketChannel> {

    private SSLContext sslContext;
    private RequestHandler<RequestDto, ResponseDto> requestHandler;

    public ServerObjectChannelInitializer(SSLContext sslContext, RequestHandler<RequestDto, ResponseDto> requestHandler) {
        this.sslContext = sslContext;
        this.requestHandler = requestHandler;
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

        p.addLast(new ClientRequestAdapter(requestHandler));
    }
}
