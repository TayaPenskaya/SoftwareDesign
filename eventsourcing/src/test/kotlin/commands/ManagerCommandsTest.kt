package commands

import BaseTest
import event.TicketPeriodEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

import java.time.LocalDateTime

class ManagerCommandsTest : BaseTest() {
    @Test
    fun createUserTest() {
        createTicket()
        val expectedEvent = TicketPeriodEvent(1, LocalDateTime.now(), 30, false)
        assertEquals(expectedEvent.time.toLocalDate(), eventStore.getEventsById(1)[0].time.toLocalDate())
    }

    @Test
    fun renewPeriodTest() {
        createTicket()
        managerCommands.renewPeriod(1,1)
        assertEquals(30, (eventStore.getEventsById(1)[0] as TicketPeriodEvent).lengthInDays)
        assertEquals(1, (eventStore.getEventsById(1)[1] as TicketPeriodEvent).lengthInDays)

    }
}