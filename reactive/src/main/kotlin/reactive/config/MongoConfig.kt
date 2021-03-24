package reactive.config

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import reactive.repository.ProductRepository
import reactive.repository.UserRepository

@Configuration
@EnableReactiveMongoRepositories(
    basePackageClasses = [UserRepository::class, ProductRepository::class]
)
class MongoConfig : AbstractReactiveMongoConfiguration() {

    override fun getDatabaseName() = "reactive_db"

    override fun reactiveMongoClient() = mongoClient()

    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create()
    }

}