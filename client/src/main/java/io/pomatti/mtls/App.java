package io.pomatti.mtls;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;

public class App {

  // https://stackoverflow.com/questions/24555890/using-a-custom-truststore-in-java-as-well-as-the-default-one
  public static void main(String[] args) throws Exception {

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, Utils.trustAllCerts, new SecureRandom());

    HttpClient client = HttpClient.newHttpClient();

    // System.out.println(System.getenv("JAVA_OPTS"));

    // HttpClient client = HttpClient.newBuilder()
    // .sslContext(sslContext)
    // .build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.bank.local:8443/"))
        // .uri(URI.create("https://httpbin.org/get"))
        .build();

    HttpResponse<String> response = client.send(request,
        BodyHandlers.ofString());

    System.out.println(response.statusCode());
    System.out.println(response.body());
  }
}
