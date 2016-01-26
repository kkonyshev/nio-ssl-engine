import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFile {

    private Logger LOG = LogManager.getLogger();

    @Test
    public void testFile1() throws IOException {
        /*RandomAccessFile file = new RandomAccessFile("target/out", "rw");
        file.setLength(1024*1024);
        file.close();*/


        RandomAccessFile file = new RandomAccessFile("target/in", "r");
        FileChannel inChannel = file.getChannel();

        Files.delete(Paths.get("target/out"));

        RandomAccessFile oFile = new RandomAccessFile("target/out", "rw");
        FileChannel outChannel = oFile.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(8192);
        int bytesRead;
        while ((bytesRead = inChannel.read(buf))>0) {
            LOG.info(bytesRead + ": " + buf);
            buf.flip();
            while(buf.hasRemaining()) {
                outChannel.write(buf);
            }
            buf.clear();
        }

        inChannel.close();
        outChannel.close();
        oFile.close();
        file.close();
    }
}
