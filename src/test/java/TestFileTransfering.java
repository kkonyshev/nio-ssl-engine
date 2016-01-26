import client.ResponseHandler;
import client.impl.ClientObjectChannelInitializer;
import client.impl.SSLClient;
import dto.FileTransferReq;
import dto.FileTransferRes;
import dto.RequestObject;
import dto.ResponseObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import server.RequestHandler;
import server.impl.SSLServer;
import server.impl.ServerObjectChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

public class TestFileTransfering {

    private Logger LOG = LogManager.getLogger();

    @Test
    public void testFileTransfer1() {
        final SSLServer objectProcessingSSLServer = new SSLServer<RequestObject, ResponseObject>();
        final SSLClient sslClient = new SSLClient(SSLServer.HOST, SSLServer.PORT);

        try {
            SSLContext serverContext = SSLEngineFactory.getServerContext();

            ServerObjectChannelInitializer<FileTransferReq, FileTransferRes> serverObjectChannelInitializer =
                    new ServerObjectChannelInitializer<FileTransferReq, FileTransferRes>(
                            serverContext,
                            new RequestHandler<FileTransferReq, FileTransferRes>() {
                                public FileTransferRes handle(FileTransferReq request) {
                                    return new FileTransferRes(null, 0);
                                }
                            }
                    );

            objectProcessingSSLServer.start(
                    serverObjectChannelInitializer,
                    SSLServer.PORT,
                    false
            );


            SSLContext clientContext = SSLEngineFactory.getClientContext();
            ClientObjectChannelInitializer<FileTransferRes> clientObjectChannelInitializer = new ClientObjectChannelInitializer<FileTransferRes>(clientContext,
                    new ResponseHandler<FileTransferRes>() {
                        public void handle(FileTransferRes o) {
                            LOG.debug("server request handled");
                            assert o.bytesCount==0;
                        }
                    });

            sslClient.init(clientObjectChannelInitializer);

            sslClient.call(new FileTransferReq("1", null));

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objectProcessingSSLServer.stop();
            sslClient.shutdown();
        }
    }
}
