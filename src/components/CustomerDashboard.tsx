import React, { useState, useEffect, useMemo, useRef } from 'react';
import { User, Complaint, AIClassificationResult, SentimentType, AttachedDocument, VoiceNote } from '../types';
import {
  Send,
  Sparkles,
  Search,
  CheckCircle2,
  Clock,
  AlertCircle,
  Building2,
  Tag,
  BarChart2,
  RefreshCw,
  Info,
  ShieldAlert,
  Flame,
  Frown,
  Smile,
  Zap,
  X,
  ArrowRight,
  Paperclip,
  Mic,
  Square,
  Trash2,
  FileText,
  Volume2,
} from 'lucide-react';
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip as RechartsTooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import {
  validateComplaintModeration,
  detectSentiment,
  getQuickFixSuggestion,
  getPriorityLabel,
  getSLADetails,
} from '../utils/itsmHelper';

interface CustomerDashboardProps {
  currentUser: User;
  onOpenAuth: () => void;
}

const STATUS_COLORS: Record<string, string> = {
  OPEN: '#38bdf8', // Light Blue
  IN_PROGRESS: '#f59e0b', // Amber
  RESOLVED: '#10b981', // Emerald
};

const PRIORITY_BADGES: Record<string, { bg: string; text: string; border: string; label: string }> = {
  HIGH: { bg: 'bg-rose-500/20', text: 'text-rose-400', border: 'border-rose-500/40', label: 'P1 Critical' },
  MEDIUM: { bg: 'bg-amber-500/20', text: 'text-amber-400', border: 'border-amber-500/40', label: 'P2 High' },
  NORMAL: { bg: 'bg-teal-500/20', text: 'text-[#6fffe9]', border: 'border-teal-500/40', label: 'P3 Medium' },
  LOW: { bg: 'bg-slate-500/20', text: 'text-slate-400', border: 'border-slate-500/40', label: 'P4 Low' },
};

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

