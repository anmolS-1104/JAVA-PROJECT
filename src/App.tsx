import React, { useState, useEffect } from 'react';
import { User } from './types';
import { Navbar } from './components/Navbar';
import { CustomerDashboard } from './components/CustomerDashboard';
import { AgentDashboard } from './components/AgentDashboard';
import { AuthView } from './components/AuthView';

export function App() {
  const [currentUser, setCurrentUser] = useState<User | null>(null);

  useEffect(() => {
    if (currentUser) {
      localStorage.setItem('cms_active_user', JSON.stringify(currentUser));
    } else {
      localStorage.removeItem('cms_active_user');
    }
  }, [currentUser]);

  const handleLoginSuccess = (user: User) => {
    setCurrentUser(user);
  };

  const handleLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem('cms_active_user');
  };

  return (
    <div className="min-h-screen bg-[#0b132b] text-slate-100 flex flex-col selection:bg-[#5bc0be]/30 selection:text-[#6fffe9]">
      {/* Top Navigation */}
      <Navbar
        currentUser={currentUser}
        onLogout={handleLogout}
        onOpenAuth={() => setCurrentUser(null)}
      />

      {/* Main View Area - Strict Role-Based */}
      <main className="flex-1">
        {!currentUser ? (
          /* Authentication Screen */
          <AuthView onLoginSuccess={handleLoginSuccess} />
        ) : currentUser.role === 'AGENT' ? (
          /* Agent Command Center strictly for their assigned department */
          <AgentDashboard
            currentUser={currentUser}
            onUpdateDepartment={(dept) => {
              setCurrentUser({ ...currentUser, department: dept });
            }}
          />
        ) : (
          /* Customer Portal strictly showing Submit Complaint & My Complaint History */
          <CustomerDashboard
            currentUser={currentUser}
            onOpenAuth={handleLogout}
          />
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 bg-[#0b132b] py-6 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <p>© 2026 Intelligent Complaint Management System (ICRS) • Enterprise Service Desk</p>
          <div className="flex items-center gap-4 text-slate-400">
            <span>Finance & Payroll</span>
            <span>•</span>
            <span>Technical Support</span>
            <span>•</span>
            <span>Logistics</span>
            <span>•</span>
            <span>Customer Care</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;

