package reactive.route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router
import reactive.handler.HomeHandler
import reactive.handler.ProductHandler
import reactive.handler.UserHandler

@Configuration
class Routes(
    private val homeHandler: HomeHandler,
    private val userHandler: UserHandler,
    private val productHandler: ProductHandler
) {

    @Bean
    fun userRouter() = router {
        GET("/", homeHandler::home)
        GET("/logout", userHandler::logout)

        POST("/register", userHandler::register)
        POST("/login", userHandler::login)
    }

    @Bean
    fun productRouter() = router {
        GET("/products", productHandler::listAll)

        POST("/save/product", productHandler::save)
    }
}
