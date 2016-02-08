package app;

import client.map.MtMapClient;
import client.object.PlainClientObjectContextChannelInitializer;
import client.object.SSLClientObjectContextChannelInitializer;
import dto.map.MtTransferRes;
import dto.map.ResultStatus;
import server.SSLServer;
import server.map.PlainServerMtMapChannelInitializer;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MtClient {

  protected MtMapClient sslClient;

  public MtClient(String serverHost, int serverPort, int threadCount, String privateStorePath, String privateStorePass, String trustStorePath, String trustStorePass) throws Exception {
    sslClient = new MtMapClient(serverHost, serverPort);

    SSLContext clientSSLContext = SSLEngineFactory.createSSLContext(privateStorePath, privateStorePass, trustStorePath, trustStorePass);

    SSLClientObjectContextChannelInitializer<MtTransferRes> sslClientObjectChannelInitializer =
            new SSLClientObjectContextChannelInitializer<>(
                    clientSSLContext,
                    (ctx, resDto, monitor) -> {
                      synchronized (monitor) {
                        monitor.notify();
                      }
                    },
                    new Object()
            );

    sslClient.init(sslClientObjectChannelInitializer, threadCount);
  }

  public MtClient(String serverHost, int serverPort, int threadCount) throws Exception {
    sslClient = new MtMapClient(serverHost, serverPort);

    PlainClientObjectContextChannelInitializer<MtTransferRes> plainClientObjectChannelInitializer =
            new PlainClientObjectContextChannelInitializer<>(
                    (ctx, resDto, monitor) -> {
                        synchronized (monitor) {
                          monitor.notify();
                        }
                      }
                    ,
                    new Object()
            );

    sslClient.init(plainClientObjectChannelInitializer, threadCount);
  }

  public void transfer(Map<Object, Object> data) throws InterruptedException {
    sslClient.start(data);
  }

  public void stop() {
    sslClient.shutdown();
  }

  public static void main(String[] arv) throws Exception {
    //MtClient sslClinet = new MtClient(SSLServer.HOST, SSLServer.PORT, 5, "src/test/resources/sslClinet.private", "clientpw", "src/test/resources/server.public", "public");
    MtClient clinet = new MtClient(SSLServer.HOST, SSLServer.PORT, 5);

    Map<Object, Object> sourceMap = new HashMap<>();

    new SecureRandom().ints().limit(40).forEach(i->{
      sourceMap.put(i, UUID.randomUUID().toString());
    });

    clinet.transfer(sourceMap);

    clinet.stop();
  }
}
