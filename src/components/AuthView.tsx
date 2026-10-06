import React, { useState } from 'react';
import { User } from '../types';
import {
  ShieldCheck,
  Mail,
  Lock,
  Eye,
  EyeOff,
  User as UserIcon,
  Phone,
  Building2,
  ArrowRight,
  CheckCircle2,
  AlertCircle,
} from 'lucide-react';

interface AuthViewProps {
  onLoginSuccess: (user: User) => void;
}

export const AuthView: React.FC<AuthViewProps> = ({ onLoginSuccess }) => {
  // Tab: 'customer' or 'agent'
  const [activePortal, setActivePortal] = useState<'customer' | 'agent'>('customer');
  // Customer mode: 'login' or 'register'
  const [customerMode, setCustomerMode] = useState<'login' | 'register'>('login');

  // Customer Form State
  const [customerEmail, setCustomerEmail] = useState('');
  const [customerPassword, setCustomerPassword] = useState('');
  const [showCustomerPassword, setShowCustomerPassword] = useState(false);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [showRegisterPassword, setShowRegisterPassword] = useState(false);

  // Agent Form State
  const [agentEmail, setAgentEmail] = useState('');
  const [agentPassword, setAgentPassword] = useState('');
  const [showAgentPassword, setShowAgentPassword] = useState(false);

  // Status & Feedback
  const [errorMsg, setErrorMsg] = useState('');
  const [errorDetails, setErrorDetails] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  // Handle Customer Login (Strict MySQL verification)
  const handleCustomerLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setErrorDetails('');
    setSuccessMsg('');

    if (!customerEmail.trim() || !customerPassword.trim()) {
      setErrorMsg('Please enter both email and password.');
      return;
    }

    setLoading(true);

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: customerEmail.trim(),
          password: customerPassword.trim(),
          role: 'CUSTOMER',
        }),
      });

      const data = await res.json();

      // If unauthorized or DB error, strictly DO NOT set state or redirect
      if (!res.ok) {
        setErrorMsg(data.error || 'User not found or invalid credentials');
        if (data.details) {
          setErrorDetails(data.details);
        }
        return;
      }

      if (!data || !data.id) {
        setErrorMsg('User not found or invalid credentials');
        return;
      }

      setSuccessMsg('Authentication successful! Accessing Customer Portal...');
      setTimeout(() => {
        onLoginSuccess(data);
      }, 400);
    } catch (err: any) {
      setErrorMsg('User not found or server unreachable');
      setErrorDetails(err.message || 'Network error connecting to backend API');
    } finally {
      setLoading(false);
    }
  };

  // Handle Customer Registration (Strict MySQL INSERT)
  const handleCustomerRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setErrorDetails('');
    setSuccessMsg('');

    try {
      if (!fullName.trim()) {
        throw new Error('Please enter your full name.');
      }
      if (!customerEmail.trim()) {
        throw new Error('Please enter a valid email address.');
      }
      if (!phone.trim()) {
        throw new Error('Phone number is required for account registration.');
      }
      const phoneDigits = phone.replace(/\D/g, '');
      if (phoneDigits.length < 10) {
        throw new Error('Please enter a valid 10-digit or international phone number.');
      }
      if (customerPassword.length < 4) {
        throw new Error('Password must be at least 4 characters long.');
      }

      setLoading(true);

      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: fullName.trim(),
          fullName: fullName.trim(),
          email: customerEmail.trim(),
          password: customerPassword.trim(),
          phone: phone.trim(),
          role: 'CUSTOMER',
        }),
      });

      const data = await res.json();
      if (!res.ok) {
        setErrorMsg(data.error || 'Registration failed');
        if (data.details) setErrorDetails(data.details);
        return;
      }

      setSuccessMsg('Account registered successfully! Signing you in...');
      setTimeout(() => {
        onLoginSuccess(data.user);
      }, 500);
    } catch (err: any) {
      setErrorMsg(err.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Handle Agent Login (Manual Entry & Verification)
  const handleAgentLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setErrorDetails('');
    setSuccessMsg('');

    if (!agentEmail.trim() || !agentPassword.trim()) {
      setErrorMsg('Please enter both agent email and password.');
      return;
    }

    setLoading(true);

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: agentEmail.trim(),
          password: agentPassword.trim(),
          role: 'AGENT',
        }),
      });

      const data = await res.json();

      // If unauthorized or DB error, strictly DO NOT set state or redirect
      if (!res.ok) {
        setErrorMsg(data.error || 'Invalid credentials. Please contact your system administrator.');
        if (data.details) {
          setErrorDetails(data.details);
        }
        return;
      }

      if (!data || !data.id) {
        setErrorMsg('Invalid credentials. Please contact your system administrator.');
        return;
      }

      setSuccessMsg(`Welcome, ${data.fullName || data.name}! Loading ${data.department || 'Department'} Queue...`);
      setTimeout(() => {
        onLoginSuccess(data);
      }, 400);
    } catch (err: any) {
      setErrorMsg('Authentication request failed. Please check network connection.');
      setErrorDetails(err.message || 'Network error connecting to backend API');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-lg space-y-5">
        {/* Header & Logo */}
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-2xl bg-gradient-to-tr from-[#3a506b] to-[#5bc0be] text-[#0b132b] shadow-xl shadow-[#5bc0be]/20 mb-1">
            <ShieldCheck className="w-9 h-9 stroke-[2.5]" />
          </div>
          <h1 className="text-3xl font-extrabold text-white tracking-tight">
            Intelligent Complaint <span className="text-[#5bc0be]">Resolution</span>
          </h1>
          <p className="text-sm text-slate-300 max-w-md mx-auto">
            Secure multi-department customer and agent service portal
          </p>
        </div>

        {/* Portal Type Toggle */}
        <div className="bg-[#1c2541] p-1.5 rounded-2xl border border-slate-700/80 shadow-lg flex gap-1">
          <button
            type="button"
            id="tab-customer-portal"
            onClick={() => {
              setActivePortal('customer');
              setErrorMsg('');
              setErrorDetails('');
              setSuccessMsg('');
            }}
            className={`flex-1 py-2.5 px-4 rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center justify-center gap-2 ${
              activePortal === 'customer'
                ? 'bg-[#5bc0be] text-[#0b132b] shadow-md shadow-[#5bc0be]/20'
                : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            <UserIcon className="w-4 h-4" />
            Customer Portal
          </button>
          <button
            type="button"
            id="tab-agent-portal"
            onClick={() => {
              setActivePortal('agent');
              setErrorMsg('');
              setErrorDetails('');
              setSuccessMsg('');
            }}
            className={`flex-1 py-2.5 px-4 rounded-xl text-xs sm:text-sm font-bold transition-all flex items-center justify-center gap-2 ${
              activePortal === 'agent'
                ? 'bg-[#5bc0be] text-[#0b132b] shadow-md shadow-[#5bc0be]/20'
                : 'text-slate-300 hover:text-white hover:bg-slate-800/60'
            }`}
          >
            <Building2 className="w-4 h-4" />
            Support Agent
          </button>
        </div>

        {/* Main Auth Card */}
        <div className="cms-card p-6 sm:p-8 border border-slate-700/80 shadow-2xl space-y-6">
          {/* Direct Authentication Error Alert */}
          {errorMsg && (
            <div
              id="auth-error-banner"
              className="p-4 rounded-xl bg-rose-500/15 border-2 border-rose-500/60 text-rose-200 text-xs space-y-1.5 animate-fadeIn"
            >
              <div className="flex items-center gap-2 font-bold text-rose-300 text-sm">
                <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-400" />
                <span>{errorMsg}</span>
              </div>
              {errorDetails && (
                <div className="p-2 rounded-lg bg-black/40 font-mono text-[11px] text-rose-300/90 break-all">
                  {errorDetails}
                </div>
              )}
            </div>
          )}

          {/* Success Message */}
          {successMsg && (
            <div
              id="auth-success-banner"
              className="p-3.5 rounded-xl bg-emerald-500/15 border border-emerald-500/40 text-emerald-300 text-xs flex items-center gap-2.5 animate-fadeIn"
            >
              <CheckCircle2 className="w-4 h-4 flex-shrink-0 text-emerald-400" />
              <span>{successMsg}</span>
            </div>
          )}

          {/* ============================================================
              1. CUSTOMER PORTAL (LOGIN & REGISTRATION)
              ============================================================ */}
          {activePortal === 'customer' && (
            <div>
              {customerMode === 'login' ? (
                /* Customer Login Form */
                <form onSubmit={handleCustomerLogin} className="space-y-4">
                  <div className="pb-1 border-b border-slate-700/60">
                    <h2 className="text-lg font-bold text-white">Customer Sign In</h2>
                    <p className="text-xs text-slate-400">Access your complaint tickets and live resolution status</p>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                      Email Address
                    </label>
                    <div className="relative">
                      <Mail className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="customer-login-email"
                        type="email"
                        required
                        placeholder="you@example.com"
                        value={customerEmail}
                        onChange={(e) => setCustomerEmail(e.target.value)}
                        className="w-full cms-input has-icon pl-11 text-sm py-2.5"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                      Password
                    </label>
                    <div className="relative">
                      <Lock className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="customer-login-password"
                        type={showCustomerPassword ? 'text' : 'password'}
                        required
                        placeholder="••••••••"
                        value={customerPassword}
                        onChange={(e) => setCustomerPassword(e.target.value)}
                        className="w-full cms-input has-icon pl-11 pr-11 text-sm py-2.5"
                      />
                      <button
                        type="button"
                        id="btn-toggle-customer-password"
                        onClick={() => setShowCustomerPassword(!showCustomerPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-[#5bc0be] focus:outline-none p-1 rounded transition"
                        title={showCustomerPassword ? 'Hide password' : 'Show password'}
                      >
                        {showCustomerPassword ? (
                          <EyeOff className="w-4 h-4" />
                        ) : (
                          <Eye className="w-4 h-4" />
                        )}
                      </button>
                    </div>
                  </div>

                  <button
                    id="btn-customer-login"
                    type="submit"
                    disabled={loading}
                    className="w-full cms-btn-primary py-3 text-sm font-bold shadow-lg flex items-center justify-center gap-2 mt-2 disabled:opacity-50"
                  >
                    {loading ? (
                      'Authenticating...'
                    ) : (
                      <>
                        Sign In as Customer
                        <ArrowRight className="w-4 h-4" />
                      </>
                    )}
                  </button>

                  <div className="pt-4 mt-2 border-t border-slate-700/60 text-center">
                    <p className="text-xs text-slate-300">
                      Don't have an account?{' '}
                      <button
                        type="button"
                        id="link-go-to-register"
                        onClick={() => {
                          setCustomerMode('register');
                          setErrorMsg('');
                          setErrorDetails('');
                          setSuccessMsg('');
                        }}
                        className="text-[#5bc0be] hover:text-[#6fffe9] font-bold underline transition ml-1"
                      >
                        Register New Account
                      </button>
                    </p>
                  </div>
                </form>
              ) : (
                /* Customer Registration Form */
                <form onSubmit={handleCustomerRegister} className="space-y-4 animate-fadeIn">
                  <div className="flex items-center justify-between pb-1 border-b border-slate-700/60">
                    <div>
                      <h2 className="text-lg font-bold text-white">Customer Registration</h2>
                      <p className="text-xs text-slate-400">Create a secure profile to track your service requests</p>
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                      Full Name <span className="text-rose-400">*</span>
                    </label>
                    <div className="relative">
                      <UserIcon className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="register-fullname"
                        type="text"
                        required
                        placeholder="John Doe"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        className="w-full cms-input has-icon pl-11 text-sm py-2.5"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                      Email Address <span className="text-rose-400">*</span>
                    </label>
                    <div className="relative">
                      <Mail className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="register-email"
                        type="email"
                        required
                        placeholder="john.doe@example.com"
                        value={customerEmail}
                        onChange={(e) => setCustomerEmail(e.target.value)}
                        className="w-full cms-input has-icon pl-11 text-sm py-2.5"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                      Phone Number <span className="text-rose-400">*</span>
                    </label>
                    <div className="relative">
                      <Phone className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="register-phone"
                        type="tel"
                        required
                        placeholder="+91 98765 43210"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        className="w-full cms-input has-icon pl-11 text-sm py-2.5"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                      Password <span className="text-rose-400">*</span>
                    </label>
                    <div className="relative">
                      <Lock className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="register-password"
                        type={showRegisterPassword ? 'text' : 'password'}
                        required
                        placeholder="Create a password"
                        value={customerPassword}
                        onChange={(e) => setCustomerPassword(e.target.value)}
                        className="w-full cms-input has-icon pl-11 pr-11 text-sm py-2.5"
                      />
                      <button
                        type="button"
                        id="btn-toggle-register-password"
                        onClick={() => setShowRegisterPassword(!showRegisterPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-[#5bc0be] focus:outline-none p-1 rounded transition"
                        title={showRegisterPassword ? 'Hide password' : 'Show password'}
                      >
                        {showRegisterPassword ? (
                          <EyeOff className="w-4 h-4" />
                        ) : (
                          <Eye className="w-4 h-4" />
                        )}
                      </button>
                    </div>
                  </div>

                  <button
                    id="btn-create-account"
                    type="submit"
                    disabled={loading}
                    className="w-full cms-btn-primary py-3 text-sm font-bold shadow-lg flex items-center justify-center gap-2 mt-2 disabled:opacity-50"
                  >
                    {loading ? (
                      'Creating Account...'
                    ) : (
                      <>
                        Create Account
                        <ArrowRight className="w-4 h-4" />
                      </>
                    )}
                  </button>

                  <div className="pt-4 mt-2 border-t border-slate-700/60 text-center">
                    <p className="text-xs text-slate-300">
                      Already have an account?{' '}
                      <button
                        type="button"
                        id="link-go-to-login"
                        onClick={() => {
                          setCustomerMode('login');
                          setErrorMsg('');
                          setErrorDetails('');
                          setSuccessMsg('');
                        }}
                        className="text-[#5bc0be] hover:text-[#6fffe9] font-bold underline transition ml-1"
                      >
                        Log in
                      </button>
                    </p>
                  </div>
                </form>
              )}
            </div>
          )}

          {/* ============================================================
              2. SUPPORT AGENT LOGIN TAB (MANUAL ENTRY ONLY)
              ============================================================ */}
          {activePortal === 'agent' && (
            <form onSubmit={handleAgentLogin} className="space-y-5 animate-fadeIn">
              <div className="pb-1 border-b border-slate-700/60">
                <h2 className="text-lg font-bold text-white">Support Agent Sign In</h2>
                <p className="text-xs text-slate-400">
                  Enter your internal staff credentials to access department ticket queues
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Agent Email
                </label>
                <div className="relative">
                  <Mail className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                  <input
                    id="agent-login-email"
                    type="email"
                    required
                    placeholder="Enter agent email"
                    value={agentEmail}
                    onChange={(e) => setAgentEmail(e.target.value)}
                    className="w-full cms-input has-icon pl-11 text-sm py-2.5"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Password
                </label>
                <div className="relative">
                  <Lock className="w-5 h-5 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                  <input
                    id="agent-login-password"
                    type={showAgentPassword ? 'text' : 'password'}
                    required
                    placeholder="••••••••"
                    value={agentPassword}
                    onChange={(e) => setAgentPassword(e.target.value)}
                    className="w-full cms-input has-icon pl-11 pr-11 text-sm py-2.5"
                  />
                  <button
                    type="button"
                    id="btn-toggle-agent-password"
                    onClick={() => setShowAgentPassword(!showAgentPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-[#5bc0be] focus:outline-none p-1 rounded transition"
                    title={showAgentPassword ? 'Hide password' : 'Show password'}
                  >
                    {showAgentPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              <button
                id="btn-agent-login"
                type="submit"
                disabled={loading}
                className="w-full cms-btn-primary py-3 text-sm font-bold shadow-lg flex items-center justify-center gap-2 mt-2 disabled:opacity-50"
              >
                {loading ? (
                  'Verifying Credentials...'
                ) : (
                  <>
                    Sign In as Agent
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>

              <div className="text-center text-[11px] text-slate-400 pt-2">
                Are you a customer?{' '}
                <button
                  type="button"
                  onClick={() => {
                    setActivePortal('customer');
                    setErrorMsg('');
                    setErrorDetails('');
                    setSuccessMsg('');
                  }}
                  className="text-[#5bc0be] hover:underline font-semibold"
                >
                  Switch to Customer Login
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Feature Highlights Footer */}
        <div className="grid grid-cols-3 gap-3 text-center text-xs text-slate-400 pt-2">
          <div className="p-2.5 rounded-xl bg-[#1c2541]/40 border border-slate-800/80">
            <span className="font-semibold text-slate-200 block">AI Auto-Triage</span>
            <span className="text-[10px] text-slate-400">Direct Department Routing</span>
          </div>
          <div className="p-2.5 rounded-xl bg-[#1c2541]/40 border border-slate-800/80">
            <span className="font-semibold text-slate-200 block">Real-Time SLA</span>
            <span className="text-[10px] text-slate-400">Live Status Tracking</span>
          </div>
          <div className="p-2.5 rounded-xl bg-[#1c2541]/40 border border-slate-800/80">
            <span className="font-semibold text-slate-200 block">Multi-Department</span>
            <span className="text-[10px] text-slate-400">Finance, Tech, Care, Logistics</span>
          </div>
        </div>
      </div>
    </div>
  );
};
