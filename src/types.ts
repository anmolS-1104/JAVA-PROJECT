export type UserRole = 'CUSTOMER' | 'AGENT' | 'ADMIN';

export interface User {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  role: UserRole;
  department?: string;
  agentIdCode?: string;
}

export type PriorityLevel = 'LOW' | 'NORMAL' | 'MEDIUM' | 'HIGH';
export type ComplaintStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED';
export type SentimentType = 'URGENT' | 'FRUSTRATED' | 'NEUTRAL';

export interface AttachedDocument {
  id: string;
  name: string;
  size: number;
  type: string;
  dataUrl?: string;
}

export interface VoiceNote {
  durationSeconds: number;
  blobUrl?: string;
  dataUrl?: string;
  recordedAt: string;
}

export interface AuditLogItem {
  id: string;
  timestamp: string;
  actor: string;
  action: string;
  details: string;
  badgeColor?: string;
}

export interface Complaint {
  id: number;
  userId: number;
  customerId?: number;
  userName?: string;
  userEmail?: string;
  description: string;
  category: string;
  department: string;
  priority: PriorityLevel;
  status: ComplaintStatus;
  notes: string;
  agent_notes?: string;
  agentId?: string;
  agent_id?: string;
  agentName?: string;
  agent_name?: string;
  createdAt: string;
  created_at?: string;
  resolutionTimeHours?: number;
  sentiment?: SentimentType;
  aiSummary?: string;
  recommendedAction?: string;
  slaDeadline?: string;
  slaHours?: number;
  auditLogs?: AuditLogItem[];
  attachments?: AttachedDocument[];
  voiceNote?: VoiceNote;
}

export interface AnalyticsData {
  total: number;
  pending: number;
  inProgress: number;
  resolved: number;
  finance: number;
  technical: number;
  logistics: number;
  customerCare: number;
}

export interface AIClassificationResult {
  department: string;
  category: string;
  priority: PriorityLevel;
  priorityLabel?: string; // e.g. "P1 Critical", "P2 High", "P3 Medium", "P4 Low"
  sentiment: SentimentType;
  confidence: number;
  source: string;
  summary?: string;
  recommendedAction?: string;
  quickFix?: {
    title: string;
    description: string;
    actionLabel?: string;
  } | null;
}

export interface ModerationResult {
  isValid: boolean;
  errorMessage?: string;
  flaggedReason?: string;
}

