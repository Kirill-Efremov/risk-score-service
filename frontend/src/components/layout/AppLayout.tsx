import type { PropsWithChildren } from "react";
import { useLocation } from "react-router-dom";
import { Header } from "./Header";
import { Sidebar } from "./Sidebar";

export function AppLayout({ children }: PropsWithChildren) {
  const location = useLocation();
  const isAuthPage =
    location.pathname === "/login" || location.pathname === "/register";

  if (isAuthPage) {
    return (
      <div className="min-h-screen p-4 md:p-6">
        <div className="mx-auto flex max-w-[900px] flex-col gap-6">
          <Header />
          <main className="flex min-h-[calc(100vh-220px)] items-center justify-center">
            {children}
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen p-4 md:p-6">
      <div className="mx-auto flex max-w-[1600px] flex-col gap-6 lg:flex-row lg:items-start">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col gap-6">
          <Header />
          <main className="space-y-6">{children}</main>
        </div>
      </div>
    </div>
  );
}
