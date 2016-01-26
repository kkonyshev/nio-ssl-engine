import client.ResponseHandler;
import client.impl.ClientObjectChannelInitializer;
import client.impl.FileSSLClient;
import dto.FileTransferReq;
import dto.FileTransferReqStatus;
import org.junit.Test;
import server.impl.SSLFileServer;
import server.impl.SSLServer;
import server.impl.ServerFileChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TestPojo2StringTransfering {

    @Test
    public void testPojo1() throws IOException {

        SSLFileServer fileServer = new SSLFileServer();
        final FileSSLClient fileClient = new FileSSLClient(SSLFileServer.HOST, SSLFileServer.PORT);
        final FileSSLClient fileClient2 = new FileSSLClient(SSLFileServer.HOST, SSLFileServer.PORT);

        try {
            SSLContext serverContext = SSLEngineFactory.getServerContext();

            ServerFileChannelInitializer serverObjectChannelInitializer =
                    new ServerFileChannelInitializer(serverContext);

            fileServer.start(
                    serverObjectChannelInitializer,
                    SSLServer.PORT,
                    false
            );


            SSLContext clientContext = SSLEngineFactory.getClientContext();
            ClientObjectChannelInitializer<FileTransferReq> clientObjectChannelInitializer = new ClientObjectChannelInitializer<FileTransferReq>(
                    clientContext,
                    new ResponseHandler<FileTransferReq>() {
                        public void handle(FileTransferReq o) {
                            assert o.lCRC.equals(o.rCRC);
                        }
                    });

            fileClient.init(clientObjectChannelInitializer);
            fileClient2.init(clientObjectChannelInitializer);


            final Path filePath = Paths.get("target/file/");

            Arrays.asList(filePath.toFile().listFiles()).stream().forEach(f->f.delete());

            Files.deleteIfExists(filePath);
            Files.createDirectory(filePath);


            Executor e = Executors.newFixedThreadPool(2);
            for (int i=0; i<5; i++){
                e.execute(new Runnable() {
                    public void run() {
                        for (int i=0; i<3; i++ ) {
                            //client1
                            String fileName = filePath.toFile().getAbsoluteFile() + "/" + UUID.randomUUID().toString();
                            try {
                                RandomAccessFile f = new RandomAccessFile(fileName, "rw");
                                f.setLength(new Random().nextInt(1024*1024));
                                f.close();
                                fileClient.call(
                                        new FileTransferReq(
                                                FileTransferReqStatus.INIT,
                                                fileName,
                                                fileName+".server",
                                                null
                                        )
                                );
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                            //client2
                            String fileName2 = filePath.toFile().getAbsoluteFile() + "/" + UUID.randomUUID().toString();
                            try {
                                RandomAccessFile f2 = new RandomAccessFile(fileName2, "rw");
                                f2.setLength(new Random().nextInt(1024*1024));
                                f2.close();
                                fileClient2.call(
                                        new FileTransferReq(
                                                FileTransferReqStatus.INIT,
                                                fileName2,
                                                fileName2+".server",
                                                null
                                        )
                                );
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            fileServer.stop();
            fileClient.shutdown();
            fileClient2.shutdown();
        }
    }
}
