package commands

import event.TicketPeriodEvent
import repository.EventStore
import java.time.Clock
import java.time.LocalDateTime

class ManagerCommands(
    private val storage: EventStore,
    private val clock: Clock) {

    fun createTicket(id: Int, days: Long) {
        if (!storage.has(id)) {
            storage.addEvent(id,
                TicketPeriodEvent(id, LocalDateTime.ofInstant(clock.instant(), clock.zone), days, false))
        }
    }

    fun renewPeriod(id: Int, days: Long) {
        if (storage.has(id)) {
            storage.addEvent(id,
                TicketPeriodEvent(id, LocalDateTime.ofInstant(clock.instant(), clock.zone), days, true))
        }
    }

}