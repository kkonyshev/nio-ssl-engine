package client.object;

import client.ResponseContextHandler;
import client.ServerResponseContextAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class SSLClientObjectContextChannelInitializer<ResponseDto> extends AbstractClientObjectContextChannelInitializer<ResponseDto> {

    private SSLContext sslContext;

    public SSLClientObjectContextChannelInitializer(SSLContext sslContext, ResponseContextHandler<ResponseDto> responseHandler, Object monitor) {
        super(responseHandler, monitor);
        this.sslContext = sslContext;
    }

    @Override
    @SuppressWarnings("unchecked")
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
