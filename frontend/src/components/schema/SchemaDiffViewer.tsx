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
        Текст схем недоступен для визуального сравнения.
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
      <ReactDiffViewer
        oldValue={oldValue ?? ""}
        newValue={newValue ?? ""}
        splitView
        showDiffOnly={false}
        compareMethod={DiffMethod.WORDS}
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
