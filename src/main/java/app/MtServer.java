package app;

import server.SSLServer;
import server.map.ClientMtMapRequestAdapter;
import server.map.PlainServerMtMapChannelInitializer;
import server.map.SSLServerMtMapChannelInitializer;
import server.map.SimpleExampleClientDataProcessor;
import utils.SSLEngineFactory;

import javax.net.ssl.SSLContext;


public class MtServer {

  protected SSLServer objectProcessingSSLServer;

  public MtServer(int port, String privateStorePath, String privateStorePass, String trustStorePath, String trustStorePass) throws Exception {
    SSLContext serverSSLContext = SSLEngineFactory.createSSLContext(privateStorePath, privateStorePass, trustStorePath, trustStorePass);
    SSLServerMtMapChannelInitializer sslServerMtMapChannelInitializer = new SSLServerMtMapChannelInitializer(serverSSLContext, new SimpleExampleClientDataProcessor());
    objectProcessingSSLServer = new SSLServer();
    objectProcessingSSLServer.start(sslServerMtMapChannelInitializer, port);
  }

  public MtServer(int port) throws Exception {
    PlainServerMtMapChannelInitializer plainServerMtMapChannelInitializer = new PlainServerMtMapChannelInitializer(new SimpleExampleClientDataProcessor());
    objectProcessingSSLServer = new SSLServer();
    objectProcessingSSLServer.start(plainServerMtMapChannelInitializer, port);
  }

  public void stop() {
    objectProcessingSSLServer.stop();
  }

  public static void main(String[] argv) throws Exception {
    //MtServer sslServer = new MtServer(SSLServer.PORT, "src/test/resources/server.private", "serverpw", "src/test/resources/client.public", "public");
    MtServer plainServer = new MtServer(SSLServer.PORT);
  }

}
