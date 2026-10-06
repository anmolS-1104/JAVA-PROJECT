import {
  PriorityLevel,
  ComplaintStatus,
  SentimentType,
  ModerationResult,
  AuditLogItem,
  Complaint,
} from '../types';

// Banned inappropriate words, offensive slurs, jokes, and informal slang
const INAPPROPRIATE_TERMS = [
  // Profanities & explicit terms
  'damn', 'hell', 'crap', 'shit', 'fuck', 'bitch', 'asshole', 'bastard', 'idiot', 'moron', 'dumbass', 'stfu', 'wtf',
  'dick', 'cock', 'pussy', 'slut', 'whore', 'fag', 'nigger', 'nigga', 'retard',
  
  // Memes, jokes & informal slang
  'lol', 'lmao', 'lmfao', 'rofl', 'haha', 'hahaha', 'hehe', 'hehehe', 'xd', 'xdd',
  'bruh', 'bro', 'wassup', 'wazzup', 'skibidi', 'rizz', 'gyatt', 'yeet', 'noob', 'git gud',
  'ur mom', 'your mom', 'yo mama', 'deez nuts', 'ligma', 'sugoma',
  'prank', 'just kidding', 'jk', 'troll', 'trolling', 'rickroll', 'meme',
  'poop', 'pee', 'fart', 'bozo', 'cap', 'no cap', 'sus', 'sussy',
  'blah blah', 'bla bla', 'yada yada', 'test test', 'testing 123', 'asdf', 'qwerty',
];

/**
 * Validates whether the text contains inappropriate slang, jokes, profanities, or spam.
 */
export function validateComplaintModeration(text: string): ModerationResult {
  const trimmed = text.trim();

  if (!trimmed) {
    return {
      isValid: false,
      errorMessage: 'Please enter a description for your complaint.',
      flaggedReason: 'EMPTY_TEXT',
    };
  }

  if (trimmed.length < 10) {
    return {
      isValid: false,
      errorMessage: 'Submission Blocked: Description is too short. Please provide a detailed, genuine issue description (at least 10 characters).',
      flaggedReason: 'TOO_SHORT',
    };
  }

  const lower = trimmed.toLowerCase();

  // 1. Check for whole words or matching patterns
  for (const term of INAPPROPRIATE_TERMS) {
    // Regex for word boundary or isolated phrase
    const regex = new RegExp(`(^|[^a-zA-Z0-9])${term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}([^a-zA-Z0-9]|$)`, 'i');
    if (regex.test(lower)) {
      return {
        isValid: false,
        errorMessage:
          'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
        flaggedReason: `FLAGGED_TERM: "${term}"`,
      };
    }
  }

  // 2. Check for keyboard mash / gibberish (e.g. "asdfghjk", "zzzzzzzz", "qwertyuiop")
  if (/(.)\1{4,}/.test(lower)) {
    return {
      isValid: false,
      errorMessage:
        'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
      flaggedReason: 'REPETITIVE_CHARACTERS',
    };
  }

  // Check vowel ratio in long continuous words
  const words = lower.split(/\s+/);
  for (const word of words) {
    if (word.length > 8 && !/[aeiouy]/i.test(word)) {
      return {
        isValid: false,
        errorMessage:
          'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
        flaggedReason: 'GIBBERISH_WORD',
      };
    }
  }

  return { isValid: true };
}

/**
 * Extracts emotional sentiment from issue text
 */
export function detectSentiment(text: string): SentimentType {
  const lower = text.toLowerCase();

  const urgentWords = [
    'urgent', 'emergency', 'asap', 'immediately', 'critical', 'outage', 'downtime',
    'production down', 'blocked', 'cannot access', "can't access", 'unauthorized',
    'lost money', 'fraud', 'deadline today', 'high priority',
  ];

  const frustratedWords = [
    'angry', 'terrible', 'worst', 'unacceptable', 'ridiculous', 'frustrated',
    'disappointed', 'fed up', 'broken again', 'annoyed', 'useless', 'horrible',
    'multiple times', 'waiting for days', 'ignored', 'no response', 'ruined',
  ];

  if (urgentWords.some((w) => lower.includes(w))) {
    return 'URGENT';
  }

  if (frustratedWords.some((w) => lower.includes(w))) {
    return 'FRUSTRATED';
  }

  return 'NEUTRAL';
}

/**
 * Detects whether a dismissible Quick-Fix self-service suggestion card applies
 */
