package dev.sentinel.assist;

import dev.sentinel.matching.IncidentMatch;
import dev.sentinel.resolution.ResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssistService {

    private final LlmClient llmClient;
    private final ResolutionService resolutionService;

    public Optional<String> suggestRootCause(String newIncidentTitle, String newIncidentDescription,
                                             List<IncidentMatch> matches) {
        if (matches.isEmpty()) {
            return Optional.empty();  // nothing to ground a suggestion in — don't guess
        }

        String context = buildGroundedContext(matches);
        String prompt = """
                A new incident occurred: "%s" — %s

                Here are similar past incidents and how they were resolved:
                %s

                In 2-3 sentences, suggest the most likely root cause and the most
                promising next step, based ONLY on the historical resolutions above.
                If the resolutions don't clearly point to a cause, say so plainly
                rather than speculating.
                """.formatted(newIncidentTitle, newIncidentDescription, context);

        return llmClient.complete(prompt);
    }

    private String buildGroundedContext(List<IncidentMatch> matches) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (IncidentMatch match : matches) {
            var resolutions = resolutionService.findForIncident(match.incident().getId());
            sb.append(i++).append(". \"").append(match.incident().getTitle())
                    .append("\" (similarity: ").append(String.format("%.2f", match.combinedScore())).append(")\n");
            if (resolutions.isEmpty()) {
                sb.append("   No recorded resolution.\n");
            } else {
                for (var r : resolutions) {
                    sb.append("   Resolved by: ").append(r.getSummary())
                            .append(" (").append(r.isSuccessful() ? "worked" : "did not work").append(")\n");
                }
            }
        }
        return sb.toString();
    }
}