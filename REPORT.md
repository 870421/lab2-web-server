# Lab 2 Web Server -- Project Report

## What I specified

For Task 1, I wanted to replace the default Spring Boot Whitelabel error page with a custom HTML page. The page had to be shown when the client requested HTML and the requested path did not exist. I also decided to show the error status and the requested path as the step further. I would know it worked if an unknown path returned HTTP 404 and showed my custom page with the correct status and path.

For Task 2, I wanted to add a `GET /time` endpoint that returned the current server time as JSON. As the step further, I decided to use a fixed `TimeProvider` in a test so the returned time could be checked with an exact value. I would know it worked if `/time` returned HTTP 200, JSON content, and a `time` field.

For Task 3, I wanted to run the application over HTTPS on port 8443 and enable HTTP/2 over TLS. I also decided to add both `localhost` and `127.0.0.1` to the certificate and use the `localhost` key alias as the step further. I would know it worked if curl negotiated `h2`, `/time` returned HTTP/2 200, the error page returned HTTP/2 404, and the certificate was valid for `127.0.0.1` when using the local certificate as a trusted CA.

## What I changed

For Task 1, I added `src/main/resources/templates/error.html` to replace the default Whitelabel error page with a custom Thymeleaf page. The page shows a generic error message and also displays the HTTP status and requested path. I also added `ErrorPageTest.kt`, which starts a real server on a random port and uses `TestRestTemplate` to request an unknown path and check the 404 response, the custom HTML, the status, and the path.

I also added `src/test/resources/application.yml` with SSL disabled. It was created during Task 1 so that the tests could continue using plain HTTP after TLS was enabled later in Task 3.

For Task 2, I added `TimeComponent.kt`. It contains the `TimeDTO`, the `TimeProvider` interface, the `TimeService` implementation, the `toDTO()` conversion function, and the `TimeController` for `GET /time`. I also added `TimeControllerTest.kt` to check that `/time` returns HTTP 200, JSON content, and a `time` field. For the step further, I added `TimeControllerFixedClockTest.kt`, which replaces the real `TimeProvider` with a fixed one and checks an exact timestamp.

For Task 3, I added `openssl-localhost.cnf` to define the local certificate settings, including `localhost` and `127.0.0.1` in the Subject Alternative Name. I generated `localhost.crt` and `localhost.key` locally and created `src/main/resources/localhost.p12` as the PKCS12 keystore used by Spring Boot.

I also created `src/main/resources/application.yml` to configure port 8443, enable SSL, use the PKCS12 keystore, and enable HTTP/2. For the step further, I later added `key-alias: localhost` to this configuration.

## Technical decisions

For the error page, I used one generic `error.html` template instead of a page only for 404 errors. Because the same template can be used for different server errors, I used the generic title `Something went wrong` and displayed the real status and requested path dynamically.

For `/time`, I kept the `TimeProvider` interface separated from `TimeService`. This makes the controller depend on an abstraction and allows the real clock to be replaced by a fixed provider in tests. For the step further, I chose the fixed provider test instead of adding time-zone support because it made the endpoint easier to test with an exact and predictable value.

For testing, I used `TestRestTemplate` with a real server for the custom error page because the error template must be rendered by the running application. For `/time`, I used `MockMvc` because it was enough to test the controller response without opening a real network connection.

For TLS, I used a self-signed certificate because the server is only used locally for the lab. I generated it with `-days 365` so it was valid for one year; without this option, my OpenSSL installation generated a certificate valid for only 30 days. Tomcat terminates TLS directly in the Spring Boot application, without a reverse proxy. The certificate includes both `localhost` and `127.0.0.1` in the Subject Alternative Name so it can be checked with either address. I also used the `localhost` key alias from the PKCS12 keystore.

Automated tests keep SSL disabled using the test configuration. HTTP/2 was verified manually with curl, following the verification steps in the guide.

## How I verified

I first ran:

```bash
./gradlew check
```

The first run failed because of a ktlint formatting problem in `build.gradle.kts`. I fixed it with:

```bash
./gradlew ktlintFormat
```

After that, `./gradlew check` completed successfully.

For Task 1, I started the application with:

```bash
./gradlew bootRun
```

and requested an unknown path. I checked that the default Whitelabel page was replaced by my custom page and that it showed status `404` and the requested path.

For Task 2, I opened `/time` and checked that it returned JSON with a `time` field. The automated tests also checked the normal response and the fixed `TimeProvider` used for the step further.

