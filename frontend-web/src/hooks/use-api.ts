import { useQuery, useMutation, UseQueryOptions, UseMutationOptions } from '@tanstack/react-query';
import { ApiError } from '@/types/api';

type QueryOptions<T, E = ApiError> = UseQueryOptions<T, E>;
type MutationOptions<T, V, E = ApiError> = UseMutationOptions<T, E, V>;

export function useApiQuery<T>(options: QueryOptions<T, ApiError>) {
  return useQuery<T, ApiError>(options);
}

export function useApiMutation<T, V>(options: MutationOptions<T, V, ApiError>) {
  return useMutation<T, ApiError, V>(options);
}