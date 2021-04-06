package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MasterActor extends AbstractActor {
    private final List<String> urls;
    private final MasterResponse result;
    private final CompletableFuture<MasterResponse> futureResult;

    public static class MasterResponse {
        public HashMap<String, String> result;

        public MasterResponse() {
            result = new HashMap<>();
        }
    }

    public MasterActor(List<String> urls, Duration duration, CompletableFuture<MasterResponse> futureResult) {
        this.urls = urls;
        this.futureResult = futureResult;

        this.getContext().setReceiveTimeout(duration);
        result = new MasterResponse();
    }


    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(String.class, this::sendRequest)
                .match(ChildActor.Response.class, this::aggregate)
                .match(ReceiveTimeout.class, this::returnResult)
                .build();
    }

    public void aggregate(ChildActor.Response childResult) {
        result.result.put(childResult.usedUrl.toString(),
                childResult.response);

        if (result.result.size() == urls.size()) {
            futureResult.complete(result);
            getContext().system().stop(getSelf());
        }
    }

    private void sendRequest(String request) {
       urls.forEach(searcher -> getContext()
                        .actorOf(Props.create(ChildActor.class, searcher))
                        .tell(request, getSelf()));
    }

    private void returnResult(ReceiveTimeout msg) {
        futureResult.complete(result);
        getContext().system().stop(getSelf());
    }
}