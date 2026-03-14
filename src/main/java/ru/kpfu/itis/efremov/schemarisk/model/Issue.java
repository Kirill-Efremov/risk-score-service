package ru.kpfu.itis.efremov.schemarisk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    private String code;
    private String message;
    private IssueSeverity severity;
    private boolean breakingChange;
    private String path;
}
