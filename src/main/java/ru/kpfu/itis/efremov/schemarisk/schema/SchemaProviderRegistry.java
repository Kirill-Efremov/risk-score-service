package ru.kpfu.itis.efremov.schemarisk.schema;

import org.springframework.stereotype.Component;
import ru.kpfu.itis.efremov.schemarisk.model.SchemaType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SchemaProviderRegistry {

    private final Map<SchemaType, SchemaProvider> providers = new EnumMap<>(SchemaType.class);

    public SchemaProviderRegistry(List<SchemaProvider> providerList) {
        for (SchemaProvider provider : providerList) {
            providers.put(provider.getSchemaType(), provider);
        }
    }

    public SchemaProvider getProvider(SchemaType type) {
        SchemaProvider provider = providers.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("No SchemaProvider registered for type: " + type);
        }
        return provider;
    }
}
