package client.object;

import client.ResponseContextHandler;
import client.ServerResponseContextAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class PlainClientObjectContextChannelInitializer<ResponseDto> extends AbstractClientObjectContextChannelInitializer<ResponseDto> {

    public PlainClientObjectContextChannelInitializer(ResponseContextHandler<ResponseDto> responseHandler, Object monitor) {
        super(responseHandler, monitor);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        //p.addLast(new LoggingHandler(LogLevel.INFO));
        p.addLast(new ObjectEncoder());
        p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

        p.addLast(new ServerResponseContextAdapter(responseHandler, monitor));
    }

    public Object getMonitor() {
        return monitor;
    }
}
