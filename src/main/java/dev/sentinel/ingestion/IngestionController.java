package dev.sentinel.ingestion;

import dev.sentinel.incident.Incident;
import dev.sentinel.incident.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class IngestionController {

    private final IncidentService incidentService;
    private final PrometheusAlertAdapter prometheusAdapter;

    @PostMapping("/prometheus")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void receivePrometheus(@RequestBody PrometheusAlertPayload payload) {
        RawAlertPayload normalized = prometheusAdapter.normalize(payload);
        recordFromRaw(normalized);
    }

    private void recordFromRaw(RawAlertPayload raw) {
        Incident incident = new Incident();
        incident.setExternalId(raw.externalId());
        incident.setTitle(raw.title());
        incident.setDescription(raw.description());
        incident.setServiceName(raw.serviceName());
        incident.setSeverity(raw.severity());
        incident.setOccurredAt(raw.occurredAt());
        incidentService.record(incident);
    }
}