package requests

import BaseTest
import commands.AttendanceCommands
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repository.ReportStore
import java.time.Instant

class ReportRequestTest : BaseTest() {
    private lateinit var reportStore: ReportStore
    private lateinit var reportRequest: ReportRequest
    private lateinit var attendanceCommands: AttendanceCommands

    @BeforeEach
    fun initExtra() {
        init()
        reportStore = ReportStore()
        reportRequest = ReportRequest(reportStore)
        attendanceCommands = AttendanceCommands(eventStore, clock)
    }

    @Test
    fun unsubscribedReportTest() {
        createTicket()
        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(3600L))
        attendanceCommands.leave(1)

        val statistics = reportRequest.dailyStatistics()
        Assertions.assertEquals(0, statistics.size)
    }

    @Test
    fun subscribedReportTest() {
        createTicket()
        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(3600L))
        attendanceCommands.leave(1)
        eventStore.subscribe(reportStore)

        val statistics = reportRequest.dailyStatistics()
        Assertions.assertEquals(1, statistics.size)
    }

    @Test
    fun meanFrequencyTest() {
        createTicket()
        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(3600L))
        attendanceCommands.leave(1)
        clock.setNow(Instant.now().plusSeconds(24 * 2* 3600L))

        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(6 * 3600L))
        attendanceCommands.leave(1)
        clock.setNow(Instant.now().plusSeconds(24 * 2* 3600L))
        eventStore.subscribe(reportStore)

        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(2 * 3600L))
        attendanceCommands.leave(1)


        val meanFrequency = reportRequest.meanFrequency()
        Assertions.assertEquals(3.0, meanFrequency)
    }

    @Test
    fun meanDurationTest() {
        createTicket()
        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(3600L))
        attendanceCommands.leave(1)
        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(6 * 3600L))
        attendanceCommands.leave(1)
        eventStore.subscribe(reportStore)
        attendanceCommands.enter(1)
        clock.setNow(Instant.now().plusSeconds(2 * 3600L))
        attendanceCommands.leave(1)


        val meanDuration = reportRequest.meanDuration()
        Assertions.assertEquals(3, meanDuration)
    }
}