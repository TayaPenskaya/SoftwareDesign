package ru.tpenskaya.itmo.sd.stock

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import ru.tpenskaya.itmo.sd.stock.entity.Company
import ru.tpenskaya.itmo.sd.stock.entity.User
import ru.tpenskaya.itmo.sd.stock.model.SharesWithPrice
import java.math.BigDecimal


@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class StockApplicationTests {

    @Autowired
    private lateinit var mvc: MockMvc

    private val mapper = ObjectMapper()

    @Test
    fun `should increase company shares price`() {
        val companyName = "Business Company"
        val initialSharesPrice = BigDecimal("10.00")

        val createCompanyResult = mvc.perform(
            postJson(
                "/createCompany",
                mapOf("companyName" to companyName, "sharesPrice" to initialSharesPrice.toPlainString())
            )
        ).andExpect(status().isOk).andReturn()

        val companyId =
            mapper.readValue(createCompanyResult.response.contentAsString, ResponseBody::class.java).company!!
                .let { mapper.readValue(it, Company::class.java) }.id

        val sharesAmount = 3
        mvc.perform(
            postJson("/issueShares", mapOf("companyId" to companyId, "amount" to sharesAmount))
        ).andExpect(status().isOk)

        mvc.perform(getJson("/getCompanySharesPrice", mapOf("companyId" to companyId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.price").value("10.00"))

        val percent = 15.0
        val expectedNewSharesPrice = "11.50"
        mvc.perform(
            getJson(
                "/updateCompanySharesPrice",
                mapOf("companyId" to companyId, "percent" to percent.toString())
            )
        )
            .andExpect(status().isOk)

        mvc.perform(getJson("/getCompanySharesPrice", mapOf("companyId" to companyId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(expectedNewSharesPrice))
    }

    @Test
    fun `user with insufficient funds can not buy shares`() {
        val companyName = "Expensive Company"
        val initialSharesPrice = BigDecimal("10.00")

        val createCompanyResult = mvc.perform(
            postJson(
                "/createCompany",
                mapOf("companyName" to companyName, "sharesPrice" to initialSharesPrice.toPlainString())
            )
        ).andExpect(status().isOk).andReturn()

        val companyId =
            mapper.readValue(createCompanyResult.response.contentAsString, ResponseBody::class.java).company!!
                .let { mapper.readValue(it, Company::class.java) }.id

        val login = "poor_man"
        val initialBalance = BigDecimal.ONE
        val createUserResult = mvc.perform(
            postJson(
                "/createUser",
                mapOf("login" to login, "initialBalance" to initialBalance.toPlainString())
            )
        ).andExpect(status().isOk).andReturn()
        val userId = mapper.readValue(createUserResult.response.contentAsString, ResponseBody::class.java).user!!
            .let { mapper.readValue(it, User::class.java) }.id

        mvc.perform(getJson("/getUserTotalBalance", mapOf("userId" to userId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("1.00"))

        mvc.perform(getJson("/buyShares", mapOf(
            "userId" to userId,
            "companyId" to companyId,
            "amount" to "1"
        )))
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists())

        mvc.perform(getJson("/getUserTotalBalance", mapOf("userId" to userId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("1.00"))

    }

    @Test
    fun `user is able to buy shares and sell shares`() {
        val companyName = "Company to buy shares"
        val initialSharesPrice = BigDecimal("10")

        val createCompanyResult = mvc.perform(
            postJson(
                "/createCompany",
                mapOf("companyName" to companyName, "sharesPrice" to initialSharesPrice.toPlainString())
            )
        ).andExpect(status().isOk).andReturn()

        val companyId =
            mapper.readValue(createCompanyResult.response.contentAsString, ResponseBody::class.java).company!!
                .let { mapper.readValue(it, Company::class.java) }.id

        val companySharesAmount = 6
        mvc.perform(
            postJson("/issueShares", mapOf("companyId" to companyId, "amount" to companySharesAmount))
        ).andExpect(status().isOk)

        val login = "rich_man"
        val initialBalance = BigDecimal(0)
        val createUserResult = mvc.perform(
            postJson(
                "/createUser",
                mapOf("login" to login, "initialBalance" to initialBalance.toPlainString())
            )
        ).andExpect(status().isOk).andReturn()
        val userId = mapper.readValue(createUserResult.response.contentAsString, ResponseBody::class.java).user!!
            .let { mapper.readValue(it, User::class.java) }.id

        mvc.perform(getJson("/getUserTotalBalance", mapOf("userId" to userId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("0.00"))

        mvc.perform(postJson("/increaseUserBalance", mapOf("userId" to userId, "valueToIncrease" to "100"))
        ).andExpect(status().isOk)

        mvc.perform(getJson("/getUserTotalBalance", mapOf("userId" to userId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value("100.00"))

        mvc.perform(getJson("/buyShares", mapOf(
            "userId" to userId,
            "companyId" to companyId,
            "amount" to "5"
        ))).andExpect(status().isOk)

        var userSharesResult = mvc.perform(getJson("/getUserShares", mapOf("userId" to userId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(5))
            .andReturn()

        var userShares = mapper.readValue(userSharesResult.response.contentAsString, ResponseBody::class.java).shares!!
            .let { mapper.readValue(it, Array<SharesWithPrice>::class.java) }

        assertEquals(5, userShares.size)
        for (userShare in userShares) {
            assertEquals(userId, userShare.ownerId)
            assertEquals(companyId, userShare.companyId)
        }
        mvc.perform(getJson("/sellShares", mapOf(
            "userId" to userId,
            "companyId" to companyId,
            "amount" to "3"
        ))).andExpect(status().isOk)

        userSharesResult = mvc.perform(getJson("/getUserShares", mapOf("userId" to userId)))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(2))
            .andReturn()

        userShares = mapper.readValue(userSharesResult.response.contentAsString, ResponseBody::class.java).shares!!
            .let { mapper.readValue(it, Array<SharesWithPrice>::class.java) }

        assertEquals(2, userShares.size)

    }

    fun postJson(uri: String, body: Any): MockHttpServletRequestBuilder {
        val json = ObjectMapper().writeValueAsString(body)
        return MockMvcRequestBuilders
            .post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(json)
            .accept(MediaType.APPLICATION_JSON_VALUE)
    }

    fun getJson(uri: String, params: Map<String, String>): MockHttpServletRequestBuilder {
        val queryParams = params.takeIf { it.isNotEmpty() }
            ?.entries?.joinToString("&") { (k, v) -> "$k=$v" }
            ?.let { "?$it" }

        return MockMvcRequestBuilders
            .get(uri + queryParams)
            .accept(MediaType.APPLICATION_JSON_VALUE)
    }

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:11.6")

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgreSQLContainer.jdbcUrl,
                    "spring.datasource.username=" + postgreSQLContainer.username,
                    "spring.datasource.password=" + postgreSQLContainer.password
                )
            }
        }
    }

    data class ResponseBody(
        var company: String? = null,
        val user: String? = null,
        val amount: Int? = null,
        val shares: String? = null
    )


}