For Task 3, I first tried the curl versions already installed on my system, but they did not support the `--http2` option. Without that option, the requests used HTTP/1.1.

As a temporary check, I used OpenSSL with ALPN and confirmed that the server accepted `h2`.

I also tried curl from WSL, but it could not connect correctly to the server running on Windows. I then installed a newer curl version with HTTP/2 support using:

```powershell
winget install cURL.cURL
```

Because the Windows curl in `System32` was still first in the PATH, I used the installed curl with its full path:

```powershell
$curl = "$env:LOCALAPPDATA\Microsoft\WinGet\Links\curl.exe"
```

The certificate and PKCS12 keystore were generated from Git Bash with:

```bash
openssl req -x509 -newkey rsa:2048 -nodes -sha256 -days 365 \
  -keyout localhost.key -out localhost.crt \
  -config openssl-localhost.cnf

openssl pkcs12 -export \
  -in localhost.crt -inkey localhost.key \
  -name localhost -out localhost.p12 \
  -passout pass:secret

mv localhost.p12 src/main/resources/
```

OpenSSL was not available in the PowerShell PATH, so for the later checks I used the version included with Git for Windows:

```powershell
$openssl = "C:\Program Files\Git\mingw64\bin\openssl.exe"
```

I verified `/time` with:

```powershell
& $curl -v --http2 -k -i https://127.0.0.1:8443/time
```

and checked that the output contained:

```text
ALPN: server accepted h2
HTTP/2 200
```

I also verified the custom error page with:

```powershell
& $curl -v --http2 -k -H "Accept: text/html" -i https://127.0.0.1:8443/missing
```

and checked that it returned:

```text
HTTP/2 404
```

together with the custom HTML page, status `404`, and path `/missing`.

For the TLS step further, I checked the certificate Subject Alternative Name with:

```powershell
& $openssl x509 -in localhost.crt -noout -ext subjectAltName
```

and confirmed that it contained both `localhost` and `127.0.0.1`.

I also checked the PKCS12 keystore with:

```powershell
& $openssl pkcs12 -in src/main/resources/localhost.p12 -passin pass:secret -nokeys
```

and confirmed that it used the `localhost` alias.

Finally, I verified the certificate without `-k` by trusting the local certificate explicitly:

```powershell
& $curl -v --http2 --cacert localhost.crt -i https://127.0.0.1:8443/time
```

The output confirmed that `127.0.0.1` matched the certificate, certificate verification returned `0`, HTTP/2 was negotiated, and `/time` returned HTTP/2 200.

Before finishing the lab, I also ran:

```bash
./gradlew check
./gradlew ktlintCheck
```

to check the tests and code formatting.

## AI disclosure

- **Tools / skills:** Claude Code (Claude Opus 5.5) in VS Code and ChatGPT.

- **Purpose:** Claude Code was used to help implement the three tasks, create tests and configuration files, and diagnose problems during the TLS and HTTP/2 setup. ChatGPT was used to review the implementation, explain the code and the lab requirements, improve simple English comments, check the verification results, and help write this report.

- **Representative prompts:** Examples include asking Claude Code to implement the custom error page and its test, implement the `/time` endpoint with a fixed `TimeProvider` test, configure TLS and HTTP/2 following the lab guide, and diagnose why the installed curl did not support HTTP/2. ChatGPT was asked to review the generated files, explain how each task worked, suggest simple English comments, and check the results of the manual tests.

- **Affected files/sections:** AI assistance affected `error.html`, `ErrorPageTest.kt`, `src/test/resources/application.yml`, `TimeComponent.kt`, `TimeControllerTest.kt`, `TimeControllerFixedClockTest.kt`, `openssl-localhost.cnf`, `src/main/resources/application.yml`, the generation of `localhost.crt`, `localhost.key` and `localhost.p12`, and this report.

- **Validation steps:** I ran `./gradlew check`, `./gradlew ktlintCheck` and manual browser tests. I also verified TLS and HTTP/2 with curl and OpenSSL, checking the HTTP status codes, ALPN negotiation, certificate Subject Alternative Name, PKCS12 alias, and certificate validation.

- **Citations:** No external code snippets were copied. The implementation was based on the provided lab guide.

- **Human-reviewed:** I reviewed the generated code and configuration, changed texts and comments, chose the fixed `TimeProvider` step further, restored the `key-alias` configuration when it was accidentally removed, executed the tests and verification commands myself, and checked the final HTTP, TLS, HTTP/2 and certificate results.
