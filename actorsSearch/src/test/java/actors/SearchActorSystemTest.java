package actors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchActorSystemTest {
    private final List<String> urls = Arrays.asList(
            "http://localhost:8081/search?q=whatDoesTheFoxSay",
            "http://localhost:8082/search?q=whatDoesTheFoxSay",
            "http://localhost:8083/search?q=whatDoesTheFoxSay");

    @BeforeAll
    static void startServers() {
        new SearcherStubServer(8081, Duration.create(100, TimeUnit.MILLISECONDS));
        new SearcherStubServer(8082, Duration.create(200, TimeUnit.MILLISECONDS));
        new SearcherStubServer(8083, Duration.create(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void getAllResponses() {
        SearchActorSystem searcher = new SearchActorSystem(urls, "whatDoesTheFoxSay", Duration.create(4000, TimeUnit.MILLISECONDS));
        HashMap<String, String> searchResult = null;
        try {
            searchResult = searcher.search();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        assert searchResult != null;
        assertEquals(3, searchResult.entrySet().size());
        searchResult.values().forEach(ans -> {
            assertEquals(ans, SearcherStubServer.foxSay);
        });
    }

    @Test
    public void missOneResponse() {
        SearchActorSystem searcher = new SearchActorSystem(urls, "whatDoesTheFoxSay", Duration.create(400, TimeUnit.MILLISECONDS));
        HashMap<String, String> searchResult = null;
        try {
            searchResult = searcher.search();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        assert searchResult != null;
        assertEquals(2, searchResult.entrySet().size());
    }

    @Test
    public void noResponses() {
        SearchActorSystem searcher = new SearchActorSystem(urls, "whatDoesTheFoxSay", Duration.create(0, TimeUnit.MILLISECONDS));
        HashMap<String, String> searchResult = null;
        try {
            searchResult = searcher.search();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        assert searchResult != null;
        assertEquals(0, searchResult.entrySet().size());
    }

}