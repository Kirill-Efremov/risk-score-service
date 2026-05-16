import Editor from "@monaco-editor/react";

interface SchemaEditorProps {
  value: string;
  onChange: (value: string) => void;
  readOnly?: boolean;
  title: string;
  exampleValue?: string;
}

export function SchemaEditor({
  value,
  onChange,
  readOnly,
  title,
  exampleValue,
}: SchemaEditorProps) {
  const formatJson = () => {
    try {
      onChange(JSON.stringify(JSON.parse(value), null, 2));
    } catch {
      onChange(value);
    }
  };

  return (
    <div className="panel overflow-hidden">
      <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
        <div>
          <p className="font-semibold text-slate-800">{title}</p>
          <p className="text-xs text-slate-500">JSON schema editor</p>
        </div>
        <div className="flex gap-2">
          {exampleValue ? (
            <button
              type="button"
              className="btn-secondary"
              onClick={() => onChange(exampleValue)}
            >
              Load example
            </button>
          ) : null}
          <button type="button" className="btn-secondary" onClick={formatJson}>
            Format JSON
          </button>
          {!readOnly ? (
            <button
              type="button"
              className="btn-secondary"
              onClick={() => onChange("")}
            >
              Clear
            </button>
          ) : null}
        </div>
      </div>
      <div className="h-[340px]">
        <Editor
          defaultLanguage="json"
          theme="vs-light"
          value={value}
          onChange={(next) => onChange(next ?? "")}
          options={{
            readOnly,
            minimap: { enabled: false },
            fontSize: 13,
            wordWrap: "on",
            lineNumbers: "on",
            automaticLayout: true,
          }}
        />
      </div>
    </div>
  );
}
