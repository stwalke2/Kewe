export type ExportFormat = 'csv' | 'excel';

export function downloadSettingsExport(filenameBase: string, rows: Array<Record<string, string>>, format: ExportFormat) {
  if (!rows.length) return;
  const headers = Object.keys(rows[0]);
  const delimiter = format === 'excel' ? '\t' : ',';
  const ext = format === 'excel' ? 'xls' : 'csv';
  const mime = format === 'excel' ? 'application/vnd.ms-excel;charset=utf-8;' : 'text/csv;charset=utf-8;';

  const lines = [headers.join(delimiter), ...rows.map((row) => headers.map((header) => escapeCell(row[header] ?? '', delimiter)).join(delimiter))];
  const blob = new Blob([`\ufeff${lines.join('\n')}`], { type: mime });
  const url = URL.createObjectURL(blob);

  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `${filenameBase}.${ext}`;
  anchor.click();
  URL.revokeObjectURL(url);
}

function escapeCell(value: string, delimiter: string): string {
  const normalized = value.replace(/\r?\n/g, ' ').trim();
  if (normalized.includes('"')) {
    return `"${normalized.replace(/"/g, '""')}"`;
  }
  if (normalized.includes(delimiter)) {
    return `"${normalized}"`;
  }
  return normalized;
}
