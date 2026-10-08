package es.unizar.webeng.lab2

import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * JSON body returned by `GET /time`.
 *
 * @property time the server time, serialized as an ISO-8601 local date-time.
 */
data class TimeDTO(
    val time: LocalDateTime,
)

/**
 * Source of the current time. The controller depends on this interface so a test can replace the clock.
 */
interface TimeProvider {
    /** Returns the current date and time. */
    fun now(): LocalDateTime
}

/**
 * [TimeProvider] backed by the system clock in the server's default time zone.
 */
@Service
class TimeService : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

/** Wraps this date-time in the DTO returned by the API. */
fun LocalDateTime.toDTO(): TimeDTO = TimeDTO(time = this)

/**
 * Exposes the current server time over HTTP.
 */
@RestController
class TimeController(
    private val service: TimeProvider,
) {
    /** Handles `GET /time` and returns the time given by the [TimeProvider]. */
    @GetMapping("/time")
    fun time(): TimeDTO = service.now().toDTO()
}
