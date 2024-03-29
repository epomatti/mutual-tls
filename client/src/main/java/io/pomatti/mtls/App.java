package io.pomatti.mtls;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class App {

  public static void main(String[] args) throws Exception {
    HttpClient client = Utils.getHttpClient(false);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.bank.local:8443/"))
        .build();

    HttpResponse<String> response = client.send(request,
        BodyHandlers.ofString());

    System.out.println(response.statusCode());
    System.out.println(response.body());
  }
}
