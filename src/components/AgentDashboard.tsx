import React, { useState, useEffect, useMemo } from 'react';
import { User, Complaint } from '../types';
import {
  Building2,
  RotateCcw,
  Trash2,
  Save,
  MessageSquare,
  RefreshCw,
  Sparkles,
  ArrowUpDown,
  Search,
  Clock,
  History,
  CheckCircle2,
  AlertCircle,
  Flame,
  Bot,
  ArrowRight,
  ShieldCheck,
  UserCheck,
  Send,
  Paperclip,
  FileText,
  Volume2,
} from 'lucide-react';
import {
  getSLADetails,
  generateAISmartAssist,
  generateAuditLogs,
  getPriorityLabel,
} from '../utils/itsmHelper';

interface AgentDashboardProps {
  currentUser: User;
  onUpdateDepartment?: (dept: string) => void;
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function formatTimer(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
}

const DEPARTMENTS = [
  'Finance & Payroll',
  'Technical Support',
  'Customer Care',
  'Logistics',
];

const AGENT_ID_MAP: Record<string, { agentId: string; agentName: string }> = {
  'Finance & Payroll': { agentId: '#AGT-FIN-01', agentName: 'Elena Vance' },
  'Technical Support': { agentId: '#AGT-TECH-01', agentName: 'Alex Rivera' },
  'Customer Care': { agentId: '#AGT-CARE-01', agentName: 'Sarah Jenkins' },
  'Logistics': { agentId: '#AGT-LOG-01', agentName: 'Marcus Vance' },
};

const RESOLUTION_TEMPLATES: Record<string, string[]> = {
  'Finance & Payroll': [
    'Finance team verified invoice ledger and initiated refund credit.',
    'Payroll discrepancy corrected in Q1 adjustment batch.',
    'Tax withholding deduction statement revised and emailed.',
    'Transaction verified with merchant payment gateway.',
  ],
  'Technical Support': [
    'Patched API gateway 500 error and refreshed authentication service.',
    'System diagnostic executed: bug isolated and hotfixed in v2.1.4.',
    'Cache cleared and network socket timeout limit increased.',
    'Database connection pool enlarged to resolve latency bottleneck.',
  ],
  'Customer Care': [
    'Provided enterprise SLA policy handbook and extended active warranty.',
    'Account permissions reconfigured per customer requirements.',
    'Escalated to account manager for priority customer review.',
    'Resolved general inquiry and updated customer support file.',
  ],
  Logistics: [
    'Carrier tracking manifest confirmed: replacement dispatch expedited.',
    'Warehouse dock signature checked; package re-routed to correct address.',
    'Customs clearance release documents transmitted to freight forwarder.',
    'Delivery window rescheduled with courier logistics coordinator.',
  ],
};

export const AgentDashboard: React.FC<AgentDashboardProps> = ({ currentUser, onUpdateDepartment }) => {
  const [activeDept, setActiveDept] = useState(currentUser.department || 'Finance & Payroll');
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [loading, setLoading] = useState(false);

  // Filters
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [sortFilter, setSortFilter] = useState('Newest First');
  const [searchQuery, setSearchQuery] = useState('');

  // Selected item in table
  const [selectedId, setSelectedId] = useState<number | null>(null);

  // Active tab in Action Panel: 'action' | 'audit'
  const [activeDetailTab, setActiveDetailTab] = useState<'action' | 'audit'>('action');

  // Bottom action bar states
  const [updateStatus, setUpdateStatus] = useState<string>('IN_PROGRESS');
  const [noteInput, setNoteInput] = useState<string>('');
  const [actionStatus, setActionStatus] = useState<{ text: string; color: string; isError?: boolean } | null>(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [deleteTargetComplaint, setDeleteTargetComplaint] = useState<Complaint | null>(null);

  // Load department complaints
  const loadComplaints = async () => {
    setLoading(true);
    try {
      const res = await fetch(`/api/complaints/department/${encodeURIComponent(activeDept)}`);
      if (res.ok) {
        const data: Complaint[] = await res.json();
        setComplaints(data);
        if (data.length > 0 && !selectedId) {
          setSelectedId(data[0].id);
          setUpdateStatus(data[0].status);
          setNoteInput(data[0].notes || '');
        }
      }
    } catch (err) {
      console.error('Failed to load agent complaints:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadComplaints();
  }, [activeDept]);

  const selectedComplaint = useMemo(() => {
    return complaints.find((c) => c.id === selectedId) || null;
  }, [complaints, selectedId]);

  // AI Smart Assist for the selected complaint
  const aiAssist = useMemo(() => {
    if (!selectedComplaint) return null;
    return generateAISmartAssist(selectedComplaint);
  }, [selectedComplaint]);

  // Audit logs for the selected complaint
  const auditLogs = useMemo(() => {
    if (!selectedComplaint) return [];
    return generateAuditLogs(selectedComplaint);
  }, [selectedComplaint]);

  // SLA details for the selected complaint
  const selectedSLA = useMemo(() => {
    if (!selectedComplaint) return null;
    return getSLADetails(
      selectedComplaint.priority,
      selectedComplaint.createdAt || selectedComplaint.created_at || new Date().toISOString(),
      selectedComplaint.status
    );
  }, [selectedComplaint]);

  // Update selected complaint's form values when selection changes
  const handleSelectComplaint = (complaint: Complaint) => {
    setSelectedId(complaint.id);
    setUpdateStatus(complaint.status);
    setNoteInput(complaint.notes || '');
    setActionStatus(null);
  };

  // Filter & sort logic
  const filteredData = useMemo(() => {
    const list = complaints.filter((c) => {
      const matchesStatus = statusFilter === 'ALL' || c.status.toUpperCase() === statusFilter;
      const matchesPriority = priorityFilter === 'ALL' || c.priority.toUpperCase() === priorityFilter;
      const matchesSearch =
        c.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (c.userName && c.userName.toLowerCase().includes(searchQuery.toLowerCase())) ||
        String(c.id).includes(searchQuery);

      return matchesStatus && matchesPriority && matchesSearch;
    });

    if (sortFilter === 'Oldest First') {
      return list.sort((a, b) => a.id - b.id);
    } else if (sortFilter === 'Priority (High to Low)') {
      const weight: Record<string, number> = { HIGH: 4, MEDIUM: 3, NORMAL: 2, LOW: 1 };
      return list.sort((a, b) => (weight[b.priority] || 0) - (weight[a.priority] || 0));
    } else {
      return list.sort((a, b) => b.id - a.id);
    }
  }, [complaints, statusFilter, priorityFilter, sortFilter, searchQuery]);

  // Handle Save Update (Status & Notes with Agent ID)
  const handleSaveUpdate = async () => {
    if (!selectedComplaint) {
      setActionStatus({ text: 'Please select a complaint from the table first.', color: '#ef4444' });
      return;
    }

    setIsUpdating(true);
    try {
      const agentInfo = AGENT_ID_MAP[activeDept] || {
        agentId: currentUser.agentIdCode || `#AGT-${currentUser.id}`,
        agentName: currentUser.fullName,
      };

      const res = await fetch(`/api/complaints/${selectedComplaint.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          status: updateStatus,
          notes: noteInput.trim(),
          agentId: agentInfo.agentId,
          agentName: agentInfo.agentName,
        }),
      });

      if (res.ok) {
        setActionStatus({
          text: `Ticket #${selectedComplaint.id} updated successfully by ${agentInfo.agentId} (${agentInfo.agentName})!`,
          color: '#16a34a',
        });
        loadComplaints();
      } else {
        throw new Error('Failed to update complaint in database.');
      }
    } catch (err: any) {
      setActionStatus({ text: err.message || 'Update failed.', color: '#ef4444' });
    } finally {
      setIsUpdating(false);
    }
  };

  // Handle Open Delete Modal
  const handleOpenDeleteDialog = () => {
    if (!selectedComplaint) {
      setActionStatus({ text: 'Please select a complaint to delete.', color: '#ef4444', isError: true });
      return;
    }
    setDeleteTargetComplaint(selectedComplaint);
  };

  // Handle Confirm Permanent Delete
  const handleConfirmDelete = async () => {
    if (!deleteTargetComplaint) return;

    const targetId = deleteTargetComplaint.id;
    setIsUpdating(true);
    try {
      const res = await fetch(`/api/complaints/${targetId}`, {
        method: 'DELETE',
      });

      const data = await res.json().catch(() => ({}));

      if (res.ok && data.success !== false) {
        // 1. Immediately remove ticket from active queue list
        setComplaints((prev) => prev.filter((c) => c.id !== targetId));
        // 2. Clear the active ticket details pane
        setSelectedId(null);
        setNoteInput('');
        setDeleteTargetComplaint(null);
        // 3. Display green toast
        setActionStatus({ text: 'Ticket deleted successfully.', color: '#10b981', isError: false });
        // Background sync
        loadComplaints();
      } else {
        const errorMsg = data.details || data.error || 'Failed to delete complaint from database.';
        setDeleteTargetComplaint(null);
        setActionStatus({ text: `Database Error: ${errorMsg}`, color: '#ef4444', isError: true });
      }
    } catch (err: any) {
      setDeleteTargetComplaint(null);
      setActionStatus({ text: `Database Error: ${err.message || 'Failed to delete complaint from database.'}`, color: '#ef4444', isError: true });
    } finally {
      setIsUpdating(false);
    }
  };

  const handleResetFilters = () => {
    setStatusFilter('ALL');
    setPriorityFilter('ALL');
    setSortFilter('Newest First');
    setSearchQuery('');
  };

  const quickTemplates = RESOLUTION_TEMPLATES[activeDept] || RESOLUTION_TEMPLATES['Customer Care'];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Welcome Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-[#1c2541] to-[#2b3a50] p-6 rounded-2xl border border-slate-700/80 shadow-lg">
        <div>
          <div className="flex items-center gap-3">
            <h1 id="welcomeLabel" className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Welcome, <span className="text-[#5bc0be]">{currentUser.fullName}</span>
            </h1>
            <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-[#5bc0be]/15 text-[#6fffe9] border border-[#5bc0be]/30">
              Agent ID: {AGENT_ID_MAP[activeDept]?.agentId || currentUser.agentIdCode || `#AGT-${currentUser.id}`}
            </span>
          </div>
          <p id="deptBadge" className="text-sm text-slate-300 mt-1 flex items-center gap-1.5 font-medium">
            <Building2 className="w-4 h-4 text-[#5bc0be]" />
            Active Service Desk: <span className="text-white font-semibold">{activeDept}</span>
          </p>
        </div>

        {/* Department Switcher */}
        <div className="flex items-center gap-3">
          <div className="bg-[#0b132b]/80 p-1.5 rounded-xl border border-slate-700 flex items-center gap-2">
            <span className="text-xs text-slate-400 pl-2 font-medium">Desk View:</span>
            <select
              value={activeDept}
              onChange={(e) => {
                setActiveDept(e.target.value);
                if (onUpdateDepartment) onUpdateDepartment(e.target.value);
              }}
              className="cms-input text-xs py-1.5 px-3 bg-[#1c2541] border-slate-600 font-semibold text-[#6fffe9]"
            >
              {DEPARTMENTS.map((dept) => (
                <option key={dept} value={dept}>
                  {dept}
                </option>
              ))}
            </select>
          </div>

          <div className="bg-[#0b132b]/60 px-4 py-2.5 rounded-xl border border-slate-700/60 text-right">
            <span className="text-xs text-slate-400 block font-medium">Active Queue</span>
            <span id="assignedCountLabel" className="text-lg font-bold text-[#5bc0be] font-mono">
              {complaints.length}
            </span>
          </div>
        </div>
      </div>

      {/* Filter & Controls Bar */}
      <div className="cms-card p-4 sm:p-5 border border-slate-700 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-3">
            {/* Search */}
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                placeholder="Search queue..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="cms-input text-xs py-1.5 w-48"
                style={{ paddingLeft: '2.25rem' }}
              />
            </div>

            {/* Status filter */}
            <div className="flex items-center gap-1.5">
              <span className="text-xs text-slate-400 font-medium">Status:</span>
              <select
                id="statusFilter"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="cms-input text-xs py-1.5 px-2.5"
              >
                <option value="ALL">ALL</option>
                <option value="OPEN">OPEN</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="RESOLVED">RESOLVED</option>
              </select>
            </div>

            {/* Priority filter */}
            <div className="flex items-center gap-1.5">
              <span className="text-xs text-slate-400 font-medium">Priority:</span>
              <select
                id="priorityFilter"
                value={priorityFilter}
                onChange={(e) => setPriorityFilter(e.target.value)}
                className="cms-input text-xs py-1.5 px-2.5"
              >
                <option value="ALL">ALL</option>
                <option value="HIGH">HIGH (P1)</option>
                <option value="MEDIUM">MEDIUM (P2)</option>
                <option value="LOW">LOW (P4)</option>
              </select>
            </div>

            {/* Sort order */}
            <div className="flex items-center gap-1.5">
              <ArrowUpDown className="w-3.5 h-3.5 text-slate-400" />
              <select
                id="sortFilter"
                value={sortFilter}
                onChange={(e) => setSortFilter(e.target.value)}
                className="cms-input text-xs py-1.5 px-2.5"
              >
                <option value="Newest First">Newest First</option>
                <option value="Oldest First">Oldest First</option>
                <option value="Priority (High to Low)">Priority (High to Low)</option>
              </select>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleResetFilters}
              className="cms-btn-secondary text-xs py-1.5 px-3"
              title="Reset Filters"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              Reset
            </button>
            <button
              onClick={loadComplaints}
              className="p-2 rounded-lg bg-[#1c2541] hover:bg-slate-700 text-slate-300 border border-slate-700"
              title="Refresh Table"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>
        </div>
      </div>

      {/* Main Department Queue Table with SLA Timers */}
      <div className="cms-card p-6 border border-slate-700 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Building2 className="w-4 h-4 text-[#5bc0be]" />
            Department Ticket Queue & SLA Monitor
          </h2>
          <span className="text-xs text-slate-400">
            Showing <strong className="text-white">{filteredData.length}</strong> assigned tickets
          </span>
        </div>

        <div className="overflow-x-auto max-h-96">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="sticky top-0 z-10">
              <tr className="bg-[#1c2541] text-slate-300 uppercase tracking-wider font-semibold border-b border-slate-700">
                <th className="py-3 px-4">Ticket ID</th>
                <th className="py-3 px-4">Customer</th>
                <th className="py-3 px-4">Issue Description</th>
                <th className="py-3 px-4">Priority</th>
                <th className="py-3 px-4">SLA Resolution Timer</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Internal Notes</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 text-slate-200">
              {filteredData.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-10 text-center text-slate-400">
                    No complaints currently in this queue matching criteria.
                  </td>
                </tr>
              ) : (
                filteredData.map((item) => {
                  const isSelected = selectedId === item.id;
                  const hasCustomName = item.userName && item.userName !== 'Customer' && !item.userName.startsWith('User #');
                  const sla = getSLADetails(item.priority, item.createdAt || item.created_at || new Date().toISOString(), item.status);

                  return (
                    <tr
                      key={item.id}
                      onClick={() => handleSelectComplaint(item)}
                      className={`cursor-pointer transition-colors ${
                        isSelected
                          ? 'bg-[#5bc0be]/15 border-l-4 border-l-[#5bc0be]'
                          : 'hover:bg-[#1c2541]/60'
                      }`}
                    >
                      <td className="py-3 px-4 font-mono font-bold text-[#5bc0be]">#{item.id}</td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <div className="flex items-center gap-1.5">
                          <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-mono font-bold bg-[#1c2541] text-[#6fffe9] border border-slate-700 shadow-sm">
                            #{item.userId}
                          </span>
                          {hasCustomName && (
                            <span className="text-xs text-slate-300 font-medium truncate max-w-[140px]" title={item.userName}>
                              ({item.userName})
                            </span>
                          )}
                        </div>
                      </td>
                      <td className="py-3 px-4 max-w-xs truncate text-slate-300 font-medium" title={item.description}>
                        {item.description}
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            item.priority === 'HIGH'
                              ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                              : item.priority === 'MEDIUM'
                              ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                              : 'bg-teal-500/20 text-[#6fffe9] border border-teal-500/30'
                          }`}
                        >
                          {getPriorityLabel(item.priority)}
                        </span>
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap font-mono text-[11px]">
                        <div className="flex items-center gap-1.5">
                          <span
                            className={`w-2 h-2 rounded-full ${
                              sla.color === 'emerald'
                                ? 'bg-emerald-400'
                                : sla.color === 'amber'
                                ? 'bg-amber-400'
                                : 'bg-rose-500 animate-pulse'
                            }`}
                          />
                          <span
                            className={
                              sla.color === 'emerald'
                                ? 'text-emerald-400 font-medium'
                                : sla.color === 'amber'
                                ? 'text-amber-400 font-medium'
                                : 'text-rose-400 font-bold'
                            }
                          >
                            {sla.remainingFormatted}
                          </span>
                        </div>
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            item.status === 'RESOLVED'
                              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                              : item.status === 'IN_PROGRESS'
                              ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                              : 'bg-sky-500/20 text-sky-400 border border-sky-500/30'
                          }`}
                        >
                          {item.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3 px-4 max-w-xs truncate text-slate-300 italic" title={item.notes}>
                        {item.notes || 'Awaiting Action'}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Bottom Action & ITSM Smart Assist Panel */}
      <div className="cms-card p-6 sm:p-8 border border-slate-700 shadow-2xl space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-700">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-[#5bc0be]" />
              <h3 className="text-base font-bold text-white">
                Ticket Workspace {selectedComplaint ? `— #${selectedComplaint.id}` : ''}
              </h3>
            </div>
            {/* Tab switchers */}
            <div className="flex items-center bg-[#0b132b] p-1 rounded-lg border border-slate-700 text-xs">
              <button
                type="button"
                onClick={() => setActiveDetailTab('action')}
                className={`px-3 py-1 rounded-md font-semibold transition ${
                  activeDetailTab === 'action'
                    ? 'bg-[#1c2541] text-[#6fffe9] shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Action & AI Assist
              </button>
              <button
                type="button"
                onClick={() => setActiveDetailTab('audit')}
                className={`px-3 py-1 rounded-md font-semibold flex items-center gap-1.5 transition ${
                  activeDetailTab === 'audit'
                    ? 'bg-[#1c2541] text-[#6fffe9] shadow-sm'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <History className="w-3.5 h-3.5" />
                Audit Log
              </button>
            </div>
          </div>

          {selectedComplaint && (
            <div className="flex flex-wrap items-center gap-2 text-xs">
              {/* SLA Target */}
              {selectedSLA && (
                <div className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-[#0b132b] border border-slate-700">
                  <span className="text-slate-400">SLA:</span>
                  <span
                    className={`font-mono font-bold ${
                      selectedSLA.color === 'emerald'
                        ? 'text-emerald-400'
                        : selectedSLA.color === 'amber'
                        ? 'text-amber-400'
                        : 'text-rose-400'
                    }`}
                  >
                    {selectedSLA.remainingFormatted} ({selectedSLA.slaHours}h)
                  </span>
                </div>
              )}

              <span className="text-slate-400">
                Customer: <strong className="text-white font-mono">#{selectedComplaint.userId}</strong>
              </span>
            </div>
          )}
        </div>

        {selectedComplaint ? (
          <div>
            {activeDetailTab === 'action' ? (
              <div className="space-y-5">
                {/* Active Complaint Customer Summary */}
                <div className="p-4 rounded-xl bg-[#1c2541] border border-slate-700 text-xs text-slate-200 space-y-3">
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-slate-400 text-[11px] font-semibold uppercase tracking-wider">
                      Customer Issue Description:
                    </span>
                    <span className="text-slate-400 text-[11px]">
                      Filed: {new Date(selectedComplaint.createdAt || selectedComplaint.created_at || Date.now()).toLocaleString()}
                    </span>
                  </div>
                  <p className="text-white font-medium text-sm leading-relaxed">{selectedComplaint.description}</p>

                  {/* Attached Documents in Agent Inspector */}
                  {selectedComplaint.attachments && selectedComplaint.attachments.length > 0 && (
                    <div className="pt-2 border-t border-slate-700/60">
                      <span className="text-slate-400 text-[11px] font-semibold uppercase tracking-wider block mb-2 flex items-center gap-1.5">
                        <Paperclip className="w-3.5 h-3.5 text-[#5bc0be]" />
                        Customer Attachments ({selectedComplaint.attachments.length} files):
                      </span>
                      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2">
                        {selectedComplaint.attachments.map((doc, idx) => (
                          <div
                            key={doc.id || idx}
                            className="p-2.5 rounded-lg bg-[#0b132b] border border-slate-700 flex items-center justify-between gap-2 text-xs"
                          >
                            <div className="flex items-center gap-2 min-w-0">
                              <FileText className="w-4 h-4 text-[#5bc0be] flex-shrink-0" />
                              <div className="min-w-0">
                                <span className="font-semibold text-white truncate block max-w-[130px]" title={doc.name}>
                                  {doc.name}
                                </span>
                                <span className="text-[10px] text-slate-400 font-mono">
                                  {formatFileSize(doc.size || 0)}
                                </span>
                              </div>
                            </div>
                            {doc.dataUrl && (
                              <a
                                href={doc.dataUrl}
                                download={doc.name}
                                className="text-[10px] text-[#5bc0be] hover:underline px-2 py-1 bg-[#1c2541] rounded border border-slate-700 font-semibold"
                              >
                                View
                              </a>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Customer Voice Note in Agent Inspector */}
                  {selectedComplaint.voiceNote && (
                    <div className="pt-2 border-t border-slate-700/60">
                      <div className="p-3 rounded-xl bg-[#0b132b] border border-[#5bc0be]/40 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                        <div className="flex items-center gap-2">
                          <Volume2 className="w-4 h-4 text-[#5bc0be]" />
                          <div>
                            <span className="text-xs font-bold text-white block">Customer Voice Recording</span>
                            <span className="text-[10px] text-slate-400 font-mono">
                              Duration: {formatTimer(selectedComplaint.voiceNote.durationSeconds || 0)}
                            </span>
                          </div>
                        </div>
                        <audio
                          src={selectedComplaint.voiceNote.dataUrl || selectedComplaint.voiceNote.blobUrl}
                          controls
                          className="h-8 max-w-[240px] w-full"
                        />
                      </div>
                    </div>
                  )}
                </div>

                {/* AI Smart Assist Panel */}
                {aiAssist && (
                  <div className="p-4 rounded-xl bg-gradient-to-r from-[#1c2541] to-[#162035] border border-[#5bc0be]/40 shadow-lg space-y-3">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 text-xs font-bold text-[#6fffe9]">
                        <Bot className="w-4 h-4 text-[#5bc0be]" />
                        <span>AI Smart Assist & Triage Insight</span>
                      </div>
                      <span className="text-[10px] px-2 py-0.5 rounded bg-[#5bc0be]/15 text-[#5bc0be] font-mono border border-[#5bc0be]/30">
                        {aiAssist.confidence}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                      <div className="p-3 rounded-lg bg-[#0b132b]/80 border border-slate-700/80">
                        <span className="text-slate-400 block text-[10px] uppercase font-bold mb-1">
                          Key Issue Summary
                        </span>
                        <p className="text-slate-200 leading-relaxed">{aiAssist.aiSummary}</p>
                      </div>

                      <div className="p-3 rounded-lg bg-[#0b132b]/80 border border-slate-700/80">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-slate-400 block text-[10px] uppercase font-bold">
                            Recommended Action
                          </span>
                          <span className="text-[10px] text-teal-400 font-mono">
                            Est: {aiAssist.estimatedTime}
                          </span>
                        </div>
                        <p className="text-[#6fffe9] font-medium leading-relaxed">
                          {aiAssist.recommendedAction}
                        </p>
                      </div>
                    </div>                    {/* 1-click Insert AI Recommendation button */}
                    <div className="flex justify-end pt-1">
                      <button
                        type="button"
                        onClick={() => {
                          setNoteInput(aiAssist.recommendedAction);
                          setUpdateStatus('IN_PROGRESS');
                        }}
                        className="text-xs px-3.5 py-1.5 rounded-lg bg-[#5bc0be]/15 hover:bg-[#5bc0be]/25 text-[#6fffe9] border border-[#5bc0be]/40 font-semibold flex items-center gap-1.5 transition shadow-sm"
                      >
                        <Sparkles className="w-3.5 h-3.5 text-[#5bc0be]" />
                        Insert AI Recommendation into Internal Notes
                      </button>
                    </div>
                  </div>
                )}

                {/* Quick Resolution Templates */}
                <div>
                  <span className="text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
                    <Sparkles className="w-3.5 h-3.5 text-[#5bc0be]" />
                    Standard Resolution Presets:
                  </span>
                  <div className="flex flex-wrap gap-2 pt-1">
                    {quickTemplates.map((template, idx) => (
                      <button
                        key={idx}
                        type="button"
                        onClick={() => {
                          setNoteInput(template);
                          setUpdateStatus('RESOLVED');
                        }}
                        className="text-[11px] px-2.5 py-1.5 rounded-lg bg-[#1c2541] hover:bg-slate-700 text-slate-300 border border-slate-700 transition text-left"
                      >
                        💡 {template}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Status Dropdown and Note Field */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                  <div>
                    <label className="block text-xs font-semibold text-slate-300 mb-1">Update Status</label>
                    <select
                      id="statusUpdateBox"
                      value={updateStatus}
                      onChange={(e) => setUpdateStatus(e.target.value)}
                      className="w-full cms-input text-xs py-2 font-bold text-[#6fffe9]"
                    >
                      <option value="OPEN">OPEN</option>
                      <option value="IN_PROGRESS">IN_PROGRESS</option>
                      <option value="RESOLVED">RESOLVED</option>
                    </select>
                  </div>

                  <div className="md:col-span-3">
                    <label className="block text-xs font-semibold text-slate-300 mb-1">
                      Internal Resolution Notes (Logged under {AGENT_ID_MAP[activeDept]?.agentId || currentUser.agentIdCode || `#AGT-${currentUser.id}`})
                    </label>
                    <input
                      id="noteInputField"
                      type="text"
                      value={noteInput}
                      onChange={(e) => setNoteInput(e.target.value)}
                      placeholder="Enter resolution notes, agent actions, or status comments..."
                      className="w-full cms-input text-xs py-2"
                    />
                  </div>
                </div>

                {/* Action Feedback Label / Toast Banner */}
                {actionStatus && (
                  <div
                    id="actionStatusLabel"
                    className={`text-xs font-bold p-3 rounded-lg border flex items-center gap-2 animate-fadeIn ${
                      actionStatus.isError || actionStatus.color === '#ef4444'
                        ? 'bg-rose-500/15 border-rose-500/40 text-rose-300'
                        : 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300'
                    }`}
                  >
                    {actionStatus.isError || actionStatus.color === '#ef4444' ? (
                      <AlertCircle className="w-4 h-4 text-rose-400 shrink-0" />
                    ) : (
                      <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    )}
                    <span>{actionStatus.text}</span>
                  </div>
                )}

                {/* Action Buttons */}
                <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
                  <button
                    type="button"
                    id="btn-delete-complaint"
                    onClick={handleOpenDeleteDialog}
                    disabled={isUpdating}
                    className="cms-btn-secondary bg-rose-950/40 text-rose-300 border border-rose-800/40 hover:bg-rose-900/60 text-xs py-2 px-4 transition"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    Delete Complaint
                  </button>

                  <button
                    type="button"
                    id="btn-save-update-ticket"
                    onClick={handleSaveUpdate}
                    disabled={isUpdating}
                    className="cms-btn-primary text-xs py-2.5 px-6 shadow-lg flex items-center gap-2"
                  >
                    <Save className="w-4 h-4" />
                    {isUpdating ? 'Saving...' : 'Save & Update Ticket'}
                  </button>
                </div>
              </div>
            ) : (
              /* Activity Audit Log Tab */
              <div className="space-y-4 animate-fadeIn">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">
                    Ticket Activity & Event Timeline
                  </span>
                  <span className="text-[11px] text-slate-400 font-mono">Ticket #{selectedComplaint.id}</span>
                </div>

                <div className="space-y-3 border-l-2 border-slate-700 pl-4 ml-2">
                  {auditLogs.map((log) => (
                    <div key={log.id} className="relative group">
                      {/* Timeline dot */}
                      <span className="absolute -left-[23px] top-1.5 w-3 h-3 rounded-full ring-4 ring-[#0b132b] bg-[#5bc0be]" />

                      <div className="p-3 rounded-xl border space-y-1 bg-[#1c2541] border-slate-700/80">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-bold text-white flex items-center gap-1.5">
                            {log.action.includes('Created') && <Send className="w-3 h-3 text-sky-400" />}
                            {log.action.includes('Triaged') && <Sparkles className="w-3 h-3 text-[#5bc0be]" />}
                            {log.action.includes('Assigned') && <UserCheck className="w-3 h-3 text-teal-400" />}
                            {log.action.includes('Resolved') && <CheckCircle2 className="w-3 h-3 text-emerald-400" />}
                            <span>{log.action}</span>
                          </span>
                          <span className="text-[11px] text-slate-400 font-mono">{log.timestamp}</span>
                        </div>
                        <p className="text-xs text-slate-300">{log.details}</p>
                        <div className="text-[10px] text-slate-400 font-mono pt-1">
                          Actor: <span className="font-bold text-[#6fffe9]">{log.actor}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="p-8 text-center text-slate-400 text-sm">
            Please click a row in the table above to view details, update status, and attach notes.
          </div>
        )}
      </div>

      {/* Confirmation Modal for Permanent Ticket Deletion */}
      {deleteTargetComplaint && (
        <div
          id="delete-confirmation-modal"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fadeIn"
        >
          <div className="cms-card max-w-md w-full p-6 border border-rose-500/40 shadow-2xl space-y-4">
            <div className="flex items-center gap-3 text-rose-400">
              <div className="p-2.5 rounded-xl bg-rose-500/20 border border-rose-500/30">
                <Trash2 className="w-6 h-6 text-rose-400" />
              </div>
              <div>
                <h3 className="text-base font-bold text-white">Delete Ticket Confirmation</h3>
                <p className="text-xs text-rose-300 font-mono">Permanent Database Action</p>
              </div>
            </div>

            <p className="text-sm text-slate-200 leading-relaxed">
              Are you sure you want to permanently delete ticket{' '}
              <strong className="text-white font-mono">#{deleteTargetComplaint.id}</strong>?
            </p>

            <div className="p-3 rounded-lg bg-rose-950/30 border border-rose-900/50 text-xs text-rose-200 font-mono">
              SQL Target: <span className="text-rose-400">DELETE FROM complaints WHERE id = {deleteTargetComplaint.id};</span>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                id="btn-cancel-delete"
                onClick={() => setDeleteTargetComplaint(null)}
                disabled={isUpdating}
                className="cms-btn-secondary text-xs py-2 px-4"
              >
                Cancel
              </button>
              <button
                type="button"
                id="btn-confirm-delete"
                onClick={handleConfirmDelete}
                disabled={isUpdating}
                className="cms-btn-secondary bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs py-2 px-5 border-transparent shadow-lg flex items-center gap-2"
              >
                <Trash2 className="w-4 h-4" />
                {isUpdating ? 'Deleting...' : 'Permanently Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
