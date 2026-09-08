package xyz.erupt.cloud.common.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Shared Spring RestClient setup for cloud server <-> node calls.
 *
 * @author YuePeng
 * date 2026/9/8
 */
public class CloudHttp {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * @param readTimeout max wait for the response headers; null means unbounded
     */
    public static RestClient client(Duration readTimeout) {
        // HTTP/1.1 only: nodes are plain Tomcat, no h2c upgrade dance
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1).connectTimeout(CONNECT_TIMEOUT).build());
        if (null != readTimeout) factory.setReadTimeout(readTimeout);
        return RestClient.builder().requestFactory(factory).build();
    }

    // Execute and return status + body without throwing on 4xx/5xx; the caller decides what an error means
    public static ResponseEntity<String> exchange(RestClient.RequestHeadersSpec<?> spec) {
        return spec.retrieve().onStatus(status -> true, (request, response) -> {
        }).toEntity(String.class);
    }

    // Header setter that skips null values: a missing token must not turn into a literal "null" header
    public static Consumer<HttpHeaders> header(String name, String value) {
        return headers -> {
            if (null != value) headers.set(name, value);
        };
    }

    public static String base64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }

}
