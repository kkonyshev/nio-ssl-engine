import client.map.MtMapClient;
import client.object.AbstractClientObjectContextChannelInitializer;
import client.object.PlainClientObjectContextChannelInitializer;
import client.object.SSLClientObjectContextChannelInitializer;
import dto.map.MtTransferRes;
import dto.map.ResultStatus;
import io.netty.channel.ChannelInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import server.SSLServer;
import server.map.ClientMtMapRequestAdapter;
import server.map.PlainServerMtMapChannelInitializer;
import server.map.SSLServerMtMapChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMtTransfer {

    private final Logger LOG = LogManager.getLogger();

    @SuppressWarnings("unchecked")
    public void testClient(ChannelInitializer serverChannelInitializer, AbstractClientObjectContextChannelInitializer clientChannelInitializer, int threadCount, int mapSize, AtomicInteger count) {
        final SSLServer objectProcessingSSLServer = new SSLServer();
        final MtMapClient sslClient = new MtMapClient(SSLServer.HOST, SSLServer.PORT);
        try {
            objectProcessingSSLServer.start(serverChannelInitializer, SSLServer.PORT);
            sslClient.init(clientChannelInitializer, threadCount);

            Map<Object, Object> sourceMap = new HashMap<>();

            new SecureRandom().ints().limit(mapSize).forEach(i->{
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


    @Test
    public void testSSL() throws Exception {
        int mapSize = 100;
        int threadCount = 2;

        SSLContext serverSSLContext = SSLEngineFactory.createSSLContext("src/test/resources/server.private", "serverpw", "src/test/resources/client.public", "public");
        SSLContext clientSSLContext = SSLEngineFactory.createSSLContext("src/test/resources/client.private", "clientpw", "src/test/resources/server.public", "public");

        AtomicInteger count = new AtomicInteger();
        ClientMtMapRequestAdapter clientMtMapRequestAdapter =
                new ClientMtMapRequestAdapter();
        SSLServerMtMapChannelInitializer sslServerMtMapChannelInitializer =
                new SSLServerMtMapChannelInitializer(serverSSLContext, clientMtMapRequestAdapter);
        SSLClientObjectContextChannelInitializer<MtTransferRes> sslClientObjectChannelInitializer =
                new SSLClientObjectContextChannelInitializer<>(
                        clientSSLContext,
                        (ctx, resDto, monitor) -> {
                            int i = count.decrementAndGet();
                            if (resDto.status==ResultStatus.DONE) {
                                LOG.debug("server response: " + resDto.status + " count=" + i);
                                if (i==0) {
                                    assert resDto.size==mapSize;
                                }
                                synchronized (monitor) {
                                    monitor.notify();
                                }
                            }
                        },
                        new Object()
                );

        testClient(sslServerMtMapChannelInitializer, sslClientObjectChannelInitializer, threadCount, mapSize, count);
    }

    @Test
    public void testPlainSocket() {
        int mapSize = 1000;
        int threadCount = 5;

        AtomicInteger count = new AtomicInteger();
        ClientMtMapRequestAdapter clientMtMapRequestAdapter =
                new ClientMtMapRequestAdapter();
        PlainServerMtMapChannelInitializer plainServerMtMapChannelInitializer
                = new PlainServerMtMapChannelInitializer(clientMtMapRequestAdapter);

        PlainClientObjectContextChannelInitializer<MtTransferRes> plainClientObjectChannelInitializer =
                new PlainClientObjectContextChannelInitializer<>(
                        (ctx, resDto, monitor) -> {
                            int i = count.decrementAndGet();
                            if (resDto.status==ResultStatus.DONE) {
                                LOG.debug("server response: " + resDto.status + " count=" + i);
                                if (i==0) {
                                    assert resDto.size==mapSize;
                                }
                                synchronized (monitor) {
                                    monitor.notify();
                                }
                            }
                        },
                        new Object()
                );

        testClient(plainServerMtMapChannelInitializer, plainClientObjectChannelInitializer, threadCount, mapSize, count);
    }
}
