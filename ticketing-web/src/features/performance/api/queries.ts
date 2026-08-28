import { useQuery } from '@tanstack/react-query';
import { httpClient, unwrap, type ApiEnvelope } from '@/shared/api/client';
import type { Performance } from './types';

const keys = {
  all: ['performances'] as const,
  detail: (id: number) => ['performances', id] as const,
};

export function usePerformances() {
  return useQuery({
    queryKey: keys.all,
    queryFn: () =>
      unwrap<Performance[]>(httpClient.get<ApiEnvelope<Performance[]>>('/v1/performances')),
  });
}

export function usePerformance(id: number) {
  return useQuery({
    queryKey: keys.detail(id),
    queryFn: () =>
      unwrap<Performance>(httpClient.get<ApiEnvelope<Performance>>(`/v1/performances/${id}`)),
    enabled: Number.isFinite(id),
  });
}
