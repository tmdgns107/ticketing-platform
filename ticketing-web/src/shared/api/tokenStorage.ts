const ACCESS_TOKEN_KEY = 'ticketing.accessToken';

export const tokenStorage = {
  get(): string | null {
    try {
      return localStorage.getItem(ACCESS_TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set(token: string): void {
    try {
      localStorage.setItem(ACCESS_TOKEN_KEY, token);
    } catch {
      /* storage unavailable (private mode) — treat as no-op */
    }
  },
  clear(): void {
    try {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    } catch {
      /* no-op */
    }
  },
};
