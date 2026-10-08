package es.unizar.webeng.lab2

import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Data returned by the /time endpoint.
 */
data class TimeDTO(
    val time: LocalDateTime,
)

/**
 * Provides the current date and time.
 */
interface TimeProvider {
    fun now(): LocalDateTime
}

/**
 * Gets the current time from the system clock.
 */
@Service
class TimeService : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

/**
 * Converts a LocalDateTime into a TimeDTO.
 */
fun LocalDateTime.toDTO(): TimeDTO = TimeDTO(time = this)

/**
 * Controller for the /time endpoint.
 */
@RestController
class TimeController(
    private val service: TimeProvider,
) {
    /**
     * Returns the current server time.
     */
    @GetMapping("/time")
    fun time(): TimeDTO = service.now().toDTO()
}