export const CustomerDashboard: React.FC<CustomerDashboardProps> = ({ currentUser }) => {
  const [complaintText, setComplaintText] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ text: string; type: 'success' | 'error' | 'info' | 'moderation' } | null>(null);
  const [isModerationFailed, setIsModerationFailed] = useState(false);

  // File Attachments State (Max 10 files)
  const [attachedFiles, setAttachedFiles] = useState<AttachedDocument[]>([]);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Voice Note State (Max 2 minutes = 120s)
  const [isRecording, setIsRecording] = useState(false);
  const [recordingSecondsLeft, setRecordingSecondsLeft] = useState(120);
  const [recordedVoiceNote, setRecordedVoiceNote] = useState<VoiceNote | null>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioStreamRef = useRef<MediaStream | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const countdownIntervalRef = useRef<any>(null);

  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [isLoadingList, setIsLoadingList] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedComplaint, setSelectedComplaint] = useState<Complaint | null>(null);

  // Live classification preview & Sentiment
  const [aiPreview, setAiPreview] = useState<AIClassificationResult | null>(null);
  const [isClassifying, setIsClassifying] = useState(false);
  const [liveSentiment, setLiveSentiment] = useState<SentimentType>('NEUTRAL');
  const [quickFixDismissed, setQuickFixDismissed] = useState(false);

  // Quick fix suggestion based on current typed text
  const quickFix = useMemo(() => {
    if (quickFixDismissed || !complaintText.trim() || complaintText.length < 6) return null;
    return getQuickFixSuggestion(complaintText);
  }, [complaintText, quickFixDismissed]);

  // Load customer complaints on mount
  const loadComplaints = async () => {
    setIsLoadingList(true);
    try {
      const res = await fetch(`/api/complaints/user/${currentUser.id}`);
      if (res.ok) {
        const data = await res.json();
        setComplaints(data);
      }
    } catch (err) {
      console.error('Error fetching complaints:', err);
    } finally {
      setIsLoadingList(false);
    }
  };

  useEffect(() => {
    loadComplaints();
  }, [currentUser.id]);

  // Cleanup audio tracks and interval on unmount
  useEffect(() => {
    return () => {
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
      if (audioStreamRef.current) {
        audioStreamRef.current.getTracks().forEach((t) => t.stop());
      }
    };
  }, []);

  // Handle Document File Selection (Max 10 files)
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const selectedList = Array.from(e.target.files);

    if (attachedFiles.length + selectedList.length > 10) {
      setStatusMessage({
        text: 'Upload limit reached: A maximum of 10 documents can be attached to a single complaint.',
        type: 'error',
      });
    }

    const availableSlots = Math.max(0, 10 - attachedFiles.length);
    const filesToProcess = selectedList.slice(0, availableSlots);

    if (filesToProcess.length === 0) {
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    const promises = filesToProcess.map((file) => {
      return new Promise<AttachedDocument>((resolve) => {
        const reader = new FileReader();
        reader.onload = () => {
          resolve({
            id: 'doc_' + Math.random().toString(36).substring(2, 9),
            name: file.name,
            size: file.size,
            type: file.type || 'application/octet-stream',
            dataUrl: reader.result as string,
          });
        };
        reader.onerror = () => {
          resolve({
            id: 'doc_' + Math.random().toString(36).substring(2, 9),
            name: file.name,
            size: file.size,
            type: file.type || 'application/octet-stream',
          });
        };
        reader.readAsDataURL(file);
      });
    });

    Promise.all(promises).then((newDocs) => {
      setAttachedFiles((prev) => [...prev, ...newDocs]);
      if (fileInputRef.current) fileInputRef.current.value = '';
    });
  };

  const handleRemoveFile = (docId: string) => {
    setAttachedFiles((prev) => prev.filter((d) => d.id !== docId));
  };

  // Voice Note Recording Management (Max 2 minutes = 120s)
  const startRecording = async () => {
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        setStatusMessage({
          text: 'Microphone access is not supported by your browser.',
          type: 'error',
        });
        return;
      }

      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioStreamRef.current = stream;
      audioChunksRef.current = [];

      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        const mimeType = mediaRecorder.mimeType || 'audio/webm';
        const audioBlob = new Blob(audioChunksRef.current, { type: mimeType });
        const blobUrl = URL.createObjectURL(audioBlob);

        const reader = new FileReader();
        reader.onloadend = () => {
          setRecordedVoiceNote({
            durationSeconds: 120 - recordingSecondsLeft,
            blobUrl,
            dataUrl: reader.result as string,
            recordedAt: new Date().toISOString(),
          });
        };
        reader.readAsDataURL(audioBlob);

        if (audioStreamRef.current) {
          audioStreamRef.current.getTracks().forEach((track) => track.stop());
          audioStreamRef.current = null;
        }
      };

      mediaRecorder.start();
      setIsRecording(true);
      setRecordingSecondsLeft(120);

      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = setInterval(() => {
        setRecordingSecondsLeft((prev) => {
          if (prev <= 1) {
            stopRecording();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err: any) {
      console.error('Microphone access error:', err);
      setStatusMessage({
        text: 'Microphone permission was denied or unavailable. Please grant microphone access in browser settings.',
        type: 'error',
      });
    }
  };

  const stopRecording = () => {
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
    }
    if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
      mediaRecorderRef.current.stop();
    }
    setIsRecording(false);
  };

  const deleteVoiceNote = () => {
    if (recordedVoiceNote?.blobUrl) {
      URL.revokeObjectURL(recordedVoiceNote.blobUrl);
    }
    setRecordedVoiceNote(null);
    setRecordingSecondsLeft(120);
  };

  // Debounced real-time classification & sentiment preview
  useEffect(() => {
    if (!complaintText.trim() || complaintText.length < 5) {
      setAiPreview(null);
      setLiveSentiment('NEUTRAL');
      setIsModerationFailed(false);
      return;
    }

    // Check live sentiment
    const detected = detectSentiment(complaintText);
    setLiveSentiment(detected);

    const timer = setTimeout(async () => {
      setIsClassifying(true);
      try {
        const res = await fetch('/api/classify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ text: complaintText }),
        });
        if (res.ok) {
          const data = await res.json();
          setAiPreview({
            ...data,
            sentiment: detected,
            priorityLabel: getPriorityLabel(data.priority),
          });
          setIsModerationFailed(false);
        } else if (res.status === 422) {
          // Moderation triggered
          setIsModerationFailed(true);
        }
      } catch (e) {
        // Fallback rule-based preview
      } finally {
        setIsClassifying(false);
      }
    }, 350);

    return () => clearTimeout(timer);
  }, [complaintText]);

  // Handle complaint submission with Anti-Abuse Moderation Guardrail, Attachments & Voice Note
  const handleSubmitComplaint = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusMessage(null);

    // 1. Text Moderation & Anti-Abuse Guardrail check
    const moderation = validateComplaintModeration(complaintText);
    if (!moderation.isValid) {
      setIsModerationFailed(true);
      setStatusMessage({
        text: moderation.errorMessage || 'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
        type: 'moderation',
      });
      return;
    }

    setIsModerationFailed(false);
    setIsSubmitting(true);
    setStatusMessage({ text: 'AI is processing and categorizing your complaint...', type: 'info' });

    try {
      const res = await fetch('/api/complaints', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: currentUser.id,
          userName: currentUser.fullName,
          userEmail: currentUser.email,
          description: complaintText.trim(),
          department: aiPreview?.department,
          category: aiPreview?.category,
          priority: aiPreview?.priority,
          attachments: attachedFiles,
          voiceNote: recordedVoiceNote,
        }),
      });

      const data = await res.json();
      if (!res.ok) {
        if (res.status === 422) {
          setIsModerationFailed(true);
          throw new Error(data.error || 'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.');
        }
        throw new Error(data.error || 'Failed to submit complaint');
      }

      setStatusMessage({
        text: `Complaint #${data.complaint.id} submitted to [${data.complaint.department}] (${getPriorityLabel(data.complaint.priority)})!`,
        type: 'success',
      });
      setComplaintText('');
      setAttachedFiles([]);
      setRecordedVoiceNote(null);
      setAiPreview(null);
      setLiveSentiment('NEUTRAL');
      setQuickFixDismissed(false);
      loadComplaints();
    } catch (err: any) {
      setStatusMessage({ text: err.message || 'Submission failed.', type: isModerationFailed ? 'moderation' : 'error' });
    } finally {
      setIsSubmitting(false);
    }
  };

  // Filter complaints
  const filteredComplaints = useMemo(() => {
    return complaints.filter((c) => {
      const matchesSearch =
        c.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.department.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
        String(c.id).includes(searchQuery);

      const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [complaints, searchQuery, statusFilter]);

  // Analytics data for mini charts
  const statusChartData = useMemo(() => {
    const counts = { OPEN: 0, IN_PROGRESS: 0, RESOLVED: 0 };
    complaints.forEach((c) => {
      if (counts[c.status] !== undefined) counts[c.status]++;
    });
    return [
      { name: 'OPEN', value: counts.OPEN, color: STATUS_COLORS.OPEN },
      { name: 'IN PROGRESS', value: counts.IN_PROGRESS, color: STATUS_COLORS.IN_PROGRESS },
      { name: 'RESOLVED', value: counts.RESOLVED, color: STATUS_COLORS.RESOLVED },
    ].filter((item) => item.value > 0);
  }, [complaints]);

  const deptChartData = useMemo(() => {
    const depts: Record<string, number> = {};
    complaints.forEach((c) => {
      const d = c.department.split('&')[0].trim();
      depts[d] = (depts[d] || 0) + 1;
    });
    return Object.entries(depts).map(([name, count]) => ({ name, count }));
  }, [complaints]);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-[#1c2541] to-[#2b3a50] p-6 rounded-2xl border border-slate-700/80 shadow-lg">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            Welcome, <span className="text-[#5bc0be]">{currentUser.fullName}</span>
          </h1>
          <p className="text-sm text-slate-300 mt-1">
            Submit service requests and track real-time resolution SLA status with automated ITSM triage.
          </p>
        </div>
        <div className="flex items-center gap-3 bg-[#0b132b]/60 px-4 py-2.5 rounded-xl border border-slate-700/60 self-start md:self-auto">
          <div className="text-right">
            <span className="text-xs text-slate-400 block font-medium">Customer ID</span>
            <span className="text-base font-bold text-[#6fffe9] font-mono">#{currentUser.id}</span>
          </div>
          <div className="h-8 w-px bg-slate-700 mx-1" />
          <div className="text-left">
            <span className="text-xs text-slate-400 block font-medium">Total Filed</span>
            <span className="text-base font-bold text-white font-mono">{complaints.length}</span>
          </div>
        </div>
      </div>

      {/* Main Grid: Submit Form + Live AI Engine Preview */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Complaint Submission Box */}
        <div className="lg:col-span-7 cms-card p-6 sm:p-8 border border-slate-700 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-lg bg-[#1c2541] text-[#5bc0be]">
                <Send className="w-5 h-5" />
              </div>
              <h2 className="text-lg font-bold text-white">Log a Service Request</h2>
            </div>
            <span className="text-xs px-2.5 py-1 rounded-full bg-[#5bc0be]/10 text-[#5bc0be] border border-[#5bc0be]/30 flex items-center gap-1 font-semibold">
              <Sparkles className="w-3.5 h-3.5" /> AI Triage & Guardrails
            </span>
          </div>

          {/* Self-Service Suggestion Card (If common issue detected) */}
          {quickFix && (
            <div className="p-4 rounded-xl bg-gradient-to-r from-teal-950/70 to-[#1c2541] border border-teal-500/50 shadow-lg animate-fadeIn flex items-start justify-between gap-3">
              <div className="space-y-1">
                <div className="flex items-center gap-1.5 font-bold text-xs text-teal-300">
                  <Zap className="w-4 h-4 text-[#6fffe9] flex-shrink-0" />
                  <span>{quickFix.title}</span>
                  <span className="text-[10px] px-1.5 py-0.5 rounded bg-teal-500/20 text-[#6fffe9] uppercase font-semibold">
                    Suggested Quick Fix
                  </span>
                </div>
                <p className="text-xs text-slate-200 leading-relaxed pr-2">
                  {quickFix.description}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setQuickFixDismissed(true)}
                className="text-slate-400 hover:text-white text-xs p-1"
                title="Dismiss suggestion"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          )}

          <form onSubmit={handleSubmitComplaint} className="space-y-4">
            <div>
              <div className="flex items-center justify-between mb-2">
                <label htmlFor="complaint-input" className="block text-xs font-semibold text-slate-300">
                  Describe the issue or request in detail
                </label>
                {/* Live Sentiment Badge */}
                {complaintText.trim().length >= 6 && (
                  <span
                    className={`inline-flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full border ${
                      liveSentiment === 'URGENT'
                        ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                        : liveSentiment === 'FRUSTRATED'
                        ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                        : 'bg-teal-500/20 text-teal-300 border-teal-500/40'
                    }`}
                  >
                    {liveSentiment === 'URGENT' && <Flame className="w-3 h-3 text-rose-400" />}
                    {liveSentiment === 'FRUSTRATED' && <Frown className="w-3 h-3 text-amber-400" />}
                    {liveSentiment === 'NEUTRAL' && <Smile className="w-3 h-3 text-teal-400" />}
                    Sentiment: {liveSentiment}
                  </span>
                )}
              </div>

              <textarea
                id="complaint-input"
                rows={4}
                required
                value={complaintText}
                onChange={(e) => {
                  setComplaintText(e.target.value);
                  if (isModerationFailed) {
                    setIsModerationFailed(false);
                    setStatusMessage(null);
                  }
                }}
                placeholder="Describe your issue here (e.g. 'I was double charged on my invoice #4921 and require a refund' or 'Login portal crashes with 500 error when submitting form')..."
                className={`w-full cms-input resize-none text-sm leading-relaxed transition-all duration-200 ${
                  isModerationFailed
                    ? 'border-rose-500 ring-2 ring-rose-500/40 bg-rose-950/20 text-white focus:border-rose-400'
                    : ''
                }`}
              />

              {/* Action Toolbar directly below Textarea */}
              <div className="flex flex-wrap items-center justify-between gap-2.5 pt-2">
                <div className="flex items-center gap-2">
                  {/* Hidden file input */}
                  <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handleFileSelect}
                    multiple
                    accept=".pdf,.doc,.docx,.xlsx,.txt,.csv,.png,.jpg,.jpeg,.zip"
                    className="hidden"
                    id="file-attachment-input"
                  />
                  {/* 📎 Attach Documents button */}
                  <button
                    type="button"
                    id="btn-attach-documents"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={attachedFiles.length >= 10 || isSubmitting}
                    className="text-xs px-3 py-1.5 rounded-lg bg-[#1c2541] hover:bg-slate-700/80 text-slate-200 border border-slate-700 font-semibold flex items-center gap-1.5 transition disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                  >
                    <Paperclip className="w-3.5 h-3.5 text-[#5bc0be]" />
                    <span>Attach Documents</span>
                    {attachedFiles.length > 0 && (
                      <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-[#5bc0be]/20 text-[#6fffe9] font-mono">
                        {attachedFiles.length}/10
                      </span>
                    )}
                  </button>

                  {/* 🎙️ Record Voice Note button */}
                  {!isRecording && !recordedVoiceNote && (
                    <button
                      type="button"
                      id="btn-record-voice"
                      onClick={startRecording}
                      disabled={isSubmitting}
                      className="text-xs px-3 py-1.5 rounded-lg bg-[#1c2541] hover:bg-slate-700/80 text-slate-200 border border-slate-700 font-semibold flex items-center gap-1.5 transition disabled:opacity-50 shadow-sm"
                    >
                      <Mic className="w-3.5 h-3.5 text-rose-400" />
                      <span>Record Voice Note</span>
                    </button>
                  )}
                </div>

                {/* Counter Badge */}
                {attachedFiles.length > 0 && (
                  <span className="text-[11px] font-semibold text-slate-400">
                    Attached: <strong className="text-[#6fffe9]">{attachedFiles.length}</strong> / 10 files
                  </span>
                )}
              </div>

              {/* Live Voice Recording Module */}
              {isRecording && (
                <div className="mt-2.5 p-3.5 rounded-xl bg-rose-950/40 border border-rose-500/60 flex items-center justify-between gap-3 animate-fadeIn">
                  <div className="flex items-center gap-3">
                    <span className="relative flex h-3.5 w-3.5">
                      <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-rose-400 opacity-75"></span>
                      <span className="relative inline-flex rounded-full h-3.5 w-3.5 bg-rose-500"></span>
                    </span>
                    <div>
                      <span className="text-xs font-bold text-rose-200 block">Recording Voice Note...</span>
                      <span className="text-[10px] text-rose-300/80 font-mono">
                        Auto-stops at 00:00 (Max 2 mins)
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="font-mono text-xs font-bold text-rose-300 bg-rose-900/50 px-2.5 py-1 rounded-lg border border-rose-700/50">
                      {formatTimer(recordingSecondsLeft)}
                    </span>
                    <button
                      type="button"
                      id="btn-stop-recording"
                      onClick={stopRecording}
                      className="text-xs px-3 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-bold flex items-center gap-1.5 transition shadow-md"
                    >
                      <Square className="w-3.5 h-3.5 fill-current" />
                      <span>Stop Recording</span>
                    </button>
                  </div>
                </div>
              )}

              {/* Recorded Voice Note Player */}
              {recordedVoiceNote && !isRecording && (
                <div className="mt-2.5 p-3.5 rounded-xl bg-[#1c2541] border border-[#5bc0be]/40 flex flex-col sm:flex-row sm:items-center justify-between gap-3 animate-fadeIn">
                  <div className="flex items-center gap-2.5">
                    <div className="p-2 rounded-lg bg-[#0b132b] text-[#5bc0be]">
                      <Volume2 className="w-4 h-4" />
                    </div>
                    <div>
                      <span className="text-xs font-bold text-white block">Voice Note Recorded</span>
                      <span className="text-[10px] text-slate-400 font-mono">
                        Duration: {formatTimer(recordedVoiceNote.durationSeconds)} • Audio ready for review
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 w-full sm:w-auto">
                    <audio
                      src={recordedVoiceNote.blobUrl || recordedVoiceNote.dataUrl}
                      controls
                      className="h-8 max-w-[200px] w-full"
                    />
                    <button
                      type="button"
                      id="btn-delete-voice-note"
                      onClick={deleteVoiceNote}
                      className="text-xs p-2 rounded-lg bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 border border-rose-800/40 flex items-center gap-1 font-semibold transition"
                      title="Delete Voice Note"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span className="hidden sm:inline">Delete Note</span>
                    </button>
                  </div>
                </div>
              )}

              {/* Attached Document Chips / Cards List (Max 10 files) */}
              {attachedFiles.length > 0 && (
                <div className="mt-2.5 space-y-2 animate-fadeIn">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    {attachedFiles.map((doc) => (
                      <div
                        key={doc.id}
                        className="p-2.5 rounded-lg bg-[#1c2541] border border-slate-700/80 flex items-center justify-between gap-2 text-xs"
                      >
                        <div className="flex items-center gap-2 min-w-0">
                          <FileText className="w-4 h-4 text-[#5bc0be] flex-shrink-0" />
                          <div className="min-w-0">
                            <span className="font-semibold text-white truncate block max-w-[170px]" title={doc.name}>
                              {doc.name}
                            </span>
                            <span className="text-[10px] text-slate-400 font-mono">
                              {formatFileSize(doc.size)}
                            </span>
                          </div>
                        </div>
                        <button
                          type="button"
                          onClick={() => handleRemoveFile(doc.id)}
                          className="text-slate-400 hover:text-rose-400 p-1 transition"
                          title="Remove file"
                        >
                          <X className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Live Triage Preview with Department and Priority Badges */}
            {aiPreview && !isModerationFailed && (
              <div className="p-3.5 rounded-xl bg-[#1c2541] border border-[#5bc0be]/40 text-xs space-y-2.5 animate-fadeIn">
                <div className="flex items-center justify-between font-semibold text-slate-200">
                  <span className="flex items-center gap-1.5 text-[#6fffe9]">
                    <Sparkles className="w-4 h-4 text-[#5bc0be]" />
                    Live Triage Prediction
                  </span>
                  <span className="text-[10px] text-slate-400 font-mono">{aiPreview.source}</span>
                </div>

                <div className="grid grid-cols-3 gap-2 text-center">
                  <div className="bg-[#0b132b] p-2.5 rounded-lg border border-slate-700">
                    <span className="text-[10px] text-slate-400 block uppercase tracking-wider">Predicted Dept</span>
                    <span className="font-bold text-white text-xs truncate block mt-0.5">
                      {aiPreview.department}
                    </span>
                  </div>
                  <div className="bg-[#0b132b] p-2.5 rounded-lg border border-slate-700">
                    <span className="text-[10px] text-slate-400 block uppercase tracking-wider">Category</span>
                    <span className="font-bold text-[#5bc0be] text-xs truncate block mt-0.5">
                      {aiPreview.category}
                    </span>
                  </div>
                  <div className="bg-[#0b132b] p-2.5 rounded-lg border border-slate-700">
                    <span className="text-[10px] text-slate-400 block uppercase tracking-wider">Priority Tier</span>
                    <span
                      className={`font-bold text-xs block mt-0.5 ${
                        aiPreview.priority === 'HIGH'
                          ? 'text-rose-400'
                          : aiPreview.priority === 'MEDIUM'
                          ? 'text-amber-400'
                          : 'text-[#6fffe9]'
                      }`}
                    >
                      {getPriorityLabel(aiPreview.priority)}
                    </span>
                  </div>
                </div>
              </div>
            )}

            {/* Anti-Abuse Warning Banner / Status Feedback */}
            {statusMessage && (
              <div
                id="status-label"
                className={`p-3.5 rounded-xl text-xs font-semibold flex items-start gap-2.5 border shadow-md animate-fadeIn ${
                  statusMessage.type === 'moderation'
                    ? 'bg-rose-950/80 text-rose-200 border-rose-500/80'
                    : statusMessage.type === 'error'
                    ? 'bg-rose-500/15 text-rose-300 border-rose-500/40'
                    : statusMessage.type === 'success'
                    ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/40'
                    : 'bg-sky-500/15 text-sky-300 border-sky-500/40'
                }`}
              >
                {statusMessage.type === 'moderation' && (
                  <ShieldAlert className="w-5 h-5 text-rose-400 flex-shrink-0 mt-0.5" />
                )}
                {statusMessage.type === 'error' && (
                  <AlertCircle className="w-4 h-4 text-rose-400 flex-shrink-0 mt-0.5" />
                )}
                {statusMessage.type === 'success' && (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 flex-shrink-0 mt-0.5" />
                )}
                {statusMessage.type === 'info' && (
                  <RefreshCw className="w-4 h-4 text-sky-400 flex-shrink-0 animate-spin mt-0.5" />
                )}
                <div className="flex-1 leading-relaxed">
                  {statusMessage.type === 'moderation' && (
                    <strong className="block text-rose-300 font-bold mb-0.5">
                      Content Moderation Guardrail Triggered
                    </strong>
                  )}
                  <span>{statusMessage.text}</span>
                </div>
              </div>
            )}

            <div className="flex items-center justify-between pt-2">
              <div className="text-xs text-slate-400 flex items-center gap-1.5">
                <Info className="w-3.5 h-3.5 text-slate-400" />
                <span>ITSM router assigns target SLA & dedicated agent desk.</span>
              </div>
              <button
                type="submit"
                id="btn-submit-complaint"
                disabled={isSubmitting || !complaintText.trim()}
                className="cms-btn-primary py-2.5 px-6 shadow-lg disabled:opacity-50 disabled:cursor-not-allowed font-semibold text-xs flex items-center gap-2"
              >
                {isSubmitting ? (
                  <>
                    <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                    Routing...
                  </>
                ) : (
                  <>
                    <Send className="w-3.5 h-3.5" />
                    Submit Complaint
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        {/* Right Column: Visual Summary & Mini Analytics */}
        <div className="lg:col-span-5 space-y-6">
          <div className="cms-card p-6 border border-slate-700 flex flex-col justify-between min-h-[340px]">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <BarChart2 className="w-4 h-4 text-[#5bc0be]" />
                Your Ticket Overview
              </h3>
              <span className="text-xs text-slate-400 font-mono">{complaints.length} Total</span>
            </div>

            {complaints.length === 0 ? (
              <div className="flex-1 flex flex-col items-center justify-center text-center p-6 text-slate-400">
                <CheckCircle2 className="w-10 h-10 text-slate-600 mb-2" />
                <p className="text-sm font-medium">No complaints filed yet.</p>
                <p className="text-xs text-slate-500 mt-1">Submit your first complaint using the form on the left.</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-4 my-auto">
                <div className="h-44">
                  <p className="text-[11px] font-semibold text-center text-slate-300 mb-1">Status Ratio</p>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={statusChartData}
                        innerRadius={28}
                        outerRadius={48}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {statusChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <RechartsTooltip
                        contentStyle={{ backgroundColor: '#1c2541', borderColor: '#334155', borderRadius: '8px', fontSize: '11px' }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="h-44">
                  <p className="text-[11px] font-semibold text-center text-slate-300 mb-1">By Department</p>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={deptChartData} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                      <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 9 }} />
                      <YAxis allowDecimals={false} tick={{ fill: '#94a3b8', fontSize: 9 }} />
                      <RechartsTooltip
                        contentStyle={{ backgroundColor: '#1c2541', borderColor: '#334155', borderRadius: '8px', fontSize: '11px' }}
                      />
                      <Bar dataKey="count" fill="#5bc0be" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            )}

            <div className="grid grid-cols-3 gap-2 pt-3 border-t border-slate-700/60 text-center">
              <div className="p-2 rounded-lg bg-[#1c2541]">
                <span className="text-[10px] text-slate-400 block">Open</span>
                <span className="font-bold text-sky-400 text-sm">
                  {complaints.filter((c) => c.status === 'OPEN').length}
                </span>
              </div>
              <div className="p-2 rounded-lg bg-[#1c2541]">
                <span className="text-[10px] text-slate-400 block">In Progress</span>
                <span className="font-bold text-amber-400 text-sm">
                  {complaints.filter((c) => c.status === 'IN_PROGRESS').length}
                </span>
              </div>
              <div className="p-2 rounded-lg bg-[#1c2541]">
                <span className="text-[10px] text-slate-400 block">Resolved</span>
                <span className="font-bold text-emerald-400 text-sm">
                  {complaints.filter((c) => c.status === 'RESOLVED').length}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* History Table Section */}
      <div className="cms-card p-6 sm:p-8 border border-slate-700 shadow-xl space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-700">
          <div>
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Clock className="w-5 h-5 text-[#5bc0be]" />
              Complaint History & ITSM Status
            </h2>
            <p className="text-xs text-slate-400">Track real-time progress, SLA deadlines, and assigned agent notes</p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {/* Search Input with proper left padding */}
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                placeholder="Search complaints..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="cms-input text-xs py-1.5 w-44 sm:w-56"
                style={{ paddingLeft: '2.25rem' }}
              />
            </div>

            {/* Status Filter */}
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="cms-input text-xs py-1.5"
            >
              <option value="ALL">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
            </select>

            <button
              onClick={loadComplaints}
              title="Refresh Complaints"
              className="p-2 rounded-lg bg-[#1c2541] hover:bg-slate-700 text-slate-300 border border-slate-700"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isLoadingList ? 'animate-spin' : ''}`} />
            </button>
          </div>
        </div>

        {/* Complaints Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-[#1c2541] text-slate-300 font-semibold border-b border-slate-700">
                <th className="py-3 px-4 rounded-l-lg">ID</th>
                <th className="py-3 px-4">Complaint Description</th>
                <th className="py-3 px-4">Department</th>
                <th className="py-3 px-4">Category</th>
                <th className="py-3 px-4">Assigned Agent</th>
                <th className="py-3 px-4">Priority / SLA</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 rounded-r-lg">Agent Notes / Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800 font-normal text-slate-200">
              {filteredComplaints.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-8 text-center text-slate-400">
                    No complaints matching the selected filters.
                  </td>
                </tr>
              ) : (
                filteredComplaints.map((c) => {
                  const pBadge = PRIORITY_BADGES[c.priority] || PRIORITY_BADGES.NORMAL;
                  const hasAgent = !!(c.agentId || c.agent_id);
                  const displayAgentId = c.agentId || c.agent_id;
                  const displayAgentName = c.agentName || c.agent_name;
                  const sla = getSLADetails(c.priority, c.createdAt || c.created_at || new Date().toISOString(), c.status);

                  return (
                    <tr
                      key={c.id}
                      onClick={() => setSelectedComplaint(c)}
                      className="hover:bg-[#1c2541]/70 transition-colors cursor-pointer"
                    >
                      <td className="py-3.5 px-4 font-mono font-bold text-[#5bc0be]">#{c.id}</td>
                      <td className="py-3.5 px-4 max-w-xs truncate font-medium text-white" title={c.description}>
                        {c.description}
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <span className="inline-flex items-center gap-1 text-slate-300">
                          <Building2 className="w-3.5 h-3.5 text-slate-400" />
                          {c.department}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap text-slate-400">
                        <span className="inline-flex items-center gap-1">
                          <Tag className="w-3 h-3" />
                          {c.category}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        {hasAgent ? (
                          <div className="flex items-center gap-1.5">
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-mono font-bold bg-[#1c2541] text-[#6fffe9] border border-slate-700 shadow-sm">
                              {displayAgentId}
                            </span>
                            {displayAgentName && (
                              <span className="text-xs text-slate-300 font-medium">
                                ({displayAgentName})
                              </span>
                            )}
                          </div>
                        ) : (
                          <span className="text-[11px] text-slate-400 italic">
                            Pending Assignment
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <div className="space-y-1">
                          <span
                            className={`inline-block px-2 py-0.5 rounded-full text-[10px] font-bold uppercase border ${pBadge.bg} ${pBadge.text} ${pBadge.border}`}
                          >
                            {pBadge.label}
                          </span>
                          <div className="text-[10px] flex items-center gap-1 font-mono">
                            <span
                              className={`w-1.5 h-1.5 rounded-full ${
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
                                  ? 'text-emerald-400'
                                  : sla.color === 'amber'
                                  ? 'text-amber-400'
                                  : 'text-rose-400 font-bold'
                              }
                            >
                              {sla.remainingFormatted}
                            </span>
                          </div>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            c.status === 'RESOLVED'
                              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                              : c.status === 'IN_PROGRESS'
                              ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                              : 'bg-sky-500/20 text-sky-400 border border-sky-500/30'
                          }`}
                        >
                          {c.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 max-w-xs truncate text-slate-300 italic" title={c.notes}>
                        {c.notes || 'Awaiting Agent Assignment'}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Selected Complaint Detail Modal */}
      {selectedComplaint && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm">
          <div className="cms-card max-w-lg w-full p-6 border border-slate-700 space-y-4 shadow-2xl animate-fadeIn">
            <div className="flex items-center justify-between pb-3 border-b border-slate-700">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono font-bold text-[#5bc0be]">
                  Complaint #{selectedComplaint.id}
                </span>
                <span
                  className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    selectedComplaint.status === 'RESOLVED'
                      ? 'bg-emerald-500/20 text-emerald-400'
                      : selectedComplaint.status === 'IN_PROGRESS'
                      ? 'bg-amber-500/20 text-amber-400'
                      : 'bg-sky-500/20 text-sky-400'
                  }`}
                >
                  {selectedComplaint.status}
                </span>
              </div>
              <button
                onClick={() => setSelectedComplaint(null)}
                className="text-slate-400 hover:text-white text-sm font-bold px-2 py-1"
              >
                ✕
              </button>
            </div>

            <div>
              <span className="text-xs font-semibold text-slate-400 block mb-1">Description</span>
              <div className="p-3 rounded-lg bg-[#1c2541] text-sm text-white leading-relaxed">
                {selectedComplaint.description}
              </div>
            </div>

            {/* Attached Documents in Modal */}
            {selectedComplaint.attachments && selectedComplaint.attachments.length > 0 && (
              <div>
                <span className="text-xs font-semibold text-slate-400 block mb-1.5 flex items-center gap-1.5">
                  <Paperclip className="w-3.5 h-3.5 text-[#5bc0be]" />
                  Attached Documents ({selectedComplaint.attachments.length})
                </span>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {selectedComplaint.attachments.map((doc, idx) => (
                    <div
                      key={doc.id || idx}
                      className="p-2 rounded-lg bg-[#1c2541] border border-slate-700/80 flex items-center justify-between gap-2 text-xs"
                    >
                      <div className="flex items-center gap-2 min-w-0">
                        <FileText className="w-4 h-4 text-[#5bc0be] flex-shrink-0" />
                        <div className="min-w-0">
                          <span className="font-semibold text-white truncate block" title={doc.name}>
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
                          className="text-[10px] text-[#5bc0be] hover:underline px-2 py-1 bg-[#0b132b] rounded border border-slate-700 font-semibold"
                        >
                          Download
                        </a>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Voice Note in Modal */}
            {selectedComplaint.voiceNote && (
              <div className="p-3 rounded-xl bg-[#1c2541] border border-[#5bc0be]/40 space-y-2">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-white flex items-center gap-1.5">
                    <Volume2 className="w-4 h-4 text-[#5bc0be]" />
                    Customer Voice Note
                  </span>
                  <span className="text-[10px] text-slate-400 font-mono">
                    {formatTimer(selectedComplaint.voiceNote.durationSeconds || 0)}
                  </span>
                </div>
                <audio
                  src={selectedComplaint.voiceNote.dataUrl || selectedComplaint.voiceNote.blobUrl}
                  controls
                  className="w-full h-8"
                />
              </div>
            )}

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="p-2.5 rounded-lg bg-[#1c2541]">
                <span className="text-slate-400 block">Department</span>
                <span className="font-semibold text-white">{selectedComplaint.department}</span>
              </div>
              <div className="p-2.5 rounded-lg bg-[#1c2541]">
                <span className="text-slate-400 block">Priority & SLA</span>
                <span className="font-semibold text-white">
                  {getPriorityLabel(selectedComplaint.priority)} (
                  {selectedComplaint.priority === 'HIGH' ? '4h SLA' : selectedComplaint.priority === 'MEDIUM' ? '24h SLA' : '72h SLA'})
                </span>
              </div>
            </div>

            {/* Assigned Agent Card */}
            <div className="p-2.5 rounded-lg bg-[#1c2541] flex items-center justify-between text-xs">
              <div>
                <span className="text-slate-400 block text-[11px]">Assigned Support Agent</span>
                <span className="font-semibold text-white">
                  {selectedComplaint.agentName || selectedComplaint.agent_name || 'Pending Assignment'}
                </span>
              </div>
              {(selectedComplaint.agentId || selectedComplaint.agent_id) && (
                <span className="font-mono font-bold px-2 py-0.5 rounded bg-[#0b132b] text-[#6fffe9] border border-slate-700">
                  {selectedComplaint.agentId || selectedComplaint.agent_id}
                </span>
              )}
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-semibold text-slate-400 block">Agent Action & Notes</span>
                {(selectedComplaint.agentId || selectedComplaint.agent_id) && (
                  <span className="text-[10px] font-mono text-[#5bc0be]">
                    Logged by {selectedComplaint.agentId || selectedComplaint.agent_id}
                  </span>
                )}
              </div>
              <div className="p-3 rounded-lg bg-[#1c2541] border border-slate-700/80 text-xs text-slate-200">
                {selectedComplaint.notes || 'Under review by assigned department specialist.'}
              </div>
            </div>

            <div className="pt-2 text-right">
              <button
                onClick={() => setSelectedComplaint(null)}
                className="cms-btn-secondary text-xs py-2 px-4"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
