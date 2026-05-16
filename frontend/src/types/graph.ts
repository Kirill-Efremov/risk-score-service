export interface ImpactGraphNode {
  id: string;
  type: "SCHEMA" | "SERVICE";
  label: string;
  impact?: "SAFE" | "WARNING" | "BREAKING" | null;
  critical: boolean;
}

export interface ImpactGraphEdge {
  from: string;
  to: string;
  type: "PRODUCER" | "CONSUMER";
}

export interface ImpactGraph {
  nodes: ImpactGraphNode[];
  edges: ImpactGraphEdge[];
}
