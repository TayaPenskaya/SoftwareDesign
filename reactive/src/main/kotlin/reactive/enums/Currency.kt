package reactive.enums

enum class Currency(private val inDollars: Double) {
    RUBLE(74.0),
    DOLLAR(1.0),
    EURO(0.85);

    fun convertToPreferredCurrency(price: Double, to: Currency): Double = price * this.inDollars / to.inDollars
}