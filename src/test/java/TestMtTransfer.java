import client.map.MtMapClient;
import client.object.ClientObjectContextChannelInitializer;
import dto.map.MtTransferRes;
import dto.map.ResultStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import server.SSLServer;
import server.map.ClientMtMapRequestAdapter;
import server.map.ServerMtMapChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMtTransfer {

    private final Logger LOG = LogManager.getLogger();

    @Test
    public void testMtTransferReq() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        final SSLServer objectProcessingSSLServer = new SSLServer();
        final MtMapClient sslClient = new MtMapClient(SSLServer.HOST, SSLServer.PORT);
        try {
            SSLContext serverContext = SSLEngineFactory.getServerContext();

            ServerMtMapChannelInitializer serverObjectChannelInitializer =
                    new ServerMtMapChannelInitializer(serverContext, new ClientMtMapRequestAdapter());

            objectProcessingSSLServer.start(
                    serverObjectChannelInitializer,
                    SSLServer.PORT,
                    false
            );

            final int maxSize = 5000;
            SSLContext clientContext = SSLEngineFactory.getClientContext();

            ClientObjectContextChannelInitializer<MtTransferRes> clientObjectChannelInitializer =
                    new ClientObjectContextChannelInitializer<>(
                            clientContext,
                            (ctx, resDto, monitor) -> {
                                int i = count.decrementAndGet();
                                if (resDto.status==ResultStatus.DONE) {
                                    LOG.debug("server response: " + resDto.status + " count=" + i);
                                    if (i==0) {
                                        assert resDto.size==maxSize;
                                    }
                                    synchronized (monitor) {
                                        monitor.notify();
                                    }
                                }
                            },
                            new Object()
                    );

            sslClient.init(clientObjectChannelInitializer, 3);


            Map<Object, Object> sourceMap = new HashMap<>();

            new SecureRandom().ints().limit(maxSize).forEach(i->{
                sourceMap.put(i, UUID.randomUUID().toString());
                count.incrementAndGet();
            });

            sslClient.start(sourceMap);

            Assert.assertTrue(count.get()==0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            objectProcessingSSLServer.stop();
            sslClient.shutdown();
        }
    }
}
