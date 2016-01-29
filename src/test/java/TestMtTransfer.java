import client.ResponseHandler;
import client.impl.ClientObjectChannelInitializer;
import client.impl.SimpleSSLClient;
import dto.RequestObject;
import dto.ResponseObject;
import dto.map.MtTransferRes;
import org.junit.Assert;
import org.junit.Test;
import server.impl.SSLServer;
import server.impl.ServerMtMapChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

public class TestMtTransfer {

    @Test
    public void testMtTransferReq() {
        AtomicInteger reqCount = new AtomicInteger();

        final SSLServer objectProcessingSSLServer = new SSLServer();
        final SimpleSSLClient simpleSslClient = new SimpleSSLClient(SSLServer.HOST, SSLServer.PORT);
        final SimpleSSLClient simpleSslClient2 = new SimpleSSLClient(SSLServer.HOST, SSLServer.PORT);


        try {
            SSLContext serverContext = SSLEngineFactory.getServerContext();

            ServerMtMapChannelInitializer serverObjectChannelInitializer =
                    new ServerMtMapChannelInitializer(serverContext);

            objectProcessingSSLServer.start(
                    serverObjectChannelInitializer,
                    SSLServer.PORT,
                    false
            );


            SSLContext clientContext = SSLEngineFactory.getClientContext();
            ClientObjectChannelInitializer<MtTransferRes> clientObjectChannelInitializer = new ClientObjectChannelInitializer<MtTransferRes>(clientContext,
                    new ResponseHandler<MtTransferRes>() {
                        public void handle(MtTransferRes o) {
                            System.out.println("server response: " + o.status);
                        }
                    });

            simpleSslClient.init(clientObjectChannelInitializer);
            simpleSslClient2.init(clientObjectChannelInitializer);

            Executor e = Executors.newFixedThreadPool(2);
            for (int i=0; i<3; i++){
                e.execute(new Runnable() {
                    public void run() {
                        byte[] data = UUID.randomUUID().toString().getBytes();
                        Random rnd = new Random();
                        for (int i=0; i<5; i++ ) {
                            RequestObject msg = new RequestObject(rnd.nextInt(), rnd.nextInt(), data);
                            simpleSslClient.call(msg);
                            reqCount.incrementAndGet();
                            simpleSslClient2.call(msg);
                            reqCount.incrementAndGet();
                        }
                    }
                });
            }

            Assert.assertEquals(0, reqCount.get());
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objectProcessingSSLServer.stop();
            simpleSslClient.shutdown();
            simpleSslClient2.shutdown();
        }
    }
}