export function getQuickFixSuggestion(text: string): {
  title: string;
  description: string;
  actionLabel: string;
} | null {
  const lower = text.toLowerCase();

  // 1. Password Reset / Login Lockout
  if (
    lower.includes('password') ||
    lower.includes('forgot password') ||
    lower.includes('reset password') ||
    lower.includes('locked out') ||
    lower.includes('cannot login') ||
    lower.includes("can't login")
  ) {
    return {
      title: '🔐 Self-Service Password Reset',
      description:
        'You can instantly reset your credentials using the "Forgot Password" self-service tool or request an SMS/Email 2FA verification OTP to regain access immediately without waiting for ticket triage.',
      actionLabel: 'Use Quick Reset Tool',
    };
  }

  // 2. Billing / Invoice Refund / Double Charge
  if (
    lower.includes('invoice') ||
    lower.includes('refund') ||
    lower.includes('double charge') ||
    lower.includes('charged twice') ||
    lower.includes('overcharge') ||
    lower.includes('receipt')
  ) {
    return {
      title: '💳 Automated Billing & Refund Ledger',
      description:
        'Duplicate or disputed charges can be reviewed automatically in your Account Billing Statements. Verified pending holds are routinely credited back within 3–5 banking business days.',
      actionLabel: 'View Billing Ledger',
    };
  }

  // 3. 500 Error / Crash / Network Failure
  if (
    lower.includes('500') ||
    lower.includes('crash') ||
    lower.includes('internal server error') ||
    lower.includes('gateway timeout') ||
    lower.includes('404')
  ) {
    return {
      title: '⚡ Technical Quick Diagnostic',
      description:
        'Try performing a hard refresh (Ctrl+Shift+R / Cmd+Shift+R) and clearing cookies. Check if your network proxy or browser security extension is blocking REST endpoints.',
      actionLabel: 'Run System Diagnostic',
    };
  }

  // 4. Logistics / Shipment Tracking
  if (
    lower.includes('track') ||
    lower.includes('shipment') ||
    lower.includes('package') ||
    lower.includes('delivery') ||
    lower.includes('courier') ||
    lower.includes('where is my order')
  ) {
    return {
      title: '📦 Real-Time Carrier GPS Tracking',
      description:
        'Track live courier delivery dispatch status with your carrier tracking manifest code. Delivery windows are updated in real-time by warehouse dock dispatchers.',
      actionLabel: 'Open Carrier Tracker',
    };
  }

  return null;
}

/**
 * Returns formatted priority label (P1 Critical, P2 High, P3 Medium, P4 Low)
 */
export function getPriorityLabel(priority: PriorityLevel): string {
  switch (priority) {
    case 'HIGH':
      return 'P1 Critical';
    case 'MEDIUM':
      return 'P2 High';
    case 'NORMAL':
      return 'P3 Medium';
    case 'LOW':
      return 'P4 Low';
    default:
      return 'P3 Medium';
  }
}

/**
 * Calculates SLA hours, countdown, color, and breach status
 */
