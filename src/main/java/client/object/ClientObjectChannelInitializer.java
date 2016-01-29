package client.object;

import client.ResponseHandler;
import client.ServerResponseAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ClientObjectChannelInitializer<ResponseDto> extends ChannelInitializer<SocketChannel> {

    private SSLContext sslContext;
    private ResponseHandler<ResponseDto> responseHandler;

    public ClientObjectChannelInitializer(SSLContext sslContext, ResponseHandler<ResponseDto> responseHandler) {
        this.sslContext = sslContext;
        this.responseHandler = responseHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(true);
        engine.setNeedClientAuth(true);

        p.addLast("ssl", new SslHandler(engine));
        //p.addLast(new LoggingHandler(LogLevel.INFO));
        p.addLast(new ObjectEncoder());
        p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

        p.addLast(new ServerResponseAdapter(responseHandler));
    }
}
