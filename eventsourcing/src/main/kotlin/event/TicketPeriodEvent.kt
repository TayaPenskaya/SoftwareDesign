package event

import java.time.LocalDateTime

class TicketPeriodEvent(ticketId: Int, time: LocalDateTime, var lengthInDays: Long, extend: Boolean)
    : Event(ticketId, time) {

    init {
        type = if (extend) EventType.EXTEND else EventType.NEW
    }
}