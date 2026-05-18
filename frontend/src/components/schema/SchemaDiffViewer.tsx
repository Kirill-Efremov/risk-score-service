import ReactDiffViewer, { DiffMethod } from "react-diff-viewer-continued";

export function SchemaDiffViewer({
  oldValue,
  newValue,
}: {
  oldValue?: string | null;
  newValue?: string | null;
}) {
  if (!oldValue && !newValue) {
    return (
      <div className="panel px-5 py-4 text-sm text-slate-500">
        Schema text is not available for visual comparison.
      </div>
    );
  }

  const normalizedOldValue = normalizeSchemaForDiff(oldValue);
  const normalizedNewValue = normalizeSchemaForDiff(newValue);

  return (
    <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
      <ReactDiffViewer
        oldValue={normalizedOldValue}
        newValue={normalizedNewValue}
        splitView
        showDiffOnly={false}
        compareMethod={DiffMethod.LINES}
        leftTitle="Old schema"
        rightTitle="New schema"
        styles={{
          variables: {
            dark: {
              diffViewerBackground: "#ffffff",
              diffViewerColor: "#0f172a",
              addedBackground: "#dcfce7",
              removedBackground: "#fee2e2",
              wordAddedBackground: "#86efac",
              wordRemovedBackground: "#fca5a5",
              addedColor: "#166534",
              removedColor: "#991b1b",
            },
            light: {
              diffViewerBackground: "#ffffff",
              diffViewerColor: "#0f172a",
              addedBackground: "#dcfce7",
              removedBackground: "#fee2e2",
              wordAddedBackground: "#86efac",
              wordRemovedBackground: "#fca5a5",
              addedColor: "#166534",
              removedColor: "#991b1b",
            },
          },
        }}
      />
    </div>
  );
}

function normalizeSchemaForDiff(value?: string | null) {
  if (!value) {
    return "";
  }

  const trimmed = value.trim();
  if (!trimmed) {
    return "";
  }

  try {
    return JSON.stringify(JSON.parse(trimmed), null, 2);
  } catch {
    return trimmed;
  }
}
