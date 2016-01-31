package server.map;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

@ChannelHandler.Sharable
public class PlainServerMtMapChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelHandler clientMtMapRequestAdapter;

    public PlainServerMtMapChannelInitializer(ClientMtMapRequestAdapter clientMtMapRequestAdapter) {
        this.clientMtMapRequestAdapter = clientMtMapRequestAdapter;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        //p.addLast(new LoggingHandler(LogLevel.INFO));
        p.addLast(new ObjectEncoder());
        p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

        p.addLast(clientMtMapRequestAdapter);
    }
}
