import { useParams } from 'react-router-dom';
import { usePerformance } from '@/features/performance/api/queries';

export function PerformanceDetailPage() {
  const { id } = useParams();
  const { data, isLoading, error } = usePerformance(Number(id));

  if (isLoading) return <p>불러오는 중…</p>;
  if (error) return <p style={{ color: 'crimson' }}>{(error as Error).message}</p>;
  if (!data) return <p>공연을 찾을 수 없습니다.</p>;

  return (
    <article>
      <h1>{data.title}</h1>
      <p>장소: {data.venue ?? '미정'}</p>
      <p>예매 오픈: {new Date(data.opensAt).toLocaleString()}</p>
      <p>공연 시작: {new Date(data.startsAt).toLocaleString()}</p>
      <p>상태: {data.status}</p>
      {/* TODO: 대기열 진입 → 좌석 선택 → 예매/결제 플로우 */}
    </article>
  );
}
