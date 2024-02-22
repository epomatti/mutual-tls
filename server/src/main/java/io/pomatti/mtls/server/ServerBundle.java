package io.pomatti.mtls.server;

import javax.net.ssl.SSLContext;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;

@Component
public class ServerBundle {

  @SuppressWarnings("unused")
  private final SSLContext sslContext;

  public ServerBundle(SslBundles sslBundles) {
    SslBundle sslBundle = sslBundles.getBundle("server");
    sslContext = sslBundle.createSslContext();
  }

}
