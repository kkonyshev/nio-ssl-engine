package client.file;

import client.SSLClient;
import dto.file.FileTransferReq;
import dto.file.FileTransferReqStatus;
import io.netty.channel.Channel;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;

public class FileSSLClient extends SSLClient<FileTransferReq, FileTransferReq> {

    public FileSSLClient(String host, int port) {
        super(host, port);
    }

    protected void processCall(FileTransferReq requestDto, Channel channel) {

        try {
            RandomAccessFile file = new RandomAccessFile(requestDto.lPath, "r");
            FileChannel inChannel = file.getChannel();

            FileTransferReq init =
                    new FileTransferReq(
                            FileTransferReqStatus.START,
                            requestDto.lPath,
                            requestDto.rPath,
                            null
                    );
            channel.write(init);

            CRC32 in = new CRC32();
            //max byteByffer size is 1048576
            ByteBuffer buf = ByteBuffer.allocate(1040576);
            int bytesRead;
            while ((bytesRead = inChannel.read(buf)) > 0) {
                LOG.trace("read buffer: " + buf);
                buf.flip();
                while (buf.hasRemaining()) {
                    byte[] arr = new byte[buf.remaining()];
                    buf.get(arr);
                    FileTransferReq out =
                            new FileTransferReq(
                                    FileTransferReqStatus.PROCESS,
                                    requestDto.lPath,
                                    requestDto.rPath,
                                    arr
                            );
                    in.update(arr);
                    channel.write(out);
                }
                buf.clear();
            }
            FileTransferReq out =
                    new FileTransferReq(
                            FileTransferReqStatus.END,
                            requestDto.lPath,
                            requestDto.rPath,
                            null,
                            in.getValue(),
                            null
                            );

            channel.write(out);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
