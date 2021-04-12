package event

import java.time.LocalDateTime

abstract class Event(val ticketId: Int, val time: LocalDateTime) {
    lateinit var type: EventType

    enum class EventType {
        IN, OUT, NEW, EXTEND
    }
}