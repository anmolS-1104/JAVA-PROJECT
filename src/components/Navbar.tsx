import React from 'react';
import { User } from '../types';
import { ShieldCheck, User as UserIcon, LogOut, Building2 } from 'lucide-react';

interface NavbarProps {
  currentUser: User | null;
  onLogout: () => void;
  onOpenAuth: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentUser,
  onLogout,
  onOpenAuth,
}) => {
  return (
    <header className="sticky top-0 z-40 bg-[#0b132b]/95 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand / Logo */}
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#3a506b] to-[#5bc0be] flex items-center justify-center shadow-lg shadow-[#5bc0be]/20 text-[#0b132b]">
            <ShieldCheck className="w-6 h-6 stroke-[2.5]" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-lg tracking-tight text-white">
                Company<span className="text-[#5bc0be]">CMS</span>
              </span>
              {currentUser?.role === 'AGENT' ? (
                <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-300 border border-amber-500/30 flex items-center gap-1">
                  <Building2 className="w-3 h-3" /> Agent Desk
                </span>
              ) : currentUser?.role === 'CUSTOMER' ? (
                <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#5bc0be]/15 text-[#6fffe9] border border-[#5bc0be]/30 flex items-center gap-1">
                  <UserIcon className="w-3 h-3" /> Customer Portal
                </span>
              ) : (
                <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#5bc0be]/15 text-[#6fffe9] border border-[#5bc0be]/30">
                  AI Powered
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400">
              {currentUser?.role === 'AGENT'
                ? `${currentUser.department || 'Support'} Command Center`
                : 'Intelligent Complaint Resolution System'}
            </p>
          </div>
        </div>

        {/* User Account / Sign Out Actions */}
        <div className="flex items-center space-x-3">
          {currentUser ? (
            <div className="flex items-center space-x-3">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-semibold text-slate-100">{currentUser.fullName}</p>
                <p className="text-xs text-[#5bc0be] flex items-center justify-end gap-1 font-mono">
                  {currentUser.role === 'AGENT' ? (
                    <>
                      <Building2 className="w-3 h-3" />
                      {currentUser.department || 'Department Agent'}
                    </>
                  ) : (
                    <>
                      <UserIcon className="w-3 h-3" />
                      Customer
                    </>
                  )}
                </p>
              </div>

              <div className="w-9 h-9 rounded-full bg-[#1c2541] border border-[#5bc0be]/40 flex items-center justify-center text-[#5bc0be] font-bold text-sm">
                {currentUser.fullName.charAt(0).toUpperCase()}
              </div>

              <button
                id="btn-sign-out"
                onClick={onLogout}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-300 bg-slate-800/80 border border-slate-700 hover:text-rose-300 hover:bg-rose-500/15 hover:border-rose-500/30 transition-all shadow-sm"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Sign Out</span>
              </button>
            </div>
          ) : (
            <button
              id="btn-sign-in"
              onClick={onOpenAuth}
              className="cms-btn-primary text-sm py-2 px-4 shadow-md"
            >
              <UserIcon className="w-4 h-4" />
              Sign In / Register
            </button>
          )}
        </div>
      </div>
    </header>
  );
};

