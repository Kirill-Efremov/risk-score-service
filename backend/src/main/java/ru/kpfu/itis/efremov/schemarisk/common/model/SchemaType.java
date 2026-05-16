package ru.kpfu.itis.efremov.schemarisk.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип поддерживаемой схемы")
public enum SchemaType {
    AVRO,
    JSON,
    PROTOBUF
}




