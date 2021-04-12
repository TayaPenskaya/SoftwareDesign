package repository

import event.Event

interface EventStore {
    fun addEvent(id: Int, event: Event)
    fun getEventsById(id: Int): List<Event>
    fun getEvents(): Map<Int, List<Event>>
    fun has(id: Int): Boolean
}