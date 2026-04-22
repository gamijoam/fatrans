import { useQuery, useMutation, UseQueryOptions, UseMutationOptions } from '@tanstack/react-query';
import { toast } from 'sonner';
import { ApiError } from '@/types/api';

type QueryOptions<T> = UseQueryOptions<T, ApiError>;
type MutationOptions<T, V> = UseMutationOptions<T, ApiError, V>;

export function useApiQuery<T>(options: QueryOptions<T>) {
  return useQuery<T, ApiError>({
    ...options,
    staleTime: options.staleTime ?? 30_000,
    retry: options.retry ?? 2,
  });
}

export function useApiMutation<T, V>(options: MutationOptions<T, V>) {
  return useMutation<T, ApiError, V>({
    ...options,
    retry: options.retry ?? 1,
    onError: options.onError ?? ((error: ApiError) => toast.error(error.message || 'Error desconocido')),
  });
}