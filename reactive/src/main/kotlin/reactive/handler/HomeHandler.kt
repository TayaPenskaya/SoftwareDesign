package reactive.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactive.repository.UserRepository

@Component
class HomeHandler(private val userRepository: UserRepository) {

    fun home(request: ServerRequest): Mono<ServerResponse> =
        request.session().flatMap { session ->
            session.attributes["id"]
                ?.let { userRepository.findById(it as String) }
                ?.flatMap {
                    ok().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("greet" to "Hello, ${it.username!!}!"))
                }?.switchIfEmpty { Mono.error { error("Can not find user") } }
                ?.onErrorResume { e ->
                    e.printStackTrace()
                    ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("error" to e))
                } ?: ok().contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("greet" to "Hello, Stranger!"))

        }
}
