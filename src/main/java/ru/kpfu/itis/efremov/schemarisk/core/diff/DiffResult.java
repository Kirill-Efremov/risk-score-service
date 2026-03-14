package ru.kpfu.itis.efremov.schemarisk.core.diff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffResult {

    private String schemaName;
    private List<FieldChange> changes;
}
