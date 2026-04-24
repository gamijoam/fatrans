export function formatDate(dateStr: string | null | undefined, includeTime: boolean = false): string {
  if (!dateStr) return '-';
  const options: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  };
  if (includeTime) {
    options.hour = '2-digit';
    options.minute = '2-digit';
  }
  return new Date(dateStr).toLocaleDateString('es-VE', options);
}

export function formatCurrency(value: number, decimals: number = 0): string {
  return new Intl.NumberFormat('es-VE', {
    style: 'currency',
    currency: 'VES',
    minimumFractionDigits: decimals,
  }).format(value);
}