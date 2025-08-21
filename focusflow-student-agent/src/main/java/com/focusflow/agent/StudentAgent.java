package com.focusflow.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

public class StudentAgent {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java StudentAgent <clientId> <wsUrl>");
            return;
        }

        String clientId = args[0];
        String wsUrl = args[1];

        System.out.println("Starting StudentAgent with id: " + clientId + " connecting to " + wsUrl);

        HttpClient client = HttpClient.newHttpClient();
        client.newWebSocketBuilder()
              .buildAsync(URI.create(wsUrl), new Listener() {
                  @Override
                  public void onOpen(WebSocket webSocket) {
                      System.out.println("Connected. Registering...");
                      webSocket.sendText("{\"type\":\"register\",\"clientId\":\"" + clientId + "\"}", true);
                      Listener.super.onOpen(webSocket);
                  }

                  @Override
                  public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                      System.out.println("WS Message: " + data);
                      return Listener.super.onText(webSocket, data, last);
                  }
              });
    }
}
