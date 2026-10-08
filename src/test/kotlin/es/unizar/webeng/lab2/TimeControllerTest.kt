package es.unizar.webeng.lab2

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class TimeControllerTest {
    // MockMvc lets us test the controller without a real HTTP connection.
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun timeIsJson() {
        // Request the current server time as JSON.
        mockMvc
            .perform(get("/time").accept(MediaType.APPLICATION_JSON))
            // Check that the request is successful.
            .andExpect(status().isOk)
            // Check that the response is JSON.
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            // Check that the JSON contains the time field.
            .andExpect(jsonPath("$.time").exists())
    }
}
