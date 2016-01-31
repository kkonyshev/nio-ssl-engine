package client.object;

import client.ResponseContextHandler;
import client.ResponseHandler;
import client.ServerResponseAdapter;
import client.ServerResponseContextAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class ClientObjectContextChannelInitializer<ResponseDto> extends ChannelInitializer<SocketChannel> {

    private SSLContext sslContext;
    private ResponseContextHandler<ResponseDto> responseHandler;
    private Object monitor;

    public ClientObjectContextChannelInitializer(SSLContext sslContext, ResponseContextHandler<ResponseDto> responseHandler, Object monitor) {
        this.sslContext = sslContext;
        this.responseHandler = responseHandler;
        this.monitor = monitor;
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

        p.addLast(new ServerResponseContextAdapter(responseHandler, monitor));
    }

    public Object getMonitor() {
        return monitor;
    }
}
