package requests

import repository.ReportStore

class ReportRequest(private val store: ReportStore) {
    fun dailyStatistics() = store.dailyStatistics

    fun meanDuration() = store.duration / store.visits.toLong()

    fun meanFrequency(): Double {
        val days = store.dailyStatistics.keys.size
        var res = 0

        for (d in store.dailyStatistics.keys) {
            res += store.dailyStatistics[d]!!.key
        }

        return res.toDouble() / days.toDouble()
    }
}