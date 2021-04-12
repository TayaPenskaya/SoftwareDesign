package requests

import BaseTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ManagerRequestTest : BaseTest() {
    private lateinit var managerRequest: ManagerRequest

    @BeforeEach
    fun initExtra() {
        init()
        managerRequest = ManagerRequest(eventStore)
    }

    @Test
    fun createUserTest() {
        createTicket()
        val ticketInfo = managerRequest.getTicketInfo(1)
        Assertions.assertEquals(ticketInfo.created.plusDays(30), ticketInfo.end)
    }
}