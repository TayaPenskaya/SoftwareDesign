package ru.tpenskaya.itmo.sd.stock.util


typealias Json = Map<String, String?>


data class Body constructor(
    var companyName: String? = null,
    var sharesPrice: String? = null,
    val amount: Int? = null,
    val companyId: String? = null,
    val login: String? = null,
    val initialBalance: String? = null,
    val valueToIncrease: String? = null,
    val userId: String? = null
)

