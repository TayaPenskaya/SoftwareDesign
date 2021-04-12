import commands.ManagerCommands
import java.time.LocalDateTime

import org.junit.jupiter.api.BeforeEach
import repository.InMemoryEventStore

open class BaseTest {
    protected lateinit var clock: SettableClock
    protected lateinit var eventStore: InMemoryEventStore
    protected lateinit var managerCommands: ManagerCommands

    @BeforeEach
    open fun init() {
        clock = SettableClock(LocalDateTime.now())
        eventStore = InMemoryEventStore()
        managerCommands = ManagerCommands(eventStore, clock)
    }

    fun createTicket() {
        managerCommands.createTicket(1,30)
    }
}