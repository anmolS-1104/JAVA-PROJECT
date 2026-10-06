import express from 'express';
import cors from 'cors';
import path from 'path';
import fs from 'fs';
import { createServer as createViteServer } from 'vite';
import { GoogleGenAI } from '@google/genai';
import {
  getDbPool,
  initDatabase,
  testDbConnection,
  executeQuery,
  executeMutation,
  getDbConnectionStatus,
} from './server/db.js';

// Safe root directory resolution supporting both ESM (tsx dev) and CommonJS bundled production
const getSafeRootDir = (): string => {
  try {
    if (typeof process !== 'undefined' && process.cwd && typeof process.cwd === 'function') {
      const cwd = process.cwd();
      if (cwd && typeof cwd === 'string') return cwd;
    }
  } catch {
    // ignore
  }
  try {
    if (typeof __dirname !== 'undefined' && __dirname && typeof __dirname === 'string') {
      return __dirname;
    }
  } catch {
    // ignore
  }
  return '.';
};

const rootDir = getSafeRootDir();

// Safe storage initialization for uploads / temporary assets
try {
  const uploadDir = path.join(rootDir || process.cwd() || '.', 'uploads');
  if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
  }
} catch (err: any) {
  // Graceful fallback for read-only environments
}

// Interface for API response compatibility
export interface FormattedComplaint {
  id: number;
  userId: number;
  customerId: number;
  userName?: string;
  userEmail?: string;
  description: string;
  category: string;
  department: string;
  priority: 'LOW' | 'NORMAL' | 'MEDIUM' | 'HIGH';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED';
  notes: string;
  agent_notes: string;
  agentId?: string;
  agent_id?: string;
  agentName?: string;
  agent_name?: string;
  createdAt: string;
  created_at: string;
  resolutionTimeHours?: number;
  attachments?: any[];
  voiceNote?: any;
}

// Department to Agent ID & Name lookup
export const DEPT_AGENT_LOOKUP: Record<string, { agentId: string; agentName: string }> = {
  'Finance & Payroll': { agentId: '#AGT-FIN-01', agentName: 'Elena Vance' },
  'Finance': { agentId: '#AGT-FIN-01', agentName: 'Elena Vance' },
  'Technical Support': { agentId: '#AGT-TECH-01', agentName: 'Alex Rivera' },
  'Technical': { agentId: '#AGT-TECH-01', agentName: 'Alex Rivera' },
  'Customer Care': { agentId: '#AGT-CARE-01', agentName: 'Sarah Jenkins' },
  'Care': { agentId: '#AGT-CARE-01', agentName: 'Sarah Jenkins' },
  'Logistics Desk': { agentId: '#AGT-LOG-01', agentName: 'Marcus Vance' },
  'Logistics': { agentId: '#AGT-LOG-01', agentName: 'Marcus Vance' },
};

// Utility to format DB rows to unified frontend interface
function formatComplaintRow(row: any): FormattedComplaint {
  const notesText = row.agent_notes || row.notes || 'Awaiting Agent Assignment';
  const createdAtIso = row.created_at
    ? new Date(row.created_at).toISOString()
    : new Date().toISOString();

  const uid = Number(row.user_id || row.customer_id || row.userId || 1);
  const dept = row.department || 'Customer Care';
  const deptInfo = DEPT_AGENT_LOOKUP[dept] || DEPT_AGENT_LOOKUP['Customer Care'];

  // Resolve agentId and agentName
  let finalAgentId = row.agent_id || row.agentId || null;
  let finalAgentName = row.agent_name || row.agentName || null;

  // If status is IN_PROGRESS or RESOLVED, or if notes have been entered and not assigned yet, fallback to department agent
  if (!finalAgentId && (row.status === 'IN_PROGRESS' || row.status === 'RESOLVED' || (notesText && notesText !== 'Awaiting Agent Assignment'))) {
    finalAgentId = deptInfo.agentId;
    finalAgentName = deptInfo.agentName;
  }

  // Parse attachments
  let parsedAttachments: any[] | undefined = undefined;
  if (row.attachments) {
    if (typeof row.attachments === 'string') {
      try {
        parsedAttachments = JSON.parse(row.attachments);
      } catch {
        parsedAttachments = undefined;
      }
    } else if (Array.isArray(row.attachments)) {
      parsedAttachments = row.attachments;
    }
  }

  // Parse voiceNote
  let parsedVoiceNote: any = undefined;
  const rawVoice = row.voice_note || row.voiceNote;
  if (rawVoice) {
    if (typeof rawVoice === 'string') {
      try {
        parsedVoiceNote = JSON.parse(rawVoice);
      } catch {
        parsedVoiceNote = undefined;
      }
    } else if (typeof rawVoice === 'object') {
      parsedVoiceNote = rawVoice;
    }
  }

  return {
    id: Number(row.id),
    userId: uid,
    customerId: uid,
    userName: row.user_name || row.userName || row.name || 'Customer',
    userEmail: row.user_email || row.userEmail || row.email || 'customer@example.com',
    description: row.description || '',
    category: row.category || 'General Inquiry',
    department: dept,
    priority: (row.priority || 'NORMAL').toUpperCase() as any,
    status: (row.status || 'OPEN').toUpperCase() as any,
    notes: notesText,
    agent_notes: notesText,
    agentId: finalAgentId || undefined,
    agent_id: finalAgentId || undefined,
    agentName: finalAgentName || undefined,
    agent_name: finalAgentName || undefined,
    createdAt: createdAtIso,
    created_at: createdAtIso,
    resolutionTimeHours: row.resolution_time_hours || undefined,
    attachments: parsedAttachments,
    voiceNote: parsedVoiceNote,
  };
}

