package requests.model

import java.time.LocalDateTime

data class TicketInfo(
    val id: Int,
    val created: LocalDateTime,
    val end: LocalDateTime,
    val lastAttendance: LocalDateTime?
)