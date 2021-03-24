package reactive.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactive.entity.User

@Repository
interface UserRepository: ReactiveMongoRepository<User, String> {
    fun findByUsername(username: String): Mono<User>
}