// Gemini API lazy initializer (fails gracefully if no key provided)
let aiClient: GoogleGenAI | null = null;
function getAIClient(): GoogleGenAI | null {
  if (!aiClient && process.env.GEMINI_API_KEY) {
    aiClient = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
  }
  return aiClient;
}

// Inappropriate words, offensive slurs, jokes, and informal slang filter
const INAPPROPRIATE_TERMS = [
  'damn', 'hell', 'crap', 'shit', 'fuck', 'bitch', 'asshole', 'bastard', 'idiot', 'moron', 'dumbass', 'stfu', 'wtf',
  'dick', 'cock', 'pussy', 'slut', 'whore', 'fag', 'nigger', 'nigga', 'retard',
  'lol', 'lmao', 'lmfao', 'rofl', 'haha', 'hahaha', 'hehe', 'hehehe', 'xd', 'xdd',
  'bruh', 'bro', 'wassup', 'wazzup', 'skibidi', 'rizz', 'gyatt', 'yeet', 'noob', 'git gud',
  'ur mom', 'your mom', 'yo mama', 'deez nuts', 'ligma', 'sugoma',
  'prank', 'just kidding', 'jk', 'troll', 'trolling', 'rickroll', 'meme',
  'poop', 'pee', 'fart', 'bozo', 'cap', 'no cap', 'sus', 'sussy',
  'blah blah', 'bla bla', 'yada yada', 'test test', 'testing 123', 'asdf', 'qwerty',
];

function checkModeration(text: string): { isValid: boolean; error?: string } {
  const trimmed = text.trim();
  if (!trimmed || trimmed.length < 5) {
    return { isValid: false, error: 'Complaint description is too short.' };
  }
  const lower = trimmed.toLowerCase();

  for (const term of INAPPROPRIATE_TERMS) {
    const regex = new RegExp(`(^|[^a-zA-Z0-9])${term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}([^a-zA-Z0-9]|$)`, 'i');
    if (regex.test(lower)) {
      return {
        isValid: false,
        error: 'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
      };
    }
  }

  if (/(.)\1{4,}/.test(lower)) {
    return {
      isValid: false,
      error: 'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
    };
  }

  return { isValid: true };
}

// AI / Rule-based Triage Engine
async function classifyComplaint(description: string): Promise<{
  department: string;
  category: string;
  priority: 'LOW' | 'NORMAL' | 'MEDIUM' | 'HIGH';
  confidence: number;
  source: string;
}> {
  const ai = getAIClient();
  if (ai) {
    try {
      const response = await ai.models.generateContent({
        model: 'gemini-2.5-flash',
        contents: `You are an enterprise AI Complaint Routing Engine. Analyze the following customer issue and respond ONLY with a raw valid JSON object without markdown fences or code blocks:
{
  "department": "Finance & Payroll" | "Technical Support" | "Customer Care" | "Logistics",
  "category": "Billing" | "Technical" | "Logistics" | "General Inquiry" | "Account Access",
  "priority": "LOW" | "NORMAL" | "MEDIUM" | "HIGH",
  "confidence": 0.95
}

Customer Issue: "${description}"`,
      });

      const text = response.text?.trim() || '';
      const cleanJson = text.replace(/```json/gi, '').replace(/```/g, '').trim();
      const parsed = JSON.parse(cleanJson);

      return {
        department: parsed.department || 'Customer Care',
        category: parsed.category || 'General Inquiry',
        priority: parsed.priority || 'NORMAL',
        confidence: parsed.confidence || 0.95,
        source: 'Gemini 2.5 Flash (AI Routing)',
      };
    } catch (e) {
      console.warn('[Gemini Triage] Falling back to keyword classification engine:', (e as any).message);
    }
  }

  // Fast Rule-Based Fallback Engine
  const lower = description.toLowerCase();

  // 1. Finance & Payroll / Billing
  if (
    lower.includes('bill') ||
    lower.includes('charge') ||
    lower.includes('invoice') ||
    lower.includes('refund') ||
    lower.includes('payment') ||
    lower.includes('salary') ||
    lower.includes('payroll') ||
    lower.includes('overcharge') ||
    lower.includes('double charge') ||
    lower.includes('tax') ||
    lower.includes('deduction') ||
    lower.includes('bank') ||
    lower.includes('credit card') ||
    lower.includes('fee')
  ) {
    const isHigh = lower.includes('double charge') || lower.includes('unauthorized') || lower.includes('fraud') || lower.includes('immediate');
    return {
      department: 'Finance & Payroll',
      category: 'Billing',
      priority: isHigh ? 'HIGH' : 'MEDIUM',
      confidence: 0.92,
      source: 'Keyword Engine (Finance Rules)',
    };
  }

  // 2. Technical Support
  if (
    lower.includes('bug') ||
    lower.includes('virus') ||
    lower.includes('crash') ||
    lower.includes('error') ||
    lower.includes('login') ||
    lower.includes('system') ||
    lower.includes('computer') ||
    lower.includes('network') ||
    lower.includes('server') ||
    lower.includes('software') ||
    lower.includes('not working') ||
    lower.includes('broken') ||
    lower.includes('database') ||
    lower.includes('failed') ||
    lower.includes('500') ||
    lower.includes('404')
  ) {
    const isHigh = lower.includes('crash') || lower.includes('down') || lower.includes('virus') || lower.includes('critical') || lower.includes('failed');
    return {
      department: 'Technical Support',
      category: 'Technical',
      priority: isHigh ? 'HIGH' : 'MEDIUM',
      confidence: 0.9,
      source: 'Keyword Engine (Tech Rules)',
    };
  }

  // 3. Logistics
  if (
    lower.includes('deliver') ||
    lower.includes('package') ||
    lower.includes('shipment') ||
    lower.includes('courier') ||
    lower.includes('dispatch') ||
    lower.includes('tracking') ||
    lower.includes('arrived') ||
    lower.includes('late') ||
    lower.includes('order') ||
    lower.includes('parcel') ||
    lower.includes('logistics') ||
    lower.includes('warehouse') ||
    lower.includes('freight')
  ) {
    const isHigh = lower.includes('lost') || lower.includes('damaged') || lower.includes('urgent');
    return {
      department: 'Logistics',
      category: 'Logistics',
      priority: isHigh ? 'HIGH' : 'MEDIUM',
      confidence: 0.85,
      source: 'Keyword Engine (Logistics Rules)',
    };
  }

  // 4. Default: Customer Care
  const isHigh = lower.includes('urgent') || lower.includes('asap') || lower.includes('emergency');
  return {
    department: 'Customer Care',
    category: 'General Inquiry',
    priority: isHigh ? 'HIGH' : 'LOW',
    confidence: 0.8,
    source: 'Keyword Engine (Default Route)',
  };
}

async function startServer() {
  const app = express();
  const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;

  app.use(cors());
  app.use(express.json({ limit: '50mb' }));
  app.use(express.urlencoded({ extended: true, limit: '50mb' }));

  // Attempt database connection & schema bootstrapping
  initDatabase().then((success) => {
    if (success) {
      console.log('[Database] MySQL initialized and connected.');
    } else {
      console.warn('[Database] MySQL connection pending or failed. Status available via /api/db/status');
    }
  });

  // Health Endpoint
  app.get('/api/health', async (req, res) => {
    const dbStatus = await testDbConnection();
    res.json({
      status: 'ok',
      service: 'Complaint Management Service',
      database: dbStatus,
    });
  });

  // Database Connection Status & Diagnostic Endpoint
  app.get('/api/db/status', async (req, res) => {
    const status = await testDbConnection();
    res.json(status);
  });

  // -------------------------------------------------------------
  // PRESET AGENT ACCOUNTS (Not stored in users table)
  // -------------------------------------------------------------
  const PRESET_AGENTS: Record<string, { id: number; name: string; email: string; password: string; role: 'AGENT'; department: string; agentIdCode: string }> = {
    'finance@agent.company.com': {
      id: 9001,
      name: 'Elena Vance',
      email: 'finance@agent.company.com',
      password: 'finance123',
      role: 'AGENT',
      department: 'Finance & Payroll',
      agentIdCode: '#AGT-FIN-01',
    },
    'tech@agent.company.com': {
      id: 9002,
      name: 'Alex Rivera',
      email: 'tech@agent.company.com',
      password: 'tech123',
      role: 'AGENT',
      department: 'Technical Support',
      agentIdCode: '#AGT-TECH-01',
    },
    'care@agent.company.com': {
      id: 9003,
      name: 'Sarah Jenkins',
      email: 'care@agent.company.com',
      password: 'care123',
      role: 'AGENT',
      department: 'Customer Care',
      agentIdCode: '#AGT-CARE-01',
    },
    'logistics@agent.company.com': {
      id: 9004,
      name: 'Marcus Vance',
      email: 'logistics@agent.company.com',
      password: 'logistics123',
      role: 'AGENT',
      department: 'Logistics',
      agentIdCode: '#AGT-LOG-01',
    },
  };

  // -------------------------------------------------------------
  // AUTHENTICATION & USERS
  // -------------------------------------------------------------
  app.post('/api/auth/login', async (req, res) => {
    const { email, password, role } = req.body || {};
    if (!email || !password) {
      return res.status(400).json({ error: 'Email and Password are required.' });
    }

    const cleanEmail = String(email).trim().toLowerCase();
    const cleanPass = String(password).trim();
    const requestedRole = role ? String(role).trim().toUpperCase() : null;

    // 1. Check Agent Logins against preset credentials (NO DATABASE QUERY)
    if (PRESET_AGENTS[cleanEmail]) {
      const presetAgent = PRESET_AGENTS[cleanEmail];
      if (presetAgent.password !== cleanPass) {
        return res.status(401).json({
          error: 'Invalid credentials. Please contact your system administrator.',
          code: 'INVALID_CREDENTIALS',
        });
      }

      // If customer portal was requested with agent credentials
      if (requestedRole === 'CUSTOMER') {
        return res.status(401).json({
          error: 'This account is a Support Agent account. Please use the Support Agent portal.',
          code: 'UNAUTHORIZED_ROLE',
        });
      }

      return res.json({
        id: presetAgent.id,
        name: presetAgent.name,
        fullName: presetAgent.name,
        email: presetAgent.email,
        role: presetAgent.role,
        department: presetAgent.department,
        agentIdCode: presetAgent.agentIdCode,
      });
    }

    // If agent login attempted with non-preset email
    if (requestedRole === 'AGENT') {
      return res.status(401).json({
        error: 'Invalid credentials. Please contact your system administrator.',
        code: 'AGENT_NOT_FOUND',
      });
    }

    // 2. Customer Login: Only query existing columns (id, name, email, password) from users table
    try {
      const db = getDbPool();

      let rows: any[] = [];
      try {
        const [result]: any = await db.query(
          `SELECT id, name, email, password FROM users WHERE email = ?`,
          [cleanEmail]
        );
        rows = result;
      } catch (queryErr: any) {
        if (queryErr.code === 'ER_BAD_FIELD_ERROR') {
          // Table has `full_name` column instead of `name`
          const [altResult]: any = await db.query(
            `SELECT id, full_name as name, email, password FROM users WHERE email = ?`,
            [cleanEmail]
          );
          rows = altResult;
        } else {
          throw queryErr;
        }
      }

      if (!rows || rows.length === 0) {
        return res.status(401).json({
          error: 'User not found or database unreachable',
          code: 'USER_NOT_FOUND',
        });
      }

      const user = rows[0];

      // Direct password comparison
      if (user.password !== cleanPass) {
        return res.status(401).json({
          error: 'User not found or database unreachable',
          details: 'Invalid password provided for this account.',
          code: 'INVALID_CREDENTIALS',
        });
      }

      const userName = user.name || cleanEmail.split('@')[0];
      return res.json({
        id: user.id,
        name: userName,
        fullName: userName,
        email: user.email,
        role: 'CUSTOMER',
      });
    } catch (err: any) {
      console.error('[Database Auth Error]', err.message);
      return res.status(401).json({
        error: 'User not found or database unreachable',
        details: err.message || 'MySQL database connection failed.',
        code: err.code || 'DB_CONNECTION_ERROR',
      });
    }
  });

  // User Registration (Strict MySQL-backed)
  app.post(['/api/auth/register', '/api/users/register'], async (req, res) => {
    const { name, fullName, email, password, phone, role, department } = req.body || {};
    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required.' });
    }

    if (!phone || !String(phone).trim()) {
      return res.status(400).json({ error: 'Phone number is required for account registration.' });
    }

    const cleanPhone = String(phone).trim();
    const phoneDigits = cleanPhone.replace(/\D/g, '');
    if (phoneDigits.length < 10) {
      return res.status(400).json({ error: 'Please enter a valid phone number (at least 10 digits).' });
    }

    const cleanEmail = String(email).trim().toLowerCase();
    const cleanName = String(name || fullName || cleanEmail.split('@')[0]).trim();
    const cleanPass = String(password).trim();

    try {
      const db = getDbPool();

      // Check if email already exists
      const [existing]: any = await db.query(
        `SELECT id FROM users WHERE email = ?`,
        [cleanEmail]
      );

      if (existing && existing.length > 0) {
        return res.status(400).json({ error: 'Registration failed. Email already exists in database.' });
      }

      // Execute: INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?);
      let insertResult: any;
      try {
        const [res1]: any = await db.query(
          `INSERT INTO users (name, email, phone, password) VALUES (?, ?, ?, ?)`,
          [cleanName, cleanEmail, cleanPhone, cleanPass]
        );
        insertResult = res1;
      } catch (insErr: any) {
        if (insErr.code === 'ER_BAD_FIELD_ERROR') {
          // If table has full_name instead of name
          try {
            const [res2]: any = await db.query(
              `INSERT INTO users (full_name, email, phone, password) VALUES (?, ?, ?, ?)`,
              [cleanName, cleanEmail, cleanPhone, cleanPass]
            );
            insertResult = res2;
          } catch (insErr2: any) {
            if (insErr2.code === 'ER_BAD_FIELD_ERROR') {
              // In case phone column is not found
              const [res3]: any = await db.query(
                `INSERT INTO users (name, email, password) VALUES (?, ?, ?)`,
                [cleanName, cleanEmail, cleanPass]
              );
              insertResult = res3;
            } else {
              throw insErr2;
            }
          }
        } else {
          throw insErr;
        }
      }

      const newUserId = insertResult.insertId || 101;
      const newUserObj = {
        id: newUserId,
        name: cleanName,
        fullName: cleanName,
        email: cleanEmail,
        phone: cleanPhone,
        role: 'CUSTOMER',
      };

      res.status(201).json({
        message: 'User registered successfully in database.',
        userId: newUserId,
        user: newUserObj,
      });
    } catch (err: any) {
      console.error('[Registration Error]', err.message);
      res.status(500).json({
        error: 'Database registration failed.',
        details: err.message,
      });
    }
  });

  // AI Classification endpoint
  app.post('/api/classify', async (req, res) => {
    const { text } = req.body || {};
    if (!text || typeof text !== 'string') {
      return res.status(400).json({ error: 'Complaint text is required' });
    }
    
    const moderation = checkModeration(text);
    if (!moderation.isValid) {
      return res.status(422).json({
        error: moderation.error || 'Submission Blocked: Inappropriate language, slang, or informal joke text detected.',
        isModerated: true,
      });
    }

    const result = await classifyComplaint(text);
    res.json(result);
  });

  // -------------------------------------------------------------
  // 1. CUSTOMER SUBMISSIONS & HISTORY (Direct MySQL Queries)
  // -------------------------------------------------------------
  app.post('/api/complaints', async (req, res) => {
    const { userId, customerId, description, category, department, priority, userName, userEmail, attachments, voiceNote } = req.body || {};

    if (!description || !description.trim()) {
      return res.status(400).json({ error: 'Description is required' });
    }

    // Content Moderation Guardrail
    const moderation = checkModeration(description);
    if (!moderation.isValid) {
      return res.status(422).json({
        error: moderation.error || 'Submission Blocked: Inappropriate language, slang, or informal joke text detected. Please enter a genuine, professional issue description.',
        isModerated: true,
      });
    }

    const uid = Number(userId || customerId || 1);
    let assignedDept = department;
    let assignedCategory = category;
    let assignedPriority = priority;

    if (!assignedDept || !assignedPriority) {
      const autoClassified = await classifyComplaint(description);
      if (!assignedDept) assignedDept = autoClassified.department;
      if (!assignedCategory) assignedCategory = autoClassified.category;
      if (!assignedPriority) assignedPriority = autoClassified.priority;
    }

    const serializedAttachments = attachments && Array.isArray(attachments) && attachments.length > 0 ? JSON.stringify(attachments) : null;
    const serializedVoiceNote = voiceNote ? JSON.stringify(voiceNote) : null;

    try {
      const db = getDbPool();
      let insertResult: any;

      try {
        // Try inserting with attachments and voice_note columns
        const [res1]: any = await db.query(
          `INSERT INTO complaints (user_id, description, department, priority, category, status, attachments, voice_note) 
           VALUES (?, ?, ?, ?, ?, 'OPEN', ?, ?)`,
          [uid, description.trim(), assignedDept || 'Customer Care', assignedPriority || 'NORMAL', assignedCategory || 'General Inquiry', serializedAttachments, serializedVoiceNote]
        );
        insertResult = res1;
      } catch (insErr: any) {
        if (insErr.code === 'ER_BAD_FIELD_ERROR') {
          try {
            // Fallback without attachments and voice_note
            const [res2]: any = await db.query(
              `INSERT INTO complaints (user_id, description, department, priority, category, status) 
               VALUES (?, ?, ?, ?, ?, 'OPEN')`,
              [uid, description.trim(), assignedDept || 'Customer Care', assignedPriority || 'NORMAL', assignedCategory || 'General Inquiry']
            );
            insertResult = res2;
          } catch (insErr2: any) {
            if (insErr2.code === 'ER_BAD_FIELD_ERROR') {
              // Backward compatibility if older schema used customer_id
              const [res3]: any = await db.query(
                `INSERT INTO complaints (customer_id, description, department, priority, category, status) 
                 VALUES (?, ?, ?, ?, ?, 'OPEN')`,
                [uid, description.trim(), assignedDept || 'Customer Care', assignedPriority || 'NORMAL', assignedCategory || 'General Inquiry']
              );
              insertResult = res3;
            } else {
              throw insErr2;
            }
          }
        } else {
          throw insErr;
        }
      }

      const insertedId = insertResult.insertId;
      let newRows: any[] = [];
      try {
        const [rows]: any = await db.query(`SELECT * FROM complaints WHERE id = ?`, [insertedId]);
        newRows = rows || [];
      } catch {}

      const formatted = newRows && newRows.length > 0 ? formatComplaintRow(newRows[0]) : {
        id: insertedId,
        userId: uid,
        customerId: uid,
        userName: userName || 'Customer',
        userEmail: userEmail || 'customer@example.com',
        description: description.trim(),
        category: assignedCategory || 'General Inquiry',
        department: assignedDept || 'Customer Care',
        priority: (assignedPriority || 'NORMAL') as any,
        status: 'OPEN' as any,
        notes: 'Awaiting Agent Assignment',
        agent_notes: 'Awaiting Agent Assignment',
        createdAt: new Date().toISOString(),
        created_at: new Date().toISOString(),
        attachments: attachments || undefined,
        voiceNote: voiceNote || undefined,
      };

      res.status(201).json({
        message: 'Complaint submitted successfully.',
        complaint: formatted,
      });
    } catch (err: any) {
      console.error('[Create Complaint Error]', err.message);
      res.status(500).json({ error: 'Failed to insert complaint into database.', details: err.message });
    }
  });

  // Customer History: SELECT id, user_id, description, department, priority, category, status, agent_notes, created_at FROM complaints WHERE user_id = ? ORDER BY id DESC;
  app.get('/api/complaints/user/:userId', async (req, res) => {
    const uid = parseInt(req.params.userId, 10);
    if (isNaN(uid)) {
      return res.status(400).json({ error: 'Invalid user ID' });
    }

    try {
      const db = getDbPool();
      let rows: any[] = [];
      try {
        const [r1]: any = await db.query(
          `SELECT id, user_id, description, department, priority, category, status, agent_notes, created_at FROM complaints WHERE user_id = ? ORDER BY id DESC`,
          [uid]
        );
        rows = r1;
      } catch (err1: any) {
        if (err1.code === 'ER_BAD_FIELD_ERROR') {
          const [r2]: any = await db.query(
            `SELECT * FROM complaints WHERE user_id = ? OR customer_id = ? ORDER BY id DESC`,
            [uid, uid]
          );
          rows = r2;
        } else {
          throw err1;
        }
      }

      res.json(rows.map(formatComplaintRow));
    } catch (err: any) {
      console.error('[Fetch Customer Complaints Error]', err.message);
      res.status(500).json({ error: 'Failed to fetch customer complaints from database.', details: err.message });
    }
  });

  // -------------------------------------------------------------
  // 2. AGENT DEPARTMENT QUEUES & UPDATES (Direct MySQL Queries)
  // -------------------------------------------------------------
  app.get('/api/complaints/department/:dept', async (req, res) => {
    const rawDept = decodeURIComponent(req.params.dept || '').trim();

    try {
      const db = getDbPool();
      let rows: any[] = [];

      try {
        const [r1]: any = await db.query(
          `SELECT c.*, COALESCE(u.name, u.full_name) AS user_name, u.email AS user_email
           FROM complaints c
           LEFT JOIN users u ON c.user_id = u.id
           WHERE c.department = ? OR c.department LIKE ?
           ORDER BY c.id DESC`,
          [rawDept, `%${rawDept}%`]
        );
        rows = r1;
      } catch (deptErr: any) {
        try {
          const [r2]: any = await db.query(
            `SELECT c.*, COALESCE(u.name, u.full_name) AS user_name, u.email AS user_email
             FROM complaints c
             LEFT JOIN users u ON c.customer_id = u.id
             WHERE c.department = ? OR c.department LIKE ?
             ORDER BY c.id DESC`,
            [rawDept, `%${rawDept}%`]
          );
          rows = r2;
        } catch {
          const [simpleRows]: any = await db.query(
            `SELECT * FROM complaints WHERE department = ? OR department LIKE ? ORDER BY id DESC`,
            [rawDept, `%${rawDept}%`]
          );
          rows = simpleRows;
        }
      }

      res.json(rows.map(formatComplaintRow));
    } catch (err: any) {
      console.error('[Fetch Agent Department Queue Error]', err.message);
      res.status(500).json({ error: 'Failed to fetch department complaints.', details: err.message });
    }
  });

  // Update Status & Notes: UPDATE complaints SET status = ?, agent_notes = ?, agent_id = ?, agent_name = ? WHERE id = ?
  app.put('/api/complaints/:id', async (req, res) => {
    const id = parseInt(req.params.id, 10);
    const { status, notes, agent_notes, agentId, agent_id, agentName, agent_name } = req.body || {};
    const finalNotes = notes !== undefined ? notes : agent_notes;
    const finalAgentId = agentId || agent_id;
    const finalAgentName = agentName || agent_name;

    if (!id || isNaN(id)) {
      return res.status(400).json({ error: 'Invalid complaint ID' });
    }

    try {
      const db = getDbPool();
      const updates: string[] = [];
      const params: any[] = [];

      if (status) {
        updates.push('status = ?');
        params.push(status.toUpperCase());
      }
      if (finalNotes !== undefined) {
        updates.push('agent_notes = ?');
        params.push(finalNotes.trim());
      }
      if (finalAgentId) {
        updates.push('agent_id = ?');
        params.push(finalAgentId);
      }
      if (finalAgentName) {
        updates.push('agent_name = ?');
        params.push(finalAgentName);
      }

      if (updates.length > 0) {
        params.push(id);
        await db.query(`UPDATE complaints SET ${updates.join(', ')} WHERE id = ?`, params);
      }

      const [updatedRows]: any = await db.query(`SELECT * FROM complaints WHERE id = ?`, [id]);
      if (!updatedRows || updatedRows.length === 0) {
        return res.status(404).json({ error: 'Complaint not found in database.' });
      }

      res.json({
        message: 'Complaint updated successfully in database.',
        complaint: formatComplaintRow(updatedRows[0]),
      });
    } catch (err: any) {
      console.error('[Update Complaint Error]', err.message);
      res.status(500).json({ error: 'Failed to update complaint in database.', details: err.message });
    }
  });

  // Update Status
  app.put('/api/complaints/:id/status', async (req, res) => {
    const id = parseInt(req.params.id, 10);
    const { status, agentId, agent_id, agentName, agent_name } = { ...req.query, ...req.body };
    const targetStatus = (typeof status === 'string' ? status : req.body?.status)?.toUpperCase();
    const finalAgentId = agentId || agent_id;
    const finalAgentName = agentName || agent_name;

    if (!['OPEN', 'IN_PROGRESS', 'RESOLVED'].includes(targetStatus)) {
      return res.status(400).json({ error: 'Invalid status value. Allowed: OPEN, IN_PROGRESS, RESOLVED' });
    }

    try {
      const db = getDbPool();
      if (finalAgentId && finalAgentName) {
        await db.query(
          `UPDATE complaints SET status = ?, agent_id = ?, agent_name = ? WHERE id = ?`,
          [targetStatus, finalAgentId, finalAgentName, id]
        );
      } else if (finalAgentId) {
        await db.query(
          `UPDATE complaints SET status = ?, agent_id = ? WHERE id = ?`,
          [targetStatus, finalAgentId, id]
        );
      } else {
        await db.query(
          `UPDATE complaints SET status = ? WHERE id = ?`,
          [targetStatus, id]
        );
      }

      const [rows]: any = await db.query(`SELECT * FROM complaints WHERE id = ?`, [id]);
      if (!rows || rows.length === 0) {
        return res.status(404).json({ error: 'Complaint not found in database.' });
      }

      res.json({
        message: 'Status updated successfully in database.',
        complaint: formatComplaintRow(rows[0]),
      });
    } catch (err: any) {
      console.error('[Update Status Error]', err.message);
      res.status(500).json({ error: 'Failed to update complaint status in database.', details: err.message });
    }
  });

  // Update Notes
  app.put('/api/complaints/:id/notes', async (req, res) => {
    const id = parseInt(req.params.id, 10);
    const { agentId, agent_id, agentName, agent_name } = { ...req.query, ...req.body };
    const notes = typeof req.query.notes === 'string' ? req.query.notes : (req.body?.notes || req.body?.agent_notes);
    const cleanNotes = (notes || '').trim() || 'Under Review';
    const finalAgentId = agentId || agent_id;
    const finalAgentName = agentName || agent_name;

    try {
      const db = getDbPool();
      if (finalAgentId && finalAgentName) {
        await db.query(
          `UPDATE complaints SET agent_notes = ?, agent_id = ?, agent_name = ? WHERE id = ?`,
          [cleanNotes, finalAgentId, finalAgentName, id]
        );
      } else if (finalAgentId) {
        await db.query(
          `UPDATE complaints SET agent_notes = ?, agent_id = ? WHERE id = ?`,
          [cleanNotes, finalAgentId, id]
        );
      } else {
        await db.query(
          `UPDATE complaints SET agent_notes = ? WHERE id = ?`,
          [cleanNotes, id]
        );
      }

      const [rows]: any = await db.query(`SELECT * FROM complaints WHERE id = ?`, [id]);
      if (!rows || rows.length === 0) {
        return res.status(404).json({ error: 'Complaint not found in database.' });
      }

      res.json({
        message: 'Agent notes updated successfully in database.',
        complaint: formatComplaintRow(rows[0]),
      });
    } catch (err: any) {
      console.error('[Update Notes Error]', err.message);
      res.status(500).json({ error: 'Failed to update agent notes in database.', details: err.message });
    }
  });

  // Delete Complaint
  app.delete('/api/complaints/:id', async (req, res) => {
    const id = parseInt(req.params.id, 10);
    if (!id || isNaN(id)) {
      return res.status(400).json({ error: 'Invalid complaint ID' });
    }

    try {
      const db = getDbPool();
      const [result]: any = await db.query(`DELETE FROM complaints WHERE id = ?`, [id]);
      if (result.affectedRows === 0) {
        return res.status(404).json({ error: `Complaint #${id} not found in database.` });
      }
      res.status(200).json({
        success: true,
        message: `Complaint #${id} permanently deleted.`,
      });
    } catch (err: any) {
      console.error('[Delete Complaint Error]', err.message);
      res.status(500).json({ error: 'Failed to delete complaint from database.', details: err.message });
    }
  });

  // -------------------------------------------------------------
  // BMC Helix ServiceOps & AIOps Endpoints
  // -------------------------------------------------------------
  const knowledgeArticlesStore = [
    {
      id: 'KB-101',
      ticketId: 1,
      title: 'Resolving Merchant Double Billing & Immediate Authorization Hold Release',
      category: 'Billing & Payments',
      department: 'Finance & Payroll',
      summary: 'Self-service procedure to identify pending pre-authorization holds vs captured invoices and request automated clearing.',
      troubleshootingSteps: [
        'Log in to customer portal and navigate to Statements -> Authorization Holds.',
        'Match merchant authorization descriptor with the invoice number on your order receipt.',
        'If dual charges exist for one order ID, click "Request Automated Void Release" to credit hold within 24–48 hours.',
        'For posted debit charges, download the PDF receipt and submit an instant refund request.',
      ],
      author: 'Elena Vance (#AGT-FIN-01)',
      createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
      views: 142,
      helpfulCount: 38,
    },
    {
      id: 'KB-102',
      ticketId: 2,
      title: 'Fixing HTTP 500 API Gateway Timeout and Restoring SSO Authentication',
      category: 'System Error',
      department: 'Technical Support',
      summary: 'Troubleshooting steps for customer login crashes, 500 error popups, and stale local token cache.',
      troubleshootingSteps: [
        'Clear browser cookies and session storage (Ctrl+Shift+Delete / Cmd+Shift+Delete).',
        'Check DNS settings and verify no corporate proxy is blocking secure WebSocket / REST headers.',
        'Use the "Invalidate Session" self-service tool to flush active OAuth tokens.',
        'If the issue persists, test login in Private / Incognito window to bypass stale service worker cache.',
      ],
      author: 'Alex Rivera (#AGT-TECH-01)',
      createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
      views: 319,
      helpfulCount: 94,
    },
    {
      id: 'KB-103',
      ticketId: 3,
      title: 'Real-Time Courier Package Tracking & Emergency Address Re-Route Guide',
      category: 'Order Dispatch',
      department: 'Logistics',
      summary: 'Step-by-step guide for tracking dispatch manifests and updating shipping destinations before dock release.',
      troubleshootingSteps: [
        'Locate carrier dispatch tracking number in your dispatch confirmation email.',
        'If package is marked "In Transit - Hub Sorting", address changes can be submitted via carrier self-service portal.',
        'For urgent hold-at-location requests, contact the local warehouse hub with your manifest code.',
      ],
      author: 'Marcus Vance (#AGT-LOG-01)',
      createdAt: new Date(Date.now() - 86400000 * 7).toISOString(),
      views: 205,
      helpfulCount: 52,
    },
  ];

  // Get Knowledge Articles
  app.get('/api/knowledge-articles', (req, res) => {
    res.json(knowledgeArticlesStore);
  });

  // Post new Shift-Left Knowledge Article
  app.post('/api/knowledge-articles', (req, res) => {
    const { title, category, department, summary, troubleshootingSteps, author, ticketId } = req.body;
    if (!title || !summary) {
      return res.status(400).json({ error: 'Title and summary are required for Knowledge Articles.' });
    }

    const newArticle = {
      id: `KB-${Math.floor(1000 + Math.random() * 9000)}`,
      ticketId: ticketId || undefined,
      title: String(title).trim(),
      category: category || department || 'General',
      department: department || 'Customer Care',
      summary: String(summary).trim(),
      troubleshootingSteps: Array.isArray(troubleshootingSteps) ? troubleshootingSteps : [String(troubleshootingSteps)],
      author: author || 'BMC Helix AIOps Specialist',
      createdAt: new Date().toISOString(),
      views: 1,
      helpfulCount: 0,
    };

    knowledgeArticlesStore.unshift(newArticle);
    res.status(201).json({
      success: true,
      message: 'Shift-Left Knowledge Article published to customer self-service knowledge base.',
      article: newArticle,
    });
  });

  // AIOps Autonomous Remediation Script Execution
  app.post('/api/aiops/remediate', async (req, res) => {
    const { actionId, ticketId, agentId } = req.body;
    // Simulate realistic autonomous remediation latency
    const timestamp = new Date().toISOString();

    let details = 'Auto-remediation executed successfully.';
    if (actionId === 'db_reset') {
      details = 'Reset MySQL Connection Pool, recycled idle connections, and cleared zombie deadlocks.';
    } else if (actionId === 'clear_cache') {
      details = 'Invalidated user authorization session cache, purged Redis tokens, and forced security re-auth.';
    } else if (actionId === 'reconcile_payment') {
      details = 'Triggered merchant payment gateway reconciliation webhook. Synced ledger status.';
    }

    res.json({
      success: true,
      actionId,
      ticketId,
      timestamp,
      actor: agentId || 'BMC Helix AIOps Autonomous Engine',
      logMessage: `[SYSTEM/AIOps] Auto-remediation script executed successfully by Agent: ${details}`,
      executionTimeMs: 2000,
    });
  });

  // -------------------------------------------------------------
  // 3. ALL COMPLAINTS & FILTER / ANALYTICS (Direct MySQL)
  // -------------------------------------------------------------
  app.get('/api/complaints', async (req, res) => {
    try {
      const rows = await executeQuery(`
        SELECT c.id, c.user_id, c.description, c.department, c.priority, c.category, c.status, c.agent_notes, c.created_at, c.resolution_time_hours,
               COALESCE(u.name, u.full_name) AS user_name, u.email AS user_email
        FROM complaints c
        LEFT JOIN users u ON c.user_id = u.id
        ORDER BY c.id DESC
      `).catch(async () => {
        return executeQuery(`SELECT * FROM complaints ORDER BY id DESC`);
      });
      res.json(rows.map(formatComplaintRow));
    } catch (err: any) {
      res.status(500).json({ error: 'Database query failed', details: err.message });
    }
  });

  // Filter and Sort Endpoint
  app.get('/api/complaints/filter', async (req, res) => {
    const { department, status, priority, sortBy } = req.query;

    let sql = `
      SELECT c.id, c.user_id, c.description, c.department, c.priority, c.category, c.status, c.agent_notes, c.created_at, c.resolution_time_hours,
             COALESCE(u.name, u.full_name) AS user_name, u.email AS user_email
      FROM complaints c
      LEFT JOIN users u ON c.user_id = u.id
      WHERE 1=1
    `;
    const params: any[] = [];

    if (department && department !== 'ALL') {
      sql += ` AND (c.department = ? OR c.department LIKE ?)`;
      params.push(department, `%${department}%`);
    }

    if (status && status !== 'ALL') {
      sql += ` AND c.status = ?`;
      params.push(String(status).toUpperCase());
    }

    if (priority && priority !== 'ALL') {
      sql += ` AND c.priority = ?`;
      params.push(String(priority).toUpperCase());
    }

    if (sortBy === 'Oldest First') {
      sql += ` ORDER BY c.id ASC`;
    } else if (sortBy === 'Priority (High to Low)') {
      sql += ` ORDER BY CASE c.priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'NORMAL' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END, c.id DESC`;
    } else {
      sql += ` ORDER BY c.id DESC`;
    }

    try {
      const rows = await executeQuery(sql, params);
      res.json(rows.map(formatComplaintRow));
    } catch (err: any) {
      res.status(500).json({ error: 'Filter query failed', details: err.message });
    }
  });

  // Get Single Complaint by ID
  app.get('/api/complaints/:id', async (req, res) => {
    const id = parseInt(req.params.id, 10);
    try {
      const rows = await executeQuery(
        `SELECT c.id, c.user_id, c.description, c.department, c.priority, c.category, c.status, c.agent_notes, c.created_at, c.resolution_time_hours,
                COALESCE(u.name, u.full_name) AS user_name, u.email AS user_email
         FROM complaints c
         LEFT JOIN users u ON c.user_id = u.id
         WHERE c.id = ?`,
        [id]
      );

      if (rows.length === 0) {
        return res.status(404).json({ error: `Complaint not found with ID: ${id}` });
      }

      res.json(formatComplaintRow(rows[0]));
    } catch (err: any) {
      res.status(500).json({ error: 'Query failed', details: err.message });
    }
  });

  // Analytics Aggregation directly from MySQL
  app.get('/api/analytics', async (req, res) => {
    try {
      const rows = await executeQuery(`SELECT * FROM complaints`);
      const list = rows.map(formatComplaintRow);

      const total = list.length;
      const pending = list.filter((c) => c.status === 'OPEN').length;
      const inProgress = list.filter((c) => c.status === 'IN_PROGRESS').length;
      const resolved = list.filter((c) => c.status === 'RESOLVED').length;

      const finance = list.filter((c) => c.department.includes('Finance')).length;
      const technical = list.filter((c) => c.department.includes('Technical')).length;
      const logistics = list.filter((c) => c.department.includes('Logistics')).length;
      const customerCare = list.filter((c) => c.department.includes('Customer Care')).length;

      res.json({
        total,
        pending,
        inProgress,
        resolved,
        finance,
        technical,
        logistics,
        customerCare,
      });
    } catch (err: any) {
      res.status(500).json({ error: 'Analytics query failed', details: err.message });
    }
  });

  // Vite Middleware in Development vs Static in Production
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(rootDir || process.cwd() || '.', 'dist');
    app.use(express.static(distPath));
    app.use((req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
  });
}

startServer();
