package client;

import client.ResponseHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerResponseAdapter<ResponseDto> extends ChannelInboundHandlerAdapter {

    private ResponseHandler<ResponseDto> handler;

    public ServerResponseAdapter(ResponseHandler<ResponseDto> handler) {
        this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        handler.handle((ResponseDto) msg);
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
