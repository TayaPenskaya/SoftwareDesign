package reactive.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactive.entity.User
import reactive.repository.UserRepository

@Component
class UserHandler(private val userRepository: UserRepository) {
    val mapper = ObjectMapper()


    fun register(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono(String::class.java)
            .flatMap {
                Mono.fromCallable { mapper.readValue(it, User::class.java) }
            }.flatMap { user ->
                val username = user.username ?: error("Username must not be null")
                user.currency ?: error("currency must not be null")

                userRepository.findByUsername(username)
                    .flatMap { Mono.error<User> { error("User with username $username already exists") } }
                    .switchIfEmpty(
                        Mono.defer { userRepository.save(user) }
                    ).flatMap { savedUser ->
                        ok().contentType(MediaType.APPLICATION_JSON).bodyValue(savedUser)
                    }
            }.onErrorResume { e ->
                e.printStackTrace()
                badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("error" to e))
            }


    fun login(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono(String::class.java)
            .flatMap {
                Mono.fromCallable { mapper.readValue(it, User::class.java) }
            }.flatMap { user ->
                val username = user.username ?: error("Username must not be null")
                userRepository.findByUsername(username)
                    .switchIfEmpty { Mono.error { error("Wrong username") } }
            }.flatMap { user ->
                request.session().flatMap { session ->
                    session.apply {
                        attributes["id"] = user.id
                    }
                    ok().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("user" to user, "sessionId" to session.id))
                }
            }.onErrorResume { e ->
                e.printStackTrace()
                badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("error" to e))
            }

    fun logout(request: ServerRequest): Mono<ServerResponse> =
        request.session().flatMap {
            it.attributes.remove("id")
            ok().build()
        }
}

