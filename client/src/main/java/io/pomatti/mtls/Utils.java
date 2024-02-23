package io.pomatti.mtls;

import java.net.http.HttpClient;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utils {

  protected static TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
      }
  };

  protected static HttpClient getHttpClient() throws Exception {
    return getHttpClient(false);
  }

  protected static HttpClient getHttpClient(boolean trustAllCerts) throws Exception {
    if (trustAllCerts == false) {
      return HttpClient.newHttpClient();
    } else {
      // https://stackoverflow.com/questions/24555890/using-a-custom-truststore-in-java-as-well-as-the-default-one
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, Utils.trustAllCerts, new SecureRandom());
      return HttpClient.newBuilder()
          .sslContext(sslContext)
          .build();
    }
  }
}
