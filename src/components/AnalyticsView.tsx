import React, { useState, useEffect, useMemo } from 'react';
import { AnalyticsData, Complaint } from '../types';
import {
  BarChart3,
  CheckCircle2,
  Clock,
  AlertCircle,
  Building2,
  TrendingUp,
  RefreshCw,
  PieChart as PieIcon,
  ShieldAlert,
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip as RechartsTooltip,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend,
} from 'recharts';

export const AnalyticsView: React.FC = () => {
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchAnalytics = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/complaints');
      if (res.ok) {
        const data: Complaint[] = await res.json();
        setComplaints(data);
      }
    } catch (err) {
      console.error('Failed to fetch analytics data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const stats = useMemo(() => {
    const total = complaints.length;
    const pending = complaints.filter((c) => c.status === 'OPEN').length;
    const inProgress = complaints.filter((c) => c.status === 'IN_PROGRESS').length;
    const resolved = complaints.filter((c) => c.status === 'RESOLVED').length;

    const finance = complaints.filter((c) => c.department.includes('Finance')).length;
    const technical = complaints.filter((c) => c.department.includes('Technical')).length;
    const logistics = complaints.filter((c) => c.department.includes('Logistics')).length;
    const customerCare = complaints.filter((c) => c.department.includes('Customer Care')).length;

    const resolutionRate = total > 0 ? Math.round((resolved / total) * 100) : 0;

    return {
      total,
      pending,
      inProgress,
      resolved,
      finance,
      technical,
      logistics,
      customerCare,
      resolutionRate,
    };
  }, [complaints]);

  // Chart datasets
  const statusPieData = [
    { name: 'Pending (Open)', value: stats.pending, color: '#38bdf8' },
    { name: 'In Progress', value: stats.inProgress, color: '#f59e0b' },
    { name: 'Resolved', value: stats.resolved, color: '#10b981' },
  ].filter((d) => d.value > 0);

  const departmentBarData = [
    { name: 'Finance & Payroll', total: stats.finance, resolved: complaints.filter((c) => c.department.includes('Finance') && c.status === 'RESOLVED').length },
    { name: 'Tech Support', total: stats.technical, resolved: complaints.filter((c) => c.department.includes('Technical') && c.status === 'RESOLVED').length },
    { name: 'Logistics', total: stats.logistics, resolved: complaints.filter((c) => c.department.includes('Logistics') && c.status === 'RESOLVED').length },
    { name: 'Customer Care', total: stats.customerCare, resolved: complaints.filter((c) => c.department.includes('Customer Care') && c.status === 'RESOLVED').length },
  ];

  const priorityData = [
    { name: 'High Priority', count: complaints.filter((c) => c.priority === 'HIGH').length, fill: '#f43f5e' },
    { name: 'Medium Priority', count: complaints.filter((c) => c.priority === 'MEDIUM').length, fill: '#f59e0b' },
    { name: 'Normal / Low', count: complaints.filter((c) => ['NORMAL', 'LOW'].includes(c.priority)).length, fill: '#5bc0be' },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-[#1c2541] to-[#2b3a50] p-6 rounded-2xl border border-slate-700/80 shadow-lg">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight flex items-center gap-2.5">
            <BarChart3 className="w-7 h-7 text-[#5bc0be]" />
            ICRS Analytics & Performance
          </h1>
          <p className="text-sm text-slate-300 mt-1">
            Real-time complaint throughput, department load, and resolution benchmarks.
          </p>
        </div>
        <button
          onClick={fetchAnalytics}
          className="cms-btn-secondary text-xs py-2 px-4 self-start sm:self-auto shadow-md"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh Stats
        </button>
      </div>

      {/* Top Metric Cards matching AnalyticsController labels */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {/* Total Complaints */}
        <div className="cms-card p-5 border border-slate-700 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Complaints</span>
            <div className="p-2 rounded-lg bg-[#1c2541] text-[#5bc0be]">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <p id="totalLabel" className="text-3xl font-extrabold text-white mt-2 font-mono">
            {stats.total}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Across all enterprise departments</p>
        </div>

        {/* Pending (Open) */}
        <div className="cms-card p-5 border border-slate-700 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Pending / Open</span>
            <div className="p-2 rounded-lg bg-sky-500/10 text-sky-400">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <p id="pendingLabel" className="text-3xl font-extrabold text-sky-400 mt-2 font-mono">
            {stats.pending}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Awaiting specialist assignment</p>
        </div>

        {/* In Progress */}
        <div className="cms-card p-5 border border-slate-700 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">In Progress</span>
            <div className="p-2 rounded-lg bg-amber-500/10 text-amber-400">
              <AlertCircle className="w-4 h-4" />
            </div>
          </div>
          <p id="inProgressLabel" className="text-3xl font-extrabold text-amber-400 mt-2 font-mono">
            {stats.inProgress}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Under active agent review</p>
        </div>

        {/* Resolved */}
        <div className="cms-card p-5 border border-slate-700 relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Resolved</span>
            <div className="p-2 rounded-lg bg-emerald-500/10 text-emerald-400">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <p id="resolvedLabel" className="text-3xl font-extrabold text-emerald-400 mt-2 font-mono">
            {stats.resolved}
          </p>
          <p className="text-[11px] text-slate-400 mt-1">{stats.resolutionRate}% resolution success rate</p>
        </div>
      </div>

      {/* Department Breakdown Cards matching financeLabel, logisticsLabel, technicalLabel */}
      <div className="cms-card p-6 border border-slate-700 shadow-xl space-y-4">
        <h2 className="text-base font-bold text-white flex items-center gap-2">
          <Building2 className="w-5 h-5 text-[#5bc0be]" />
          Department Breakdown Metrics
        </h2>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 rounded-xl bg-[#1c2541] border border-slate-700 text-center">
            <span className="text-xs text-slate-400 block mb-1">Finance & Payroll</span>
            <span id="financeLabel" className="text-2xl font-bold text-[#6fffe9] font-mono">
              {stats.finance}
            </span>
            <span className="text-[10px] text-slate-400 block mt-1">Billing & Invoices</span>
          </div>

          <div className="p-4 rounded-xl bg-[#1c2541] border border-slate-700 text-center">
            <span className="text-xs text-slate-400 block mb-1">Technical Support</span>
            <span id="technicalLabel" className="text-2xl font-bold text-[#6fffe9] font-mono">
              {stats.technical}
            </span>
            <span className="text-[10px] text-slate-400 block mt-1">Systems & APIs</span>
          </div>

          <div className="p-4 rounded-xl bg-[#1c2541] border border-slate-700 text-center">
            <span className="text-xs text-slate-400 block mb-1">Logistics</span>
            <span id="logisticsLabel" className="text-2xl font-bold text-[#6fffe9] font-mono">
              {stats.logistics}
            </span>
            <span className="text-[10px] text-slate-400 block mt-1">Shipping & Warehouse</span>
          </div>

          <div className="p-4 rounded-xl bg-[#1c2541] border border-slate-700 text-center">
            <span className="text-xs text-slate-400 block mb-1">Customer Care</span>
            <span className="text-2xl font-bold text-[#6fffe9] font-mono">
              {stats.customerCare}
            </span>
            <span className="text-[10px] text-slate-400 block mt-1">General Inquiries</span>
          </div>
        </div>
      </div>

      {/* Visual Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Department Volume vs Resolution */}
        <div className="lg:col-span-7 cms-card p-6 border border-slate-700 shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <Building2 className="w-4 h-4 text-[#5bc0be]" />
            Department Ticket Distribution & Resolution
          </h3>
          <div className="h-64 sm:h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={departmentBarData} margin={{ top: 20, right: 20, left: -15, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <RechartsTooltip
                  contentStyle={{ backgroundColor: '#1c2541', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                <Bar dataKey="total" name="Total Tickets" fill="#3a506b" radius={[4, 4, 0, 0]} />
                <Bar dataKey="resolved" name="Resolved" fill="#5bc0be" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Status Distribution Pie */}
        <div className="lg:col-span-5 cms-card p-6 border border-slate-700 shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <PieIcon className="w-4 h-4 text-[#5bc0be]" />
            Overall Status Breakdown
          </h3>
          <div className="h-64 sm:h-72 w-full flex items-center justify-center">
            {statusPieData.length === 0 ? (
              <p className="text-slate-400 text-xs">No complaint data recorded.</p>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={statusPieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={45}
                    outerRadius={75}
                    paddingAngle={5}
                    dataKey="value"
                    label={({ name, percent }: any) => `${name} (${((percent || 0) * 100).toFixed(0)}%)`}
                    labelLine={{ stroke: '#94a3b8' }}
                  >
                    {statusPieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <RechartsTooltip
                    contentStyle={{ backgroundColor: '#1c2541', borderColor: '#334155', borderRadius: '8px', fontSize: '12px' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
