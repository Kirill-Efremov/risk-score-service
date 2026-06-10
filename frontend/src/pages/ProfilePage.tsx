import { useAuth } from "../auth/AuthContext";

export function ProfilePage() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <div className="space-y-6">
      <section className="panel p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <h2 className="text-2xl font-semibold text-slate-900">Profile</h2>
            <p className="mt-1 text-sm text-slate-500">
              Current account details from the active authenticated session.
            </p>
          </div>
          <button className="btn-secondary" onClick={logout}>
            Log out
          </button>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ProfileCard
          label="Authentication status"
          value={isAuthenticated ? "Authenticated" : "Not authenticated"}
        />
        <ProfileCard label="Username" value={user?.username ?? "-"} />
        <ProfileCard label="Role" value={user?.role ?? "-"} />
        <ProfileCard label="User ID" value={user ? String(user.id) : "-"} />
        <ProfileCard label="Active" value={user ? String(user.active) : "-"} />
        <ProfileCard label="Created at" value={user?.createdAt ?? "-"} />
      </section>
    </div>
  );
}

function ProfileCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="panel p-5">
      <p className="text-xs uppercase tracking-[0.24em] text-slate-500">
        {label}
      </p>
      <p className="mt-2 text-lg font-semibold text-slate-900">{value}</p>
    </div>
  );
}
