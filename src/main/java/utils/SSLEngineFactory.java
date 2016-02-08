package utils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public final class SSLEngineFactory {

    private static final String CERTIFICATE_ENCODING_ALGORITHM = "SunX509";
    private static final String PROTOCOL = "TLS";

    private static final SSLContext SERVER_CONTEXT;
    private static final SSLContext CLIENT_CONTEXT;

    static {

        SSLContext serverContext;
        SSLContext clientContext;

        try {
            serverContext = createSSLContext("src/test/resources/server.private", "serverpw", "src/test/resources/client.public", "public");
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the server-side SSLContext", e);
        }

        try {
            clientContext = createSSLContext("src/test/resources/client.private", "clientpw", "src/test/resources/server.public", "public");
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the client-side SSLContext", e);
        }

        SERVER_CONTEXT = serverContext;
        CLIENT_CONTEXT = clientContext;
    }

    public static SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }

    public static SSLContext getClientContext() {
        return CLIENT_CONTEXT;
    }

    private SSLEngineFactory() {
        // Unused
    }

    public static SSLContext createSSLContext(
            String keystore,
            String keystore_password,
            String truststore,
            String truststore_password)
            throws Exception
    {
        return init(
                new FileInputStream(keystore), keystore_password,
                new FileInputStream(truststore), truststore_password);
    }

    public static SSLContext init(
            InputStream keystoreStream,
            String keystore_password,
            InputStream truststoreStream,
            String truststore_password)
            throws Exception
    {
        if (keystoreStream == null)
        {
            System.err.println("*** SSL_NO_KEY_STORE");
            throw new IllegalArgumentException("No key store");
        }
        if (truststoreStream == null)
        {
            System.err.println("*** SSL_NO_TRUST_STORE");
            throw new IllegalArgumentException("No trust store");
        }
        // Create/initialize the SSLContext with key material
        char[] key_passphrase = keystore_password.toCharArray();
        // First initialize the key and trust material.
        KeyStore kstore = KeyStore.getInstance("JKS");
        kstore.load(keystoreStream, key_passphrase);

        // Create/initialize the SSLContext with key material
        char[] t_passphrase = truststore_password.toCharArray();
        // First initialize the key and trust material.
        KeyStore tstore = KeyStore.getInstance("JKS");
        tstore.load(truststoreStream, t_passphrase);

        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);

        // KeyManager's decide which key material to use.
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(CERTIFICATE_ENCODING_ALGORITHM);
        kmf.init(kstore, key_passphrase);

        // TrustManager's decide whether to allow connections.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(CERTIFICATE_ENCODING_ALGORITHM);
        tmf.init(tstore);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
}