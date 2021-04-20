package ru.tpenskaya.itmo.sd.stock.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.web.bind.annotation.*
import ru.tpenskaya.itmo.sd.stock.exception.ServiceException
import ru.tpenskaya.itmo.sd.stock.service.StockService
import ru.tpenskaya.itmo.sd.stock.service.UserService
import ru.tpenskaya.itmo.sd.stock.util.Body
import ru.tpenskaya.itmo.sd.stock.util.Json
import java.math.BigDecimal

@RestController
class UserController(private val userService: UserService, private val stockService: StockService) {

    private val mapper = ObjectMapper()
    @PostMapping("/createUser", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createUser(@RequestBody body: String): ResponseEntity<Json> {
        val bodyObj = mapper.readValue(body, Body::class.java)
        val login = bodyObj.login!!
        val initialBalance = bodyObj.initialBalance ?: "0"
        val newUser = userService.registerNewUser(login, BigDecimal(initialBalance))
        return ResponseEntity.ok(mapOf("user" to mapper.writeValueAsString(newUser)))
    }

    @PostMapping("/increaseUserBalance", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional(propagation = Propagation.REQUIRED)
    fun increaseUserBalance(@RequestBody body: String) {
        val bodyObj = mapper.readValue(body, Body::class.java)
        val userId = bodyObj.userId!!
        val valueToIncrease = bodyObj.valueToIncrease ?: "0"
        userService.increaseUserBalance(userId, BigDecimal(valueToIncrease))
    }

    @GetMapping("/getUserShares", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserShares(@RequestParam("userId") userId: String): ResponseEntity<Json> =
        try {
            val shares = userService.getUserShares(userId)
            ResponseEntity.ok(mapOf("amount" to shares.size.toString(), "shares" to mapper.writeValueAsString(shares)))
        } catch (e: ServiceException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }

    @GetMapping("/getUserTotalBalance", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserTotalBalance(@RequestParam("userId") userId: String): ResponseEntity<Json> =
        try {
            val balance = userService.getUserBalance(userId)
            val shares = userService.getUserShares(userId)
            val totalBalance = shares.fold(balance) { l, r ->
                l.plus(r.price)
            }
            ResponseEntity.ok(mapOf("balance" to totalBalance.toPlainString()))
        } catch (e: ServiceException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }

    @GetMapping("/buyShares", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional(propagation = Propagation.REQUIRED)
    fun buyShares(
        @RequestParam("userId") userId: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("amount") amount: String
    ): ResponseEntity<Json> =
        try {
            val price = stockService.getCompanySharesPrice(companyId).multiply(BigDecimal(amount))
            userService.decreaseUserBalance(userId, price)
            stockService.buyCompanyShares(userId, companyId, amount.toInt())
            ResponseEntity.ok().build()
        } catch (e: ServiceException) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }

    @GetMapping("/sellShares", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional(propagation = Propagation.REQUIRED)
    fun sellShares(
        @RequestParam("userId") userId: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("amount") amount: String
    ): ResponseEntity<Json> =
        try {
            val price = stockService.getCompanySharesPrice(companyId).multiply(BigDecimal(amount))
            userService.increaseUserBalance(userId, price)
            stockService.sellCompanyShares(userId, companyId, amount.toInt())
            ResponseEntity.ok().build()
        } catch (e: ServiceException) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
}
