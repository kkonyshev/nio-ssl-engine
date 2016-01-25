import client.ResponseHandler;
import client.impl.ClientObjectChannelInitializer;
import client.impl.SSLClient;
import dto.RequestObject;
import dto.ResponseObject;
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

public class TestPojoTransfering {

    @Test
    public void testPojo1() {
        final SSLServer objectProcessingSSLServer = new SSLServer<RequestObject, ResponseObject>();
        final SSLClient sslClient = new SSLClient(SSLServer.HOST, SSLServer.PORT);
        final SSLClient sslClient2 = new SSLClient(SSLServer.HOST, SSLServer.PORT);


        try {
            SSLContext serverContext = SSLEngineFactory.getServerContext();

            ServerObjectChannelInitializer<RequestObject, ResponseObject> serverObjectChannelInitializer =
                    new ServerObjectChannelInitializer<RequestObject, ResponseObject>(
                            serverContext,
                            new RequestHandler<RequestObject, ResponseObject>() {
                                public ResponseObject handle(RequestObject request) {
                                    return new ResponseObject(request);
                                }
                            }
                    );

            objectProcessingSSLServer.start(
                    serverObjectChannelInitializer,
                    SSLServer.PORT,
                    false
            );


            SSLContext clientContext = SSLEngineFactory.getClientContext();
            ClientObjectChannelInitializer<ResponseObject> clientObjectChannelInitializer = new ClientObjectChannelInitializer<ResponseObject>(clientContext,
                    new ResponseHandler<ResponseObject>() {
                        public void handle(ResponseObject o) {
                            int localSum = o.request.d1 + o.request.d2;
                            assert localSum == o.sum;
                            CRC32 crc = new CRC32();
                            crc.update(o.request.data);
                            assert crc.getValue() == o.checksum;
                        }
                    });

            sslClient.init(clientObjectChannelInitializer);
            sslClient2.init(clientObjectChannelInitializer);

            Executor e = Executors.newFixedThreadPool(5);
            for (int i=0; i<10; i++){
                e.execute(new Runnable() {
                    public void run() {
                        byte[] data = UUID.randomUUID().toString().getBytes();
                        Random rnd = new Random();
                        for (int i=0; i<20; i++ ) {
                            RequestObject msg = new RequestObject(rnd.nextInt(), rnd.nextInt(), data);
                            sslClient.call(msg);
                            sslClient2.call(msg);
                        }
                    }
                });
            }

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objectProcessingSSLServer.stop();
            sslClient.shutdown();
            sslClient2.shutdown();
        }
    }
}
