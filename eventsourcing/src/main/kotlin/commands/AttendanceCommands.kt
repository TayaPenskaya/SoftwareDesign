package commands

import event.Event
import event.TicketPeriodEvent
import event.TicketVisitEvent
import repository.EventStore
import java.time.Clock
import java.time.LocalDateTime

class AttendanceCommands(
    private val storage: EventStore,
    private val clock: Clock
    ) {

    fun enter(id: Int) {
        val events = storage.getEventsById(id)
        val createEvent = events[0] as TicketPeriodEvent
        var end = createEvent.lengthInDays
        var canEnter = true

        for (i in 1 until events.size) {
            if (events[i].type == Event.EventType.EXTEND) {
                val event = events[i] as TicketPeriodEvent
                end += event.lengthInDays
            }
            if (events[i].type == Event.EventType.IN) {
                canEnter = false
            }
            if (events[i].type == Event.EventType.OUT) {
                canEnter = true
            }
        }

        if (!canEnter || !createEvent.time.plusDays(end).isAfter(LocalDateTime.ofInstant(clock.instant(), clock.zone))) {
            return
        }

        storage.addEvent(id,
            TicketVisitEvent(id, LocalDateTime.ofInstant(clock.instant(), clock.zone), true))
    }

    fun leave(id: Int) {
        val events = storage.getEventsById(id)
        var canLeave = false
        for (i in 1 until events.size) {
            if (events[i].type == Event.EventType.IN) {
                canLeave = true
            }
            if (events[i].type == Event.EventType.OUT) {
                canLeave = false
            }
        }
        if (!canLeave) {
            return
        }
        storage.addEvent(id,
            TicketVisitEvent(id, LocalDateTime.ofInstant(clock.instant(), clock.zone), false))
    }
}