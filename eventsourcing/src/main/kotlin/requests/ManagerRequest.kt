package requests

import event.Event
import event.TicketPeriodEvent
import repository.EventStore
import requests.model.TicketInfo
import java.time.LocalDateTime

class ManagerRequest(
    private val storage: EventStore
) {
    fun getTicketInfo(id: Int): TicketInfo {
        val events = storage.getEventsById(id)

        val createEvent = events[0] as TicketPeriodEvent
        var end = createEvent.lengthInDays
        var lastAttendance: LocalDateTime? = null

        for (i in 1 until events.size) {
            if (events[i].type == Event.EventType.EXTEND) {
                val event = events[i] as TicketPeriodEvent
                end += event.lengthInDays
            } else if (events[i].type == Event.EventType.IN) {
                    lastAttendance = events[i].time
            }
        }

        return TicketInfo(id, createEvent.time, createEvent.time.plusDays(end), lastAttendance)
    }
}