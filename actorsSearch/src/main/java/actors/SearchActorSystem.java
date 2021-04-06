package actors;

import actors.MasterActor.MasterResponse;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SearchActorSystem {
    public final List<String> urls;
    public final String query;
    public final Duration timeout;

    public final ActorSystem system;

    public SearchActorSystem(List<String> urls, String query, Duration timeout) {
        this.urls = urls;
        this.query = query;
        this.timeout = timeout;
        this.system = ActorSystem.create("actorSystem");
    }

    public HashMap<String, String> search() throws ExecutionException, InterruptedException {
        CompletableFuture<MasterResponse> futureResult = new CompletableFuture<>();
        ActorRef aggregator = system.actorOf(Props.create(MasterActor.class, urls, timeout, futureResult));
        aggregator.tell(query, ActorRef.noSender());
        HashMap<String, String> result = futureResult.get().result;
        system.terminate();
        return result;
    }
}
