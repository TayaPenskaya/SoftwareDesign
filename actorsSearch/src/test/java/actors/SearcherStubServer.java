package actors;

import org.mockserver.integration.ClientAndServer;
import scala.concurrent.duration.Duration;


import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

public class SearcherStubServer implements AutoCloseable {
    public static String foxSay = "Ring-ding-ding-ding-dingeringeding!";
    private final ClientAndServer stubServer;

    SearcherStubServer(int port, Duration timeout) {
        stubServer = startClientAndServer(port);
        stubServer.when(request()
                .withMethod("GET")
                .withPath("/search")
        ).respond(
                request -> {
                    if (timeout.toMillis() > 0)
                        Thread.sleep(timeout.toMillis());

                    return response()
                            .withStatusCode(OK_200.code())
                            .withBody(foxSay);
                }
        );
    }

    @Override
    public void close() {
        stubServer.close();
    }
}
