package ru.kpfu.itis.efremov.schemarisk.analysis.graph;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.GraphEdgeDto;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.GraphNodeDto;
import ru.kpfu.itis.efremov.schemarisk.analysis.graph.dto.UsageGraphResponse;
import ru.kpfu.itis.efremov.schemarisk.analysis.impact.ImpactResult;
import ru.kpfu.itis.efremov.schemarisk.common.port.ServiceUsageRepository;
import ru.kpfu.itis.efremov.schemarisk.usage.model.ServiceUsageInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UsageGraphService {

    private static final String SCHEMA_NODE_TYPE = "SCHEMA";
    private static final String SERVICE_NODE_TYPE = "SERVICE";

    private final ServiceUsageRepository serviceUsageRepository;

    public UsageGraphService(ServiceUsageRepository serviceUsageRepository) {
        this.serviceUsageRepository = serviceUsageRepository;
    }

    public UsageGraphResponse buildGraph(String subject) {
        return buildGraph(subject, null);
    }

    public UsageGraphResponse buildGraph(String subject, ImpactResult impact) {
        List<ServiceUsageInfo> usages = serviceUsageRepository.getUsageBySubject(subject);

        Map<String, GraphNodeDto> nodes = new LinkedHashMap<>();
        List<GraphEdgeDto> edges = new ArrayList<>();
        boolean breaking = impact != null && impact.breaking();
        Set<String> criticalServices = impact != null
                ? new HashSet<>(impact.criticalServices())
                : Set.of();

        String schemaId = "schema:" + subject;
        nodes.put(schemaId, new GraphNodeDto(schemaId, SCHEMA_NODE_TYPE, subject, null, false));

        for (ServiceUsageInfo usage : usages) {
            String serviceId = "service:" + usage.serviceName();
            boolean critical = criticalServices.contains(usage.serviceName());

            nodes.putIfAbsent(serviceId, new GraphNodeDto(
                    serviceId,
                    SERVICE_NODE_TYPE,
                    usage.serviceName(),
                    breaking ? "BREAKING" : "SAFE",
                    critical
            ));

            edges.add(new GraphEdgeDto(
                    schemaId,
                    serviceId,
                    usage.role().name()
            ));
        }

        return new UsageGraphResponse(new ArrayList<>(nodes.values()), edges);
    }
}
