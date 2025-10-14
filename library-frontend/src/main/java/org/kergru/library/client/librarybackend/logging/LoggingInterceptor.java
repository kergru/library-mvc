package org.kergru.library.client.librarybackend.logging;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request,
      byte[] body,
      ClientHttpRequestExecution execution) throws IOException {

    logRequest(request);

    ClientHttpResponse response = execution.execute(request, body);

    logResponse(response);

    return response;
  }

  private void logRequest(HttpRequest request) {
    System.out.println("=== LibraryBackendClient: Outgoing Request ===");
    System.out.println("URI: " + request.getURI());
    System.out.println("Method: " + request.getMethod());
    System.out.println("Headers: " + request.getHeaders());
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    System.out.println("=== LibraryBackendClient: Incoming Response ===");
    System.out.println("Status: " + response.getStatusCode());
    System.out.println("Headers: " + response.getHeaders());
  }
}