import mysql from 'mysql2/promise';

export interface DbUser {
  id: number;
  name?: string;
  full_name?: string;
  email: string;
  password?: string;
  phone?: string;
  role: 'CUSTOMER' | 'AGENT' | 'ADMIN';
  department?: string;
}

export interface DbComplaint {
  id: number;
  customer_id: number;
  description: string;
  department: string;
  priority: 'LOW' | 'NORMAL' | 'MEDIUM' | 'HIGH';
  category: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED';
  agent_notes?: string;
  agent_id?: string;
  agent_name?: string;
  created_at?: string | Date;
  resolution_time_hours?: number;
  user_name?: string;
  user_email?: string;
}

export const TARGET_DATABASE_NAME = 'complaints_db';

// Strictly enforce the target database name
process.env.DB_NAME = TARGET_DATABASE_NAME;
process.env.AWS_RDS_DATABASE = TARGET_DATABASE_NAME;

let pool: mysql.Pool | null = null;
let activeDatabaseName: string = TARGET_DATABASE_NAME;

const DEFAULT_DB_HOST = 'complaints-db.czoe06ig4twu.ap-south-1.rds.amazonaws.com';
const DEFAULT_DB_USER = 'admin';
const DEFAULT_DB_PASS = 'BarclaysCMS#2026';
const DEFAULT_DB_PORT = 3306;

let lastConnectionStatus: {
  isConnected: boolean;
  host: string;
  database: string;
  port: number;
  user: string;
  error: string | null;
  code?: string;
  checkedAt: string;
} = {
  isConnected: false,
  host: process.env.DB_HOST || process.env.AWS_RDS_HOST || DEFAULT_DB_HOST,
  database: TARGET_DATABASE_NAME,
  port: Number(process.env.DB_PORT || process.env.AWS_RDS_PORT || DEFAULT_DB_PORT),
  user: process.env.DB_USER || process.env.AWS_RDS_USER || DEFAULT_DB_USER,
  error: null,
  checkedAt: new Date().toISOString(),
};

/**
 * Resets the connection pool (used when switching databases or recovering from errors)
 */
export function resetDbPool() {
  if (pool) {
    try {
      pool.end().catch(() => {});
    } catch {}
    pool = null;
  }
}

/**
 * Connects to MySQL root server without specifying a database to ensure `complaints_db` exists
 */
export async function ensureDatabaseCreated(): Promise<string | null> {
  const host = process.env.DB_HOST || process.env.AWS_RDS_HOST || DEFAULT_DB_HOST;
  const user = process.env.DB_USER || process.env.AWS_RDS_USER || DEFAULT_DB_USER;
  const password = process.env.DB_PASSWORD || process.env.AWS_RDS_PASSWORD || DEFAULT_DB_PASS;
  const port = Number(process.env.DB_PORT || process.env.AWS_RDS_PORT || DEFAULT_DB_PORT);
  const useSsl = process.env.DB_SSL === 'true' || process.env.AWS_RDS_SSL === 'true';

  let rootConn: mysql.Connection | null = null;
  try {
    // Connect without selecting a database
    rootConn = await mysql.createConnection({
      host,
      user,
      password,
      port,
      connectTimeout: 8000,
      ssl: useSsl ? { rejectUnauthorized: false } : undefined,
    });

    // Try creating complaints_db if not existing
    try {
      await rootConn.query(`CREATE DATABASE IF NOT EXISTS \`complaints_db\`;`);
      console.log(`[MySQL] Verified/created database 'complaints_db'`);
      activeDatabaseName = TARGET_DATABASE_NAME;
    } catch (createErr: any) {
      console.warn(`[MySQL] Note: CREATE DATABASE complaints_db: ${createErr.message}`);
    }

    // Check which databases exist
    try {
      const [dbs]: any = await rootConn.query(`SHOW DATABASES`);
      const existingDbNames = (dbs || []).map((row: any) => Object.values(row)[0]);
      console.log(`[MySQL] Available databases on server:`, existingDbNames);

      if (existingDbNames.includes('complaints_db')) {
        activeDatabaseName = 'complaints_db';
      }
    } catch (showErr: any) {
      console.warn(`[MySQL] SHOW DATABASES check:`, showErr.message);
    }

    await rootConn.end();
    return activeDatabaseName;
  } catch (err: any) {
    console.warn(`[MySQL] Direct server connection error:`, err.message);
    if (rootConn) {
      try { await rootConn.end(); } catch {}
    }
    return TARGET_DATABASE_NAME;
  }
}

