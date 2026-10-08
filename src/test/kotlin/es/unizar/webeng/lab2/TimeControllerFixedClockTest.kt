package es.unizar.webeng.lab2

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class TimeControllerFixedClockTest {
    @TestConfiguration
    class FixedClockConfiguration {
        @Bean
        @Primary
        fun fixedTimeProvider(): TimeProvider =
            object : TimeProvider {
                override fun now(): LocalDateTime = FIXED_TIME
            }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun timeIsTheOneGivenByTheProvider() {
        mockMvc
            .perform(get("/time").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.time").value("2026-10-07T18:30:15"))
    }

    companion object {
        private val FIXED_TIME: LocalDateTime = LocalDateTime.of(2026, 10, 7, 18, 30, 15)
    }
}
