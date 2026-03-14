package ru.kpfu.itis.efremov.schemarisk.core.diff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {

    private FieldChangeType type;
    private String fieldName;
    private String oldType;
    private String newType;
    private String oldDefault;
    private String newDefault;
}

