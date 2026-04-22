import { toast } from 'sonner';

export const toastSuccess = (message: string, description?: string) => {
  toast.success(message, {
    description,
    className: 'bg-green-50 border-green-200 text-green-800',
  });
};

export const toastError = (message: string, description?: string) => {
  toast.error(message, {
    description,
    className: 'bg-red-50 border-red-200 text-red-800',
  });
};

export const toastInfo = (message: string, description?: string) => {
  toast.info(message, {
    description,
    className: 'bg-blue-50 border-blue-200 text-blue-800',
  });
};

export const toastWarning = (message: string, description?: string) => {
  toast.warning(message, {
    description,
    className: 'bg-yellow-50 border-yellow-200 text-yellow-800',
  });
};

export const toastLoading = (message: string) => {
  return toast.loading(message, {
    className: 'bg-gray-50 border-gray-200',
  });
};

export const toastDismiss = (id: string | number) => {
  toast.dismiss(id);
};

export const toastPromise = <T>(
  promise: Promise<T>,
  messages: {
    loading: string;
    success: string;
    error: string;
  }
) => {
  return toast.promise(promise, {
    loading: messages.loading,
    success: messages.success,
    error: messages.error,
  });
};
