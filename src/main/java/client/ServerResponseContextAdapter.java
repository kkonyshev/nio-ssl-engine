package client;

import com.sun.xml.internal.ws.client.ResponseContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerResponseContextAdapter<ResponseDto> extends ChannelInboundHandlerAdapter {

    private ResponseContextHandler<ResponseDto> handler;
    private Object monitor;

    public ServerResponseContextAdapter(ResponseContextHandler<ResponseDto> handler, Object monitor) {
        this.handler = handler;
        this.monitor = monitor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        handler.handle(ctx, (ResponseDto) msg, monitor);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
