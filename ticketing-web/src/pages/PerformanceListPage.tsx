import { Link } from 'react-router-dom';
import { usePerformances } from '@/features/performance/api/queries';

export function PerformanceListPage() {
  const { data, isLoading, error } = usePerformances();

  if (isLoading) return <p>불러오는 중…</p>;
  if (error) return <p style={{ color: 'crimson' }}>{(error as Error).message}</p>;
  if (!data || data.length === 0) return <p>등록된 공연이 없습니다.</p>;

  return (
    <ul style={{ listStyle: 'none', padding: 0, display: 'grid', gap: 12 }}>
      {data.map((performance) => (
        <li
          key={performance.id}
          style={{ border: '1px solid #8884', borderRadius: 8, padding: 16 }}
        >
          <Link to={`/performances/${performance.id}`} style={{ textDecoration: 'none' }}>
            <strong>{performance.title}</strong>
          </Link>
          <div style={{ opacity: 0.7, fontSize: 14 }}>
            {performance.venue ?? '장소 미정'} · {performance.status}
          </div>
        </li>
      ))}
    </ul>
  );
}
