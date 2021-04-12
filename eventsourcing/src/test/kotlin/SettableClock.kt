import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SettableClock(time: LocalDateTime?) : java.time.Clock() {
    private var now: Instant

    init {
        now = time!!.atZone(ZoneId.systemDefault()).toInstant()
    }

    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): java.time.Clock {
        throw UnsupportedOperationException()
    }

    override fun instant() = now

    fun setNow(newNow: Instant) {
        now = newNow
    }
}