package guru.zoroark.shedinja.extensions.services

import guru.zoroark.shedinja.dsl.put
import guru.zoroark.shedinja.dsl.shedinja
import guru.zoroark.shedinja.environment.Identifier
import guru.zoroark.shedinja.environment.get
import guru.zoroark.shedinja.environment.named
import guru.zoroark.shedinja.extensions.with
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FullTestServices {

    enum class Status {
        Initialized,
        Started,
        Stopped
    }

    class SimpleService : ShedinjaService {
        val status: Status
            get() = _started

        private var _started: Status = Status.Initialized

        override fun start() {
            _started = Status.Started
        }

        override fun stop() {
            _started = Status.Stopped
        }
    }

    class SuspendingService : SuspendShedinjaService {
        val status: Status
            get() = _started

        private var _started: Status = Status.Initialized

        override suspend fun start() {
            _started = Status.Started
        }

        override suspend fun stop() {
            _started = Status.Stopped
        }
    }

    class DelayStartStopService(private val delayMillis: Long) : SuspendShedinjaService {
        val status: Status
            get() = _started

        private var _started: Status = Status.Initialized

        override suspend fun start() {
            delay(delayMillis)
            _started = Status.Started
        }

        override suspend fun stop() {
            delay(delayMillis)
            _started = Status.Stopped
        }
    }

    @Test
    fun `Test simple non-suspending service start stop`() {
        val env = shedinja {
            useServices()

            put(::SuspendingService)
        }
        val service = env.get<SuspendingService>()
        assertEquals(Status.Initialized, service.status)
        runBlocking { env.services.startAll() }
        assertEquals(Status.Started, service.status)
        runBlocking { env.services.stopAll() }
        assertEquals(Status.Stopped, service.status)
    }

    @Test
    fun `Test simple suspending service start stop`() {
        val env = shedinja {
            useServices()

            put(::SuspendingService)
        }
        val service = env.get<SuspendingService>()
        assertEquals(Status.Initialized, service.status)
        runBlocking { env.services.startAll() }
        assertEquals(Status.Started, service.status)
        runBlocking { env.services.stopAll() }
        assertEquals(Status.Stopped, service.status)
    }

    @Test
    fun `Starting and stopping done in parallel`() {
        val env = shedinja {
            useServices()

            repeat(5) {
                put(named("$it")) { DelayStartStopService(1000) }
            }
        }

        val services = List(5) {
            env.get<DelayStartStopService>(named("$it"))
        }
        services.forEach { assertEquals(Status.Initialized, it.status) }

        val startTime = runBlocking {
            measureTimeMillis { env.services.startAll() }
        }
        assertContains(1000L..1200L, startTime, "All services should have been started in ~1sec")
        services.forEach { assertEquals(Status.Started, it.status) }

        val stopTime = runBlocking {
            measureTimeMillis { env.services.stopAll() }
        }
        assertContains(1000L..1200L, stopTime, "All services should have been stopped in ~1sec")
        services.forEach { assertEquals(Status.Stopped, it.status) }
    }

    @Test
    fun `Component start and stop exclusion`() {
        val env = shedinja {
            useServices()

            put(::SimpleService)
            put(named("nope"), ::SimpleService) with noService

            put(::SuspendingService)
            put(named("nope"), ::SuspendingService) with noService
        }

        val service = env.get<SimpleService>()
        val suspendingService = env.get<SuspendingService>()
        val yeetService = env.get<SimpleService>(named("nope"))
        val suspendingYeetService = env.get<SuspendingService>(named("nope"))

        assertEquals(Status.Initialized, service.status)
        assertEquals(Status.Initialized, suspendingService.status)
        assertEquals(Status.Initialized, yeetService.status)
        assertEquals(Status.Initialized, suspendingYeetService.status)

        runBlocking { env.services.startAll() }

        assertEquals(Status.Started, service.status)
        assertEquals(Status.Initialized, yeetService.status)
        assertEquals(Status.Started, suspendingService.status)
        assertEquals(Status.Initialized, suspendingYeetService.status)

        runBlocking { env.services.stopAll() }

        assertEquals(Status.Stopped, service.status)
        assertEquals(Status.Initialized, yeetService.status)
        assertEquals(Status.Stopped, suspendingService.status)
        assertEquals(Status.Initialized, suspendingYeetService.status)
    }

    @Test
    fun `Component start exclusion`() {
        val env = shedinja {
            useServices()

            put(::SimpleService)
            put(named("nope"), ::SimpleService) with noServiceStart

            put(::SuspendingService)
            put(named("nope"), ::SuspendingService) with noServiceStart
        }

        val service = env.get<SimpleService>()
        val suspendingService = env.get<SuspendingService>()
        val yeetService = env.get<SimpleService>(named("nope"))
        val suspendingYeetService = env.get<SuspendingService>(named("nope"))

        assertEquals(Status.Initialized, service.status)
        assertEquals(Status.Initialized, suspendingService.status)
        assertEquals(Status.Initialized, yeetService.status)
        assertEquals(Status.Initialized, suspendingYeetService.status)

        runBlocking { env.services.startAll() }

        assertEquals(Status.Started, service.status)
        assertEquals(Status.Initialized, yeetService.status)
        assertEquals(Status.Started, suspendingService.status)
        assertEquals(Status.Initialized, suspendingYeetService.status)

        runBlocking { env.services.stopAll() }

        assertEquals(Status.Stopped, service.status)
        assertEquals(Status.Stopped, yeetService.status)
        assertEquals(Status.Stopped, suspendingService.status)
        assertEquals(Status.Stopped, suspendingYeetService.status)
    }

    @Test
    fun `Component stop exclusion`() {
        val env = shedinja {
            useServices()

            put(::SimpleService)
            put(named("nope"), ::SimpleService) with noServiceStop

            put(::SuspendingService)
            put(named("nope"), ::SuspendingService) with noServiceStop
        }

        val service = env.get<SimpleService>()
        val suspendingService = env.get<SuspendingService>()
        val yeetService = env.get<SimpleService>(named("nope"))
        val suspendingYeetService = env.get<SuspendingService>(named("nope"))

        assertEquals(Status.Initialized, service.status)
        assertEquals(Status.Initialized, suspendingService.status)
        assertEquals(Status.Initialized, yeetService.status)
        assertEquals(Status.Initialized, suspendingYeetService.status)

        runBlocking { env.services.startAll() }

        assertEquals(Status.Started, service.status)
        assertEquals(Status.Started, yeetService.status)
        assertEquals(Status.Started, suspendingService.status)
        assertEquals(Status.Started, suspendingYeetService.status)

        runBlocking { env.services.stopAll() }

        assertEquals(Status.Stopped, service.status)
        assertEquals(Status.Started, yeetService.status)
        assertEquals(Status.Stopped, suspendingService.status)
        assertEquals(Status.Started, suspendingYeetService.status)
    }

    @Test
    fun `Component starting time statistics are accurate`() {
        val times = (0L..3L).toList().map { it * 1000 }
        val env = shedinja {
            useServices()

            times.forEach { timeMillis ->
                put(named(timeMillis.toString())) { DelayStartStopService(timeMillis) }
            }
        }
        val startStats = runBlocking { env.services.startAll() }
        assertEquals(times.size, startStats.size)
        times.forEach { time ->
            assertContains(
                (time - 200)..(time + 200),
                startStats[Identifier(DelayStartStopService::class, named(time.toString()))]
            )
        }

        val stopStats = runBlocking { env.services.stopAll() }
        assertEquals(times.size, startStats.size)
        times.forEach { time ->
            assertContains(
                (time - 200)..(time + 200),
                stopStats[Identifier(DelayStartStopService::class, named(time.toString()))]
            )
        }
    }
}
