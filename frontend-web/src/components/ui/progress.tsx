'use client';

import { cn } from '@/lib/utils/cn';

interface ProgressBarProps {
  value: number;
  max?: number;
  className?: string;
  showLabel?: boolean;
  variant?: 'default' | 'success' | 'error';
}

export function ProgressBar({
  value,
  max = 100,
  className,
  showLabel = false,
  variant = 'default',
}: ProgressBarProps) {
  const percentage = Math.min(Math.max((value / max) * 100, 0), 100);

  const variantClasses = {
    default: 'bg-blue-600',
    success: 'bg-green-600',
    error: 'bg-red-600',
  };

  return (
    <div className={cn('w-full', className)}>
      <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className={cn(
            'h-full transition-all duration-300 ease-out',
            variantClasses[variant]
          )}
          style={{ width: `${percentage}%` }}
          role="progressbar"
          aria-valuenow={value}
          aria-valuemin={0}
          aria-valuemax={max}
        />
      </div>
      {showLabel && (
        <p className="mt-1 text-sm text-gray-600">
          {Math.round(percentage)}%
        </p>
      )}
    </div>
  );
}

interface ProgressUploadProps {
  filename: string;
  progress: number;
  onCancel?: () => void;
  status?: 'uploading' | 'complete' | 'error';
}

export function ProgressUpload({
  filename,
  progress,
  onCancel,
  status = 'uploading',
}: ProgressUploadProps) {
  const variant = status === 'complete' ? 'success' : status === 'error' ? 'error' : 'default';

  return (
    <div className="rounded-lg border bg-white p-3 shadow-sm">
      <div className="flex items-center justify-between">
        <span className="truncate text-sm font-medium">{filename}</span>
        {status === 'uploading' && onCancel && (
          <button
            onClick={onCancel}
            className="text-sm text-gray-500 hover:text-gray-700"
            aria-label="Cancelar subida"
          >
            ✕
          </button>
        )}
        {status === 'complete' && (
          <span className="text-sm text-green-600">✓</span>
        )}
        {status === 'error' && (
          <span className="text-sm text-red-600">✕</span>
        )}
      </div>
      <div className="mt-2">
        <ProgressBar value={progress} variant={variant} />
      </div>
    </div>
  );
}
