import axios, { AxiosError, AxiosHeaders } from 'axios';
import { tokenStorage } from './tokenStorage';

export interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
  error: { code: string; message: string } | null;
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number,
    readonly code?: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10_000,
});

httpClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    const headers = AxiosHeaders.from(config.headers);
    headers.set('Authorization', `Bearer ${token}`);
    config.headers = headers;
  }
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiEnvelope<unknown>>) => {
    const status = error.response?.status;
    const code = error.response?.data?.error?.code;
    const message =
      error.response?.data?.error?.message ?? error.message ?? '알 수 없는 오류가 발생했습니다.';

    if (status === 401) {
      // TODO: refresh-token flow. For now drop the stale token; route guards
      // will bounce the user to the login page once auth pages exist.
      tokenStorage.clear();
    }

    return Promise.reject(new ApiError(message, status, code));
  },
);

export async function unwrap<T>(promise: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  const { data } = await promise;
  if (!data.success || data.data === null) {
    throw new ApiError(data.error?.message ?? '응답을 처리할 수 없습니다.', undefined, data.error?.code);
  }
  return data.data;
}
