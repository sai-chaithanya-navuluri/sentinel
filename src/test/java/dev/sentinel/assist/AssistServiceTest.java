package dev.sentinel.assist;

import dev.sentinel.incident.Incident;
import dev.sentinel.incident.Severity;
import dev.sentinel.matching.IncidentMatch;
import dev.sentinel.resolution.ResolutionService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AssistServiceTest {

    private Incident sampleIncident(long id, String title) {
        Incident i = new Incident();
        i.setId(id);
        i.setTitle(title);
        i.setServiceName("payment-service");
        i.setSeverity(Severity.CRITICAL);
        i.setOccurredAt(Instant.now());
        return i;
    }

    @Test
    void refuses_to_call_llm_when_no_matches_exist() {
        LlmClient llmClient = mock(LlmClient.class);
        ResolutionService resolutionService = mock(ResolutionService.class);
        AssistService service = new AssistService(llmClient, resolutionService);

        Optional<String> result = service.suggestRootCause("title", "desc", List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(llmClient);
    }

    @Test
    void calls_llm_with_grounded_context_when_matches_exist() {
        LlmClient llmClient = mock(LlmClient.class);
        ResolutionService resolutionService = mock(ResolutionService.class);
        when(resolutionService.findForIncident(anyLong())).thenReturn(List.of());
        when(llmClient.complete(anyString())).thenReturn(Optional.of("a grounded suggestion"));

        AssistService service = new AssistService(llmClient, resolutionService);
        Incident match = sampleIncident(1L, "DB connection timeout");
        IncidentMatch im = new IncidentMatch(match, 0.9, 0.95, 0.93);

        Optional<String> result = service.suggestRootCause("new title", "new desc", List.of(im));

        assertThat(result).contains("a grounded suggestion");
        verify(llmClient).complete(contains("DB connection timeout"));
    }
}