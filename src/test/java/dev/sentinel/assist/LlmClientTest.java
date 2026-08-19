package dev.sentinel.assist;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LlmClientTest {

    @Test
    void reports_unavailable_when_disabled() {
        LlmClient client = new LlmClient(new SimpleMeterRegistry());
        ReflectionTestUtils.setField(client, "enabled", false);
        ReflectionTestUtils.setField(client, "apiKey", "some-key");

        assertThat(client.isAvailable()).isFalse();
    }

    @Test
    void reports_unavailable_when_no_api_key_configured() {
        LlmClient client = new LlmClient(new SimpleMeterRegistry());
        ReflectionTestUtils.setField(client, "enabled", true);
        ReflectionTestUtils.setField(client, "apiKey", "");

        assertThat(client.isAvailable()).isFalse();
    }

    @Test
    void complete_returns_empty_immediately_when_unavailable_no_network_call_attempted() {
        LlmClient client = new LlmClient(new SimpleMeterRegistry());
        ReflectionTestUtils.setField(client, "enabled", false);
        ReflectionTestUtils.setField(client, "apiKey", "");

        Optional<String> result = client.complete("any prompt");

        assertThat(result).isEmpty();
    }
}