# URL Preview

A Spring Boot app that generates a title, description, and image preview for a public web page.

## Run

```bash
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) and submit a URL.

The JSON endpoint is `GET /api/preview?url=...`:

```bash
curl --get http://localhost:8080/api/preview \
  --data-urlencode 'url=https://example.com'
```

The app accepts public HTTP/HTTPS URLs on the standard ports. It revalidates redirect destinations, limits request time and response size, and returns a clear error for invalid or unavailable pages. Avoid submitting URLs containing secrets, since query strings may be recorded in server or proxy logs.

## Test

```bash
./mvnw test
```