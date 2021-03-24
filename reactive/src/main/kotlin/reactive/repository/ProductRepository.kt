package reactive.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactive.entity.Product

@Repository
interface ProductRepository: ReactiveMongoRepository<Product, String> {
    fun findByUserId(userId: String): Flux<Product>
}