package actors;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ChildActor extends AbstractActor {
    String url;

    public static class Response {
        public String response;
        public String usedUrl;

        public Response(String response, String usedUrl) {
            this.response = response;
            this.usedUrl = usedUrl;
        }
    }

    public ChildActor(String url) {
        this.url = url;
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder().match(String.class, this::onReceive).build();
    }

    public void onReceive(String msg) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        String response = null;
        try {
            response = client
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body().intern();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        sender().tell(new Response(response, url), getSelf());
    }
}
