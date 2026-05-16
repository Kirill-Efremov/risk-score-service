import { useMemo } from "react";
import ReactFlow, {
  Background,
  Controls,
  Handle,
  MarkerType,
  Position,
  type Edge,
  type Node,
  type NodeProps,
} from "reactflow";
import type { ImpactGraph } from "../../types/graph";

const nodeTypes = {
  impactNode: ImpactNode,
};

export function ImpactGraphView({ graph }: { graph?: ImpactGraph | null }) {
  const { nodes, edges } = useMemo(() => {
    const source = graph ?? { nodes: [], edges: [] };
    const schemaNode = source.nodes.find((node) => node.type === "SCHEMA");
    const serviceMap = new Map(source.nodes.map((node) => [node.id, node]));

    const producers = uniqueNodes(
      source.edges
        .filter((edge) => edge.type === "PRODUCER")
        .map((edge) => inferServiceNode(edge, serviceMap)),
    );
    const consumers = uniqueNodes(
      source.edges
        .filter((edge) => edge.type === "CONSUMER")
        .map((edge) => inferServiceNode(edge, serviceMap)),
    );

    const flowNodes: Node[] = [];
    if (schemaNode) {
      flowNodes.push(toNode(schemaNode, { x: 470, y: 190 }));
    }

    const producerStartY = centerStartY(producers.length);
    producers.forEach((node, index) => {
      flowNodes.push(toNode(node, { x: 60, y: producerStartY + index * 140 }));
    });

    const consumerStartY = centerStartY(consumers.length);
    consumers.forEach((node, index) => {
      flowNodes.push(toNode(node, { x: 900, y: consumerStartY + index * 140 }));
    });

    const flowEdges: Edge[] = [];
    producers.forEach((node, index) => {
      if (!schemaNode) return;
      flowEdges.push({
        id: `producer-${node.id}-${index}`,
        source: schemaNode.id,
        sourceHandle: "source-left",
        target: node.id,
        targetHandle: "target-right",
        label: "PRODUCER",
        markerEnd: { type: MarkerType.ArrowClosed },
        style: { stroke: "#0f766e", strokeWidth: 1.6 },
        labelStyle: { fill: "#475569", fontWeight: 600 },
      });
    });
    consumers.forEach((node, index) => {
      if (!schemaNode) return;
      flowEdges.push({
        id: `consumer-${node.id}-${index}`,
        source: schemaNode.id,
        sourceHandle: "source-right",
        target: node.id,
        targetHandle: "target-left",
        label: "CONSUMER",
        markerEnd: { type: MarkerType.ArrowClosed },
        style: { stroke: "#b45309", strokeWidth: 1.6 },
        labelStyle: { fill: "#475569", fontWeight: 600 },
      });
    });

    return { nodes: flowNodes, edges: flowEdges };
  }, [graph]);

  if (!graph?.nodes?.length) {
    return (
      <div className="panel px-5 py-4 text-sm text-slate-500">
        Impact graph is empty for this subject.
      </div>
    );
  }

  return (
    <div className="panel h-[560px] overflow-hidden">
      <ReactFlow
        fitView
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        fitViewOptions={{ padding: 0.18 }}
        proOptions={{ hideAttribution: true }}
      >
        <Background color="#dbe4db" gap={18} />
        <Controls />
      </ReactFlow>
    </div>
  );
}

function inferServiceNode(
  edge: ImpactGraph["edges"][number],
  serviceMap: Map<string, ImpactGraph["nodes"][number]>,
) {
  if (serviceMap.get(edge.from)?.type === "SERVICE") return serviceMap.get(edge.from);
  if (serviceMap.get(edge.to)?.type === "SERVICE") return serviceMap.get(edge.to);
  return undefined;
}

function uniqueNodes(nodes: Array<ImpactGraph["nodes"][number] | undefined>) {
  const seen = new Set<string>();
  return nodes.filter((node): node is ImpactGraph["nodes"][number] => {
    if (!node || seen.has(node.id)) return false;
    seen.add(node.id);
    return true;
  });
}

function centerStartY(count: number) {
  if (count <= 0) return 190;
  return Math.max(40, 220 - ((count - 1) * 140) / 2);
}

function toNode(
  node: ImpactGraph["nodes"][number],
  position: { x: number; y: number },
): Node {
  return {
    id: node.id,
    position,
    type: "impactNode",
    draggable: false,
    selectable: false,
    data: node,
  };
}

function ImpactNode({ data }: NodeProps<ImpactGraph["nodes"][number]>) {
  const impactStyles: Record<string, string> = {
    SAFE: "border-emerald-300 bg-emerald-50",
    WARNING: "border-amber-300 bg-amber-50",
    BREAKING: "border-rose-300 bg-rose-50",
  };

  return (
    <div
      className={`relative w-[220px] rounded-2xl border px-4 py-3 text-left shadow-sm ${
        impactStyles[data.impact || ""] || "border-slate-300 bg-white"
      } ${data.critical ? "ring-2 ring-rose-300" : ""}`}
    >
      <Handle id="source-left" type="source" position={Position.Left} className="!h-2 !w-2 !border-0 !bg-transparent" />
      <Handle id="source-right" type="source" position={Position.Right} className="!h-2 !w-2 !border-0 !bg-transparent" />
      <Handle id="target-left" type="target" position={Position.Left} className="!h-2 !w-2 !border-0 !bg-transparent" />
      <Handle id="target-right" type="target" position={Position.Right} className="!h-2 !w-2 !border-0 !bg-transparent" />

      <div className="text-xs uppercase tracking-wide text-slate-500">
        {data.type}
      </div>
      <div className="mt-1 font-semibold text-slate-900">{data.label}</div>
      <div className="mt-1 text-xs text-slate-600">
        {data.impact || "NEUTRAL"}
        {data.critical ? " | critical" : ""}
      </div>
    </div>
  );
}
