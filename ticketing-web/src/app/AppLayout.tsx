import { Link, Outlet } from 'react-router-dom';

export function AppLayout() {
  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: '24px 16px' }}>
      <header style={{ marginBottom: 24 }}>
        <Link to="/" style={{ fontWeight: 700, fontSize: 20, textDecoration: 'none' }}>
          🎟️ Ticketing
        </Link>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