/**
 * Get or initialize the MySQL connection pool
 */
export function getDbPool(): mysql.Pool {
  const host = process.env.DB_HOST || process.env.AWS_RDS_HOST || DEFAULT_DB_HOST;
  const user = process.env.DB_USER || process.env.AWS_RDS_USER || DEFAULT_DB_USER;
  const password = process.env.DB_PASSWORD || process.env.AWS_RDS_PASSWORD || DEFAULT_DB_PASS;
  const port = Number(process.env.DB_PORT || process.env.AWS_RDS_PORT || DEFAULT_DB_PORT);
  const useSsl = process.env.DB_SSL === 'true' || process.env.AWS_RDS_SSL === 'true';

  if (!pool) {
    activeDatabaseName = TARGET_DATABASE_NAME;
    pool = mysql.createPool({
      host,
      user,
      password,
      database: TARGET_DATABASE_NAME,
      port,
      waitForConnections: true,
      connectionLimit: 10,
      queueLimit: 0,
      connectTimeout: 8000,
      ssl: useSsl ? { rejectUnauthorized: false } : undefined,
    });
  }
  return pool;
}

/**
 * Actively test connection to MySQL database with automatic recovery on ER_BAD_DB_ERROR
 */
export async function testDbConnection(): Promise<typeof lastConnectionStatus> {
  const host = process.env.DB_HOST || process.env.AWS_RDS_HOST || '127.0.0.1';
  const user = process.env.DB_USER || process.env.AWS_RDS_USER || 'root';
  const port = Number(process.env.DB_PORT || process.env.AWS_RDS_PORT || 3306);

  try {
    const db = getDbPool();
    const conn = await db.getConnection();
    await conn.query('SELECT 1');
    conn.release();

    lastConnectionStatus = {
      isConnected: true,
      host,
      database: activeDatabaseName,
      port,
      user,
      error: null,
      checkedAt: new Date().toISOString(),
    };
    return lastConnectionStatus;
  } catch (err: any) {
    console.error('[MySQL Connection Test Error]', err.message);

    // If Unknown database error, attempt auto-creation / database resolution
    if (err.code === 'ER_BAD_DB_ERROR' || err.message?.includes('Unknown database')) {
      console.log('[MySQL] Unknown database encountered. Attempting to ensure database exists...');
      const createdDb = await ensureDatabaseCreated();
      if (createdDb) {
        resetDbPool();
        try {
          const retryDb = getDbPool();
          const retryConn = await retryDb.getConnection();
          await retryConn.query('SELECT 1');
          retryConn.release();

          lastConnectionStatus = {
            isConnected: true,
            host,
            database: activeDatabaseName,
            port,
            user,
            error: null,
            checkedAt: new Date().toISOString(),
          };
          return lastConnectionStatus;
        } catch (retryErr: any) {
          console.error('[MySQL Retry Connection Error]', retryErr.message);
        }
      }
    }

    lastConnectionStatus = {
      isConnected: false,
      host,
      database: activeDatabaseName,
      port,
      user,
      error: err.message || 'Failed to connect to MySQL database',
      code: err.code || 'ERR_CONNECTION_REFUSED',
      checkedAt: new Date().toISOString(),
    };
    return lastConnectionStatus;
  }
}

/**
 * Initializes database schemas on MySQL:
 * Creates `users` and `complaints` tables, and seeds initial users if the table is empty.
 */
