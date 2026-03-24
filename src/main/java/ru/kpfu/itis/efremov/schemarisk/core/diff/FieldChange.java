package ru.kpfu.itis.efremov.schemarisk.core.diff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.kpfu.itis.efremov.schemarisk.model.IssueSeverity;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {

    @JsonProperty("type")
    private FieldChangeType changeType;
    private String fieldName;
    private String oldType;
    private String newType;
    private String oldDefault;
    private String newDefault;
    @JsonIgnore
    private IssueSeverity severity;
    @JsonIgnore
    private boolean breaking;

    @JsonIgnore
    public FieldChangeType getType() {
        return changeType;
    }

    @JsonIgnore
    public void setType(FieldChangeType type) {
        this.changeType = type;
    }
}