export function getSLADetails(
  priority: PriorityLevel,
  createdAtStr: string,
  status: ComplaintStatus
) {
  // P1 High: 4h SLA, P2 Medium: 24h SLA, P3 Normal/Low: 72h SLA
  let slaTotalHours = 72;
  if (priority === 'HIGH') slaTotalHours = 4;
  else if (priority === 'MEDIUM') slaTotalHours = 24;

  const createdTime = new Date(createdAtStr).getTime() || Date.now();
  const deadlineTime = createdTime + slaTotalHours * 60 * 60 * 1000;
  const now = Date.now();
  const diffMs = deadlineTime - now;

  if (status === 'RESOLVED') {
    return {
      slaHours: slaTotalHours,
      deadlineTime: new Date(deadlineTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isBreached: false,
      isResolved: true,
      color: 'emerald',
      label: `Resolved (${slaTotalHours}h SLA Met)`,
      remainingFormatted: '✓ Met SLA',
    };
  }

  if (diffMs <= 0) {
    const overdueMinutes = Math.abs(Math.floor(diffMs / (1000 * 60)));
    const overdueHours = Math.floor(overdueMinutes / 60);
    const mins = overdueMinutes % 60;
    const overdueText = overdueHours > 0 ? `${overdueHours}h ${mins}m` : `${mins}m`;

    return {
      slaHours: slaTotalHours,
      deadlineTime: new Date(deadlineTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isBreached: true,
      isResolved: false,
      color: 'rose',
      label: `SLA Breached (+${overdueText})`,
      remainingFormatted: `Breached (+${overdueText})`,
    };
  }

  const remainingMinutes = Math.floor(diffMs / (1000 * 60));
  const remainingHours = Math.floor(remainingMinutes / 60);
  const mins = remainingMinutes % 60;

  // Warning when less than 25% or less than 2 hours remaining
  const isWarning = remainingHours < 2 || diffMs < (slaTotalHours * 60 * 60 * 1000 * 0.3);

  return {
    slaHours: slaTotalHours,
    deadlineTime: new Date(deadlineTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    isBreached: false,
    isResolved: false,
    color: isWarning ? 'amber' : 'emerald',
    label: `SLA: ${remainingHours}h ${mins}m left`,
    remainingFormatted: `${remainingHours}h ${mins}m`,
  };
}

/**
 * Generates an AI summary and recommended action for Agent Smart Assist
 */
export function generateAISmartAssist(complaint: Complaint): {
  aiSummary: string;
  recommendedAction: string;
  suggestedNote: string;
  confidence: string;
  estimatedTime: string;
} {
  const desc = complaint.description;
  const dept = complaint.department;
  const lower = desc.toLowerCase();

  let summary = `Customer reported issue in ${dept} regarding ticket specifications.`;
  let recommendedAction = `Verify ticket details, review customer account history, and execute standard ${dept} operational procedures.`;
  let suggestedNote = `Reviewed customer inquiry in ${dept}. Action taken per standard SLA protocol.`;
  let confidence = '94% Confidence';
  let estimatedTime = '< 30 mins';

  if (lower.includes('invoice') || lower.includes('refund') || lower.includes('charge') || lower.includes('payment') || dept.includes('Finance')) {
    summary = `Customer requested billing verification and possible financial refund or ledger adjustment.`;
    recommendedAction = `Verify payment gateway ledger for duplicate transaction and initiate refund credit or updated tax statement.`;
    suggestedNote = `Verified transaction records in payment ledger. Initiated refund credit and dispatched revised billing confirmation to customer.`;
    confidence = '98% Confidence';
    estimatedTime = '15 mins';
  } else if (lower.includes('login') || lower.includes('500') || lower.includes('error') || lower.includes('crash') || lower.includes('bug') || dept.includes('Technical')) {
    summary = `System failure or authentication issue affecting application accessibility.`;
    recommendedAction = `Inspect API telemetry logs, invalidate stale token session cache, and confirm database pool connection health.`;
    suggestedNote = `Investigated system diagnostic telemetry. Isolated service latency, refreshed authorization token cache, and confirmed service restoration.`;
    confidence = '96% Confidence';
    estimatedTime = '20 mins';
  } else if (lower.includes('delivery') || lower.includes('shipment') || lower.includes('package') || lower.includes('courier') || dept.includes('Logistics')) {
    summary = `Shipment or order dispatch inquiry regarding courier delivery tracking.`;
    recommendedAction = `Cross-reference warehouse carrier manifest with dispatch tracking code and request carrier expedite status.`;
    suggestedNote = `Confirmed carrier tracking manifest with warehouse logistics desk. Expedited delivery window and updated customer status file.`;
    confidence = '95% Confidence';
    estimatedTime = '45 mins';
  } else {
    summary = `Customer requesting general support and service guidance.`;
    recommendedAction = `Consult customer account history, provide enterprise SLA policy handbook, and update support file.`;
    suggestedNote = `Contacted customer with resolution instructions and updated account documentation.`;
    confidence = '92% Confidence';
    estimatedTime = '25 mins';
  }

  return {
    aiSummary: summary,
    recommendedAction,
    suggestedNote,
    confidence,
    estimatedTime,
  };
}

/**
 * Generates ITSM lifecycle audit logs for a complaint
 */
export function generateAuditLogs(complaint: Complaint): AuditLogItem[] {
  const createdDate = new Date(complaint.createdAt || complaint.created_at || Date.now());
  const createdFormatted = createdDate.toLocaleString([], {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const triagedDate = new Date(createdDate.getTime() + 60 * 1000);
  const triagedFormatted = triagedDate.toLocaleString([], {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const logs: AuditLogItem[] = [
    {
      id: 'log-1',
      timestamp: createdFormatted,
      actor: complaint.userName || `Customer #${complaint.userId}`,
      action: 'Ticket Created',
      details: `New complaint logged via customer portal and entered initial intake queue.`,
      badgeColor: 'sky',
    },
    {
      id: 'log-2',
      timestamp: triagedFormatted,
      actor: 'ITSM Auto-Triage',
      action: 'Automated Routing',
      details: `Classified to [${complaint.department}] as ${getPriorityLabel(complaint.priority)}. SLA timer initialized.`,
      badgeColor: 'teal',
    },
  ];

  if (complaint.agentId || complaint.agent_id || complaint.status !== 'OPEN') {
    const agentName = complaint.agentName || complaint.agent_name || 'Department Specialist';
    const agentCode = complaint.agentId || complaint.agent_id || '#AGT-DESK';
    const updatedDate = new Date(createdDate.getTime() + 15 * 60 * 1000);
    const updatedFormatted = updatedDate.toLocaleString([], {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });

    logs.push({
      id: 'log-3',
      timestamp: updatedFormatted,
      actor: `${agentCode} (${agentName})`,
      action: complaint.status === 'RESOLVED' ? 'Ticket Resolved' : 'Assigned & In Progress',
      details: `Status set to ${complaint.status}. Agent Notes: "${complaint.notes || complaint.agent_notes || 'Under review'}"`,
      badgeColor: complaint.status === 'RESOLVED' ? 'emerald' : 'amber',
    });
  }

  return logs;
}

