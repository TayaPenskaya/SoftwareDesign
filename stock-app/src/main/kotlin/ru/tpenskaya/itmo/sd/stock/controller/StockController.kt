package ru.tpenskaya.itmo.sd.stock.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.tpenskaya.itmo.sd.stock.exception.ServiceException
import ru.tpenskaya.itmo.sd.stock.service.StockService
import ru.tpenskaya.itmo.sd.stock.util.Body
import ru.tpenskaya.itmo.sd.stock.util.Json
import java.math.BigDecimal

@RestController
class StockController(private val stockService: StockService) {

    private val mapper = ObjectMapper()

    @PostMapping("/createCompany", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createCompany(@RequestBody body: String): ResponseEntity<Json> {
        val bodyMap = mapper.readValue(body, Body::class.java)
        val companyName = bodyMap.companyName!!
        val sharesPrice = bodyMap.sharesPrice!!
        if (stockService.findCompanyByName(companyName) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "Company with name=$companyName already exists"))
        }
        val createdCompany = stockService.createNewCompany(companyName, BigDecimal(sharesPrice))

        return ResponseEntity.ok(mapOf("company" to mapper.writeValueAsString(createdCompany)))
    }

    @PostMapping("/issueShares", consumes = ["application/json"], produces = ["application/json"])
    fun issueShares(@RequestBody body: String): ResponseEntity<Json> {
        val bodyObj = mapper.readValue(body, Body::class.java)
        val amount = bodyObj.amount!!
        val companyId = bodyObj.companyId!!
        if (amount < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Invalid amount=$amount"))
        }
        return try {
            stockService.issueShares(companyId, amount)
            ResponseEntity.ok().build()
        } catch (e: ServiceException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/getCompanySharesPrice", produces = ["application/json"])
    fun getCompanySharesPrice(@RequestParam("companyId") companyId: String): ResponseEntity<Json> =
        try {
            val price = stockService.getCompanySharesPrice(companyId)
            ResponseEntity.ok(mapOf("price" to price.toPlainString()))
        } catch (e: ServiceException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }


    @GetMapping("/getCompanySharesAmount", produces = ["application/json"])
    fun getCompanySharesAmount(@RequestParam("companyId") companyId: String): ResponseEntity<Json> =
        try {
            val amount = stockService.countCompanySharesPrice(companyId)
            ResponseEntity.ok(mapOf("amount" to amount.toString()))
        } catch (e: ServiceException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }


    @GetMapping("/buyCompanyShares", produces = ["application/json"])
    fun buyCompanyShares(
        @RequestParam("buyerId") buyerId: String,
        @RequestParam("companyId") companyId: String,
        @RequestParam("amount") amount: Int
    ): ResponseEntity<Json> = try {
        stockService.buyCompanyShares(buyerId, companyId, amount)
        ResponseEntity.ok(mapOf())
    } catch (e: ServiceException) {
        ResponseEntity.badRequest().body(mapOf("error" to e.message))
    }

    @GetMapping("/updateCompanySharesPrice", produces = ["application/json"])
    fun updateCompanySharesPrice(
        @RequestParam("companyId") companyId: String,
        @RequestParam("percent") percent: Double
    ): ResponseEntity<Json> =
        try {
            stockService.updateCompanySharesPrice(companyId, percent)
            ResponseEntity.ok(mapOf())
        } catch (e: ServiceException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
}