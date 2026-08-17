package dev.sentinel.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SyntheticIncidentGenerator generator;

    @PostMapping("/generate-demo-data")
    public String generate(
            @RequestParam(defaultValue = "50") int count,
            @RequestParam(defaultValue = "30") int daysBack
    ) {
        generator.generateHistory(count, daysBack);
        return "Generated " + count + " synthetic incidents over the last " + daysBack + " days.";
    }
}