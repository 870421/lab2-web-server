package es.unizar.webeng.lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ErrorPageTest {
    // Port used by the real test server.
    @LocalServerPort
    private var port: Int = 0

    // HTTP client used to call the running server.
    @Autowired
    private lateinit var client: TestRestTemplate

    @Test
    fun unknownPathRendersErrorHtml() {
        val headers = HttpHeaders()

        // Request an HTML response.
        headers.accept = listOf(MediaType.TEXT_HTML)

        // Call a path that does not exist.
        val response =
            client.exchange(
                "http://127.0.0.1:$port/missing",
                HttpMethod.GET,
                HttpEntity<Void>(headers),
                String::class.java,
            )

        // Check that the server returns 404.
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

        val body = response.body!!

        // Check that our custom error page is rendered.
        assertTrue(body.contains("<h1>Something went wrong</h1>"))

        // Check the dynamic error information.
        assertTrue(body.contains("<dd id=\"status\">404</dd>"))
        assertTrue(body.contains("<dd id=\"path\">/missing</dd>"))
    }
}
