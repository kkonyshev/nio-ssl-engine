package client.impl;

import dto.FileTransferReq;
import dto.FileTransferReqStatus;
import io.netty.channel.Channel;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.zip.CRC32;

public class MtMapClient extends SimpleSSLClient<FileTransferReq, FileTransferReq> {

    public MtMapClient(String host, int port) {
        super(host, port);
    }

    public start(Map<Object,Object>) {

    }

    protected void processCall(FileTransferReq requestDto, Channel channel) {
        channel.write(requestDto);
    }

}
