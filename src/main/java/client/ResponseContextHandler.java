package client;

import io.netty.channel.ChannelHandlerContext;

public interface ResponseContextHandler<ResponseDto> {
    void handle(ChannelHandlerContext ctx, ResponseDto responseDto, Object monitor);
}
