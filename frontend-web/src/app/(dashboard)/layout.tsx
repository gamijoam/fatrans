export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen">
      <aside className="w-64 bg-gray-100 p-4">Dashboard Sidebar</aside>
      <main className="flex-1">{children}</main>
    </div>
  );
}