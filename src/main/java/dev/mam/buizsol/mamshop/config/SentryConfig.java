package dev.mam.buizsol.mamshop.config;

import io.sentry.Hint;
import io.sentry.ITransportFactory;
import io.sentry.RequestDetails;
import io.sentry.SentryEnvelope;
import io.sentry.SentryEnvelopeItem;
import io.sentry.SentryOptions;
import io.sentry.transport.ITransport;
import io.sentry.transport.RateLimiter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SentryConfig {

    @Bean
    public ITransportFactory sentryTransportFactory() {
        return (options, requestDetails) -> new LegacySentryTransport(options, requestDetails);
    }

    private static class LegacySentryTransport implements ITransport {
        private final SentryOptions options;
        private final RequestDetails requestDetails;

        public LegacySentryTransport(SentryOptions options, RequestDetails requestDetails) {
            this.options = options;
            this.requestDetails = requestDetails;
        }

        @Override
        public RateLimiter getRateLimiter() {
            return null;
        }

        @Override
        public void send(SentryEnvelope envelope, Hint hint) {
            try {
                URL url = requestDetails.getUrl();
                String urlString =
                        url.toString().replace("/envelope/", "/store/").replace("/envelope", "/store/");

                for (SentryEnvelopeItem item : envelope.getItems()) {
                    try {
                        byte[] data = item.getData();
                        if (data != null && data.length > 0) {
                            sendJson(urlString, requestDetails.getHeaders(), data);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to send Sentry item: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send Sentry envelope", e);
            }
        }

        private void sendJson(String urlString, Map<String, String> headers, byte[] data) {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URI(urlString).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (headers != null) {
                    headers.forEach(conn::setRequestProperty);
                }
                conn.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data);
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    log.info("Successfully sent event to Sentry server, response: {}", code);
                } else {
                    log.warn("Sentry server returned HTTP code: {} for URL: {}", code, urlString);
                }
            } catch (Exception e) {
                log.warn("Failed to POST Sentry event to {}: {}", urlString, e.getMessage());
            }
        }

        @Override
        public void flush(long timeoutMillis) {}

        @Override
        public void close(boolean isRestarting) {}

        @Override
        public void close() {
            close(false);
        }
    }
}