export async function initDatabase(): Promise<boolean> {
  try {
    // Ensure the database exists on MySQL server
    await ensureDatabaseCreated();

    const db = getDbPool();
    const conn = await db.getConnection();
    console.log(`[MySQL] Successfully connected to MySQL host: ${process.env.DB_HOST || process.env.AWS_RDS_HOST || '127.0.0.1'} (DB: ${activeDatabaseName})`);

    // 1. Create Users Table (with both 'name' and 'full_name' and 'phone' support)
    await conn.query(`
      CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(150) NOT NULL,
        full_name VARCHAR(150),
        email VARCHAR(150) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        phone VARCHAR(50) NOT NULL,
        role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
        department VARCHAR(100),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    `);

    // Ensure phone column exists on pre-existing users table
    try {
      const [userCols]: any = await conn.query(`SHOW COLUMNS FROM users`);
      const existingUserColNames = (userCols || []).map((col: any) => col.Field.toLowerCase());
      if (!existingUserColNames.includes('phone')) {
        await conn.query(`ALTER TABLE users ADD COLUMN phone VARCHAR(50) DEFAULT NULL`);
        console.log('[MySQL] Added phone column to users table');
      }
    } catch (uColErr: any) {
      console.warn('[MySQL] Could not check users table columns:', uColErr.message);
    }

    // 2. Create Complaints Table (with user_id, agent_id, agent_name)
    await conn.query(`
      CREATE TABLE IF NOT EXISTS complaints (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        description TEXT NOT NULL,
        department VARCHAR(100) NOT NULL,
        priority VARCHAR(50) NOT NULL,
        category VARCHAR(100) NOT NULL,
        status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
        agent_notes TEXT,
        agent_id VARCHAR(50) DEFAULT NULL,
        agent_name VARCHAR(150) DEFAULT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        resolution_time_hours INT DEFAULT NULL,
        INDEX idx_user (user_id),
        INDEX idx_department (department),
        INDEX idx_status (status)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    `);

    // Ensure backwards-compatible columns exist on pre-existing complaints table
    try {
      const [complaintCols]: any = await conn.query(`SHOW COLUMNS FROM complaints`);
      const existingColNames = (complaintCols || []).map((col: any) => col.Field.toLowerCase());

      // If agent_id is missing:
      if (!existingColNames.includes('agent_id')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN agent_id VARCHAR(50) DEFAULT NULL`);
          console.log('[MySQL] Added agent_id column to complaints table');
        } catch (alterErr: any) {
          console.warn('[MySQL] ALTER complaints agent_id:', alterErr.message);
        }
      }

      // If agent_name is missing:
      if (!existingColNames.includes('agent_name')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN agent_name VARCHAR(150) DEFAULT NULL`);
          console.log('[MySQL] Added agent_name column to complaints table');
        } catch (alterErr: any) {
          console.warn('[MySQL] ALTER complaints agent_name:', alterErr.message);
        }
      }

      // If user_id is missing:
      if (!existingColNames.includes('user_id')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN user_id INT DEFAULT 1`);
          if (existingColNames.includes('customer_id')) {
            await conn.query(`UPDATE complaints SET user_id = customer_id WHERE user_id IS NULL OR user_id = 1`);
          }
          console.log('[MySQL] Added user_id column to complaints table');
        } catch (alterErr: any) {
          console.warn('[MySQL] ALTER complaints user_id:', alterErr.message);
        }
      }

      // If customer_id is missing:
      if (!existingColNames.includes('customer_id')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN customer_id INT DEFAULT 1`);
          if (existingColNames.includes('user_id')) {
            await conn.query(`UPDATE complaints SET customer_id = user_id WHERE customer_id IS NULL OR customer_id = 1`);
          }
        } catch {}
      }

      // If agent_notes is missing:
      if (!existingColNames.includes('agent_notes')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN agent_notes TEXT DEFAULT NULL`);
          if (existingColNames.includes('notes')) {
            await conn.query(`UPDATE complaints SET agent_notes = notes WHERE agent_notes IS NULL`);
          }
        } catch {}
      }

      // If notes is missing:
      if (!existingColNames.includes('notes')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN notes TEXT DEFAULT NULL`);
          if (existingColNames.includes('agent_notes')) {
            await conn.query(`UPDATE complaints SET notes = agent_notes WHERE notes IS NULL`);
          }
        } catch {}
      }

      // If category is missing:
      if (!existingColNames.includes('category')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN category VARCHAR(100) DEFAULT 'General Inquiry'`);
        } catch {}
      }

      // If department is missing:
      if (!existingColNames.includes('department')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN department VARCHAR(100) DEFAULT 'Customer Care'`);
        } catch {}
      }

      // If priority is missing:
      if (!existingColNames.includes('priority')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN priority VARCHAR(50) DEFAULT 'NORMAL'`);
        } catch {}
      }

      // If status is missing:
      if (!existingColNames.includes('status')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN status VARCHAR(50) DEFAULT 'OPEN'`);
        } catch {}
      }

      // If attachments is missing:
      if (!existingColNames.includes('attachments')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN attachments LONGTEXT DEFAULT NULL`);
          console.log('[MySQL] Added attachments column to complaints table');
        } catch {}
      }

      // If voice_note is missing:
      if (!existingColNames.includes('voice_note')) {
        try {
          await conn.query(`ALTER TABLE complaints ADD COLUMN voice_note LONGTEXT DEFAULT NULL`);
          console.log('[MySQL] Added voice_note column to complaints table');
        } catch {}
      }
    } catch (colInspectErr: any) {
      console.warn('[MySQL] Could not inspect complaints columns:', colInspectErr.message);
    }

    // 3. Seed initial users if table is empty
    const [userRows]: any = await conn.query(`SELECT COUNT(*) as count FROM users`);
    if (userRows[0]?.count === 0) {
      console.log('[MySQL] Seeding initial users in MySQL database...');
      const seedUsers = [
        {
          id: 1,
          name: 'Alex Johnson',
          email: 'alex.customer@example.com',
          password: 'password123',
          phone: '+1-555-0192',
          role: 'CUSTOMER',
          department: null,
        },
        {
          id: 2,
          name: 'Sophia Martinez',
          email: 'sophia.m@example.com',
          password: 'password123',
          phone: '+1-555-0183',
          role: 'CUSTOMER',
          department: null,
        },
        {
          id: 9001,
          name: 'Elena Vance (Finance Lead)',
          email: 'finance@agent.company.com',
          password: 'finance123',
          phone: '+1-555-9001',
          role: 'AGENT',
          department: 'Finance & Payroll',
        },
        {
          id: 9002,
          name: 'Marcus Brody (Tech Specialist)',
          email: 'tech@agent.company.com',
          password: 'tech123',
          phone: '+1-555-9002',
          role: 'AGENT',
          department: 'Technical Support',
        },
        {
          id: 9003,
          name: 'Sarah Chen (Care Specialist)',
          email: 'care@agent.company.com',
          password: 'care123',
          phone: '+1-555-9003',
          role: 'AGENT',
          department: 'Customer Care',
        },
        {
          id: 9004,
          name: 'David Miller (Logistics Mgr)',
          email: 'logistics@agent.company.com',
          password: 'logistics123',
          phone: '+1-555-9004',
          role: 'AGENT',
          department: 'Logistics',
        },
      ];

      for (const u of seedUsers) {
        try {
          await conn.query(
            `INSERT INTO users (id, name, full_name, email, password, phone, role, department) 
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE name=VALUES(name)`,
            [u.id, u.name, u.name, u.email, u.password, u.phone, u.role, u.department]
          );
        } catch (seedErr: any) {
          // Fallback if users table only has basic columns (id, name, email, password)
          try {
            await conn.query(
              `INSERT INTO users (id, name, email, password) 
               VALUES (?, ?, ?, ?)
               ON DUPLICATE KEY UPDATE name=VALUES(name)`,
              [u.id, u.name, u.email, u.password]
            );
          } catch {
            // Ignore if schema differs or already present
          }
        }
      }
    }

    // 4. Seed initial complaints if table is empty
    const [complaintRows]: any = await conn.query(`SELECT COUNT(*) as count FROM complaints`);
    if (complaintRows[0]?.count === 0) {
      console.log('[MySQL] Seeding sample complaints in MySQL database...');
      const seedComplaints = [
        {
          id: 101,
          customer_id: 1,
          description: 'Double charge occurred on my March invoice #INV-4921. Need immediate refund.',
          category: 'Billing',
          department: 'Finance & Payroll',
          priority: 'HIGH',
          status: 'IN_PROGRESS',
          agent_notes: 'Investigating billing transaction gateway ledger with merchant team.',
          agent_id: '#AGT-FIN-01',
          agent_name: 'Elena Vance (Finance Lead)',
          created_at: new Date(Date.now() - 36 * 3600 * 1000),
        },
        {
          id: 102,
          customer_id: 1,
          description: 'API gateway throws 500 internal server error during OAuth token refresh loop.',
          category: 'Technical',
          department: 'Technical Support',
          priority: 'HIGH',
          status: 'OPEN',
          agent_notes: 'Awaiting Agent Assignment',
          agent_id: null,
          agent_name: null,
          created_at: new Date(Date.now() - 14 * 3600 * 1000),
        },
        {
          id: 103,
          customer_id: 2,
          description: 'Hardware shipment package #TRK-8812 was marked delivered but never arrived at warehouse dock.',
          category: 'Logistics',
          department: 'Logistics',
          priority: 'MEDIUM',
          status: 'IN_PROGRESS',
          agent_notes: 'Contacted carrier dispatcher for dock manifest signature verification.',
          agent_id: '#AGT-LOG-01',
          agent_name: 'David Miller (Logistics Mgr)',
          created_at: new Date(Date.now() - 48 * 3600 * 1000),
        },
        {
          id: 104,
          customer_id: 2,
          description: 'General inquiry regarding enterprise SLA escalation terms and warranty extension policy.',
          category: 'General Inquiry',
          department: 'Customer Care',
          priority: 'LOW',
          status: 'RESOLVED',
          agent_notes: 'Provided complete enterprise tier SLA handbook and activated 1-year complimentary warranty.',
          agent_id: '#AGT-CARE-01',
          agent_name: 'Sarah Chen (Care Specialist)',
          created_at: new Date(Date.now() - 96 * 3600 * 1000),
          resolution_time_hours: 18,
        },
      ];

      for (const c of seedComplaints) {
        await conn.query(
          `INSERT INTO complaints (id, customer_id, description, department, priority, category, status, agent_notes, agent_id, agent_name, created_at, resolution_time_hours)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           ON DUPLICATE KEY UPDATE status=VALUES(status)`,
          [
            c.id,
            c.customer_id,
            c.description,
            c.department,
            c.priority,
            c.category,
            c.status,
            c.agent_notes,
            c.agent_id || null,
            c.agent_name || null,
            c.created_at,
            c.resolution_time_hours || null,
          ]
        );
      }
    }

    conn.release();
    lastConnectionStatus.isConnected = true;
    lastConnectionStatus.error = null;
    return true;
  } catch (err: any) {
    console.error('[MySQL] Database schema initialization error:', err.message);
    lastConnectionStatus.isConnected = false;
    lastConnectionStatus.error = err.message;
    return false;
  }
}

/**
 * Execute MySQL Query
 */
export async function executeQuery<T = any>(sql: string, params: any[] = []): Promise<T[]> {
  const db = getDbPool();
  const [rows]: any = await db.query(sql, params);
  lastConnectionStatus.isConnected = true;
  lastConnectionStatus.error = null;
  return rows as T[];
}

/**
 * Execute MySQL Mutation (INSERT, UPDATE, DELETE)
 */
export async function executeMutation(sql: string, params: any[] = []): Promise<{ insertId?: number; affectedRows: number }> {
  const db = getDbPool();
  const [result]: any = await db.query(sql, params);
  lastConnectionStatus.isConnected = true;
  lastConnectionStatus.error = null;
  return {
    insertId: result?.insertId,
    affectedRows: result?.affectedRows || 0,
  };
}

export function getDbConnectionStatus() {
  return lastConnectionStatus;
}
