package repository

import event.Event
import requests.ReportRequest
import java.util.ArrayList
import java.util.HashMap
import java.util.function.Consumer

class InMemoryEventStore: EventStore {
    private val events = HashMap<Int, MutableList<Event>>()
    private val subscribers = ArrayList<ReportStore>()

    override fun addEvent(id: Int, event: Event) {
        if(events[id] == null) {
            events[id] = ArrayList()
        }
        events[id]?.add(event)
        subscribers.forEach { it.handle(id, event) }
    }

    fun subscribe(subscriber: ReportStore) {
        events.forEach { (id, events) ->
            events.forEach{ subscriber.handle(id, it) }
        }
        subscribers.add(subscriber)
    }

    override fun getEventsById(id: Int): List<Event> = this.events[id] ?: emptyList()

    override fun getEvents(): Map<Int, List<Event>> = events

    override fun has(id: Int): Boolean = getEventsById(id).isNotEmpty()
}