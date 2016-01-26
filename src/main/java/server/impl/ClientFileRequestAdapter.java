package server.impl;

import dto.FileTransferReq;
import dto.FileTransferReqStatus;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.zip.CRC32;

@Sharable
public class ClientFileRequestAdapter extends ChannelInboundHandlerAdapter {

    private Logger LOG = LogManager.getLogger();

    private RandomAccessFile oFile;
    private FileChannel outChannel;
    private long fileSize = 0;
    private long dateStart;
    private CRC32 out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, IOException {
        if (msg instanceof FileTransferReq) {
            FileTransferReq req = (FileTransferReq)msg;
            switch (req.status) {
                case START:
                    LOG.info(req.status + "| from: " + req.lPath + "; to: " + req.rPath);
                    oFile = new RandomAccessFile(req.rPath, "rw");
                    outChannel = oFile.getChannel();
                    dateStart = new Date().getTime();
                    out = new CRC32();
                    break;
                case PROCESS:
                    LOG.trace(req.status + "| chunk size: " + req.payload.length);
                    out.update(req.payload);
                    fileSize=fileSize+req.payload.length;
                    ByteBuffer writeBuff = ByteBuffer.wrap(req.payload);
                    outChannel.write(writeBuff);
                    break;
                case END:
                    long dateEnd = new Date().getTime();
                    long totalTime = dateEnd - dateStart;
                    LOG.info(req.status + "| " + fileSize + " in " + totalTime + " ms @" + String.format("%-5.2f", fileSize/1024./1024./totalTime*1000.) + "Mb/sec");
                    outChannel.close();
                    oFile.close();
                    FileTransferReq res = new FileTransferReq(FileTransferReqStatus.DONE, req.lPath, req.rPath, null, req.lCRC, out.getValue());
                    ctx.write(res).addListener(ChannelFutureListener.CLOSE);
                    break;
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException {
        // Close the connection when an exception is raised.
        LOG.error(cause.getMessage(), cause);
        ctx.close();

        if (outChannel!=null) {
            outChannel.close();
        }
        if (oFile!=null) {
            oFile.close();
        }
    }
}
