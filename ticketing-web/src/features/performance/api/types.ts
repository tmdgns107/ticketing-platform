export type PerformanceStatus = 'SCHEDULED' | 'ON_SALE' | 'SOLD_OUT' | 'CLOSED';

export interface Performance {
  id: number;
  title: string;
  venue: string | null;
  opensAt: string;
  startsAt: string;
  status: PerformanceStatus;
}
