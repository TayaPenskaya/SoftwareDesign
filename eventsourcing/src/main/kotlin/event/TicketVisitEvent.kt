package event

import java.time.LocalDateTime

class TicketVisitEvent(ticketId: Int, time: LocalDateTime, enter: Boolean)
    : Event(ticketId, time) {

    init {
        type = if (enter) EventType.IN else EventType.OUT
    }
}