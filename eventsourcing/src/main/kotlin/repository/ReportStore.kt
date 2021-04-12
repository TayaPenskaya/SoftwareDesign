package repository

import event.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.AbstractMap

class ReportStore {
    internal val dailyStatistics = HashMap<LocalDate, Map.Entry<Int, Long>>()
    internal var visits = 0
    internal var duration = 0L
    private val entrance = HashMap<Int, LocalDateTime>()

    fun handle(id: Int, event: Event) {
        if (event.type == Event.EventType.IN) {
            entrance[id] = event.time
        } else if (event.type == Event.EventType.OUT) {
            val time = event.time
            val date = time.toLocalDate()

            val p = dailyStatistics.getOrDefault(date, AbstractMap.SimpleEntry(0, 0L))
            val d = p.value + ChronoUnit.HOURS.between(entrance[id], time)

            dailyStatistics[date] = AbstractMap.SimpleEntry(p.key + 1, d)
            visits++
            duration += d
        }
    }
}