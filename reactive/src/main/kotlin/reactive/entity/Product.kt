package reactive.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import reactive.enums.Currency

@Document(collection = "product")
data class Product(
    @Id var id: String? = null,
    var name: String? = null,
    var userId: String? = null,
    var price: Double? = null,
    var currency: Currency? = null
)