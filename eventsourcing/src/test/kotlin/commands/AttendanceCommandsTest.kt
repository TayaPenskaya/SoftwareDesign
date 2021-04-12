package commands

import BaseTest
import event.Event
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class AttendanceCommandsTest : BaseTest() {
    private lateinit var attendanceCommands: AttendanceCommands

    @BeforeEach
    fun initExtra() {
        init()
        attendanceCommands = AttendanceCommands(eventStore, clock)
    }

    @Test
    fun enterTest() {
        createTicket()
        attendanceCommands.enter(1)
        val event = eventStore.getEventsById(1)[1]
        Assertions.assertEquals(Event.EventType.IN, event.type)
    }

    @Test
    fun exitTest() {
        createTicket()
        attendanceCommands.enter(1)
        val event1 = eventStore.getEventsById(1)[1]
        Assertions.assertEquals(Event.EventType.IN, event1.type)
        clock.setNow(Instant.now().plusSeconds(3600L))
        attendanceCommands.leave(1)
        val event2 = eventStore.getEventsById(1)[2]
        Assertions.assertEquals(Event.EventType.OUT, event2.type)
    }
}