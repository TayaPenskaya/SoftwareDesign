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
import reactive.entity.Product
import reactive.repository.ProductRepository
import reactive.repository.UserRepository

@Component
class ProductHandler(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {
    private val mapper = ObjectMapper()

    fun save(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono(String::class.java)
            .flatMap {
                Mono.fromCallable { mapper.readValue(it, Product::class.java) }
            }.flatMap { product ->
                request.session().flatMap { session ->
                    (session.attributes["id"] as String?)
                        ?.let { id -> userRepository.findById(id) }
                }.switchIfEmpty(
                    Mono.error { error("unregistered users can not add new products") }
                ).flatMap { user ->
                    productRepository.save( product.apply { userId = user.id })
                }.flatMap { savedProduct ->
                    ok().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("savedProduct" to savedProduct))
                }.switchIfEmpty { error("Can not save product") }
            }.onErrorResume { e ->
                e.printStackTrace()
                badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("error" to e))
            }

    fun listAll(request: ServerRequest): Mono<ServerResponse> =
        request.session().flatMap { session ->
            val id = session.attributes["id"]!! as String
            userRepository.findById(id)
        }.flatMap { user ->
            productRepository.findAll().map { product ->
                val preferredCurrency = user.currency!!
                val price = product.currency!!.convertToPreferredCurrency(product.price!!, preferredCurrency)
                Product(name = product.name, price = price, currency = preferredCurrency)
            }.collectList().flatMap { products ->
                ok().contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(mapOf("products" to products, "username" to user.username!!))
            }
        }.switchIfEmpty {
            error("Only authorized users can view product list")
        }.onErrorResume { e ->
            e.printStackTrace()
            badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(mapOf("error" to e))
        }
}