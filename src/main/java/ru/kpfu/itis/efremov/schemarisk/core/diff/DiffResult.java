package ru.kpfu.itis.efremov.schemarisk.core.diff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffResult {

    private String schemaName;
    private List<FieldChange> changes;

    public List<FieldChange> getChanges() {
        return Objects.requireNonNullElse(changes, List.of());
    }
}
