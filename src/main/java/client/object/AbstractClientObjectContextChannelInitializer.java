package client.object;

import client.ResponseContextHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public abstract class AbstractClientObjectContextChannelInitializer<ResponseDto> extends ChannelInitializer<SocketChannel> {

    protected ResponseContextHandler<ResponseDto> responseHandler;
    protected Object monitor;

    public AbstractClientObjectContextChannelInitializer(ResponseContextHandler<ResponseDto> responseHandler, Object monitor) {
        this.responseHandler = responseHandler;
        this.monitor = monitor;
    }

    public Object getMonitor() {
        return monitor;
    }
}
