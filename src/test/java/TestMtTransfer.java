import client.ResponseHandler;
import client.SSLClient;
import client.map.MtMapClient;
import client.object.ClientObjectChannelInitializer;
import dto.map.MtTransferRes;
import dto.object.RequestObject;
import org.junit.Assert;
import org.junit.Test;
import server.SSLServer;
import server.map.ServerMtMapChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMtTransfer {

    @Test
    public void testMtTransferReq() {
        AtomicInteger reqCount = new AtomicInteger();

        final SSLServer objectProcessingSSLServer = new SSLServer();
        final MtMapClient sslClient = new MtMapClient(SSLServer.HOST, SSLServer.PORT);

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

            sslClient.init(clientObjectChannelInitializer);

            Map<Object, Object> sourceMap = new HashMap<>();
            sourceMap.put(1, 2);
            sourceMap.put(2, 3);
            sourceMap.put(4, 1);
            sourceMap.put("key", "value");
            sourceMap.put("long-key", "value2");
            sourceMap.put(3, 0);
            sourceMap.put(40, 22);
            sourceMap.put(31, 2);
            sourceMap.put(34, 1);

            sslClient.start(sourceMap);

            Assert.assertEquals(0, reqCount.get());
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objectProcessingSSLServer.stop();
            sslClient.shutdown();
        }
    }
}
