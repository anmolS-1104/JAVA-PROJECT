# 🗂️ Intelligent Complaint Resolution System (ICRS)

A full-stack complaint routing and resolution management portal ported to **React 19 + TypeScript + Express + Vite** for AI Studio. Complaints are automatically categorized with AI classification / smart keyword engines, assigned priority levels, and routed to specialized department agents.

---

## 📋 Features

- **Automated AI & Keyword Classification**: Classifies incoming complaints into departments (`Finance & Payroll`, `Technical Support`, `Logistics`, `Customer Care`), categories, and priorities (`HIGH`, `MEDIUM`, `LOW`, `NORMAL`).
- **Customer Portal**: Issue lodging with real-time AI auto-routing preview, complaint tracking, and personal submission history.
- **Agent Dashboard**: Department-specific queues, status progression (`OPEN`, `IN_PROGRESS`, `RESOLVED`), internal notes, resolution templates, and ticket deletion.
- **Interactive Analytics**: Department load metrics, ticket status distribution charts, and resolution throughput.
- **Preconfigured Agent Credentials**:
  - `finance@agent.company.com` / `finance123` → Finance & Payroll
  - `tech@agent.company.com` / `tech123` → Technical Support
  - `care@agent.company.com` / `care123` → Customer Care
  - `logistics@agent.company.com` / `logistics123` → Logistics

---

## 🚀 Tech Stack

- **Frontend**: React 19, TypeScript, Tailwind CSS, Lucide React, Recharts, Motion
- **Backend API**: Express 5, Node.js 22, Gemini AI SDK (`@google/genai`)
- **Build Tool**: Vite 8, esbuild, tsx

---

## 🛠️ API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Authenticate customer or department agent |
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/classify` | AI / keyword classification engine |
| GET | `/api/complaints` | Get all complaints |
| GET | `/api/complaints/:id` | Get complaint details |
| POST | `/api/complaints` | Submit a new complaint |
| GET | `/api/complaints/department/:dept` | Get complaints by department |
| GET | `/api/complaints/user/:userId` | Get complaints by user ID |
| GET | `/api/complaints/filter` | Filter complaints (department, status, priority, sort) |
| PUT | `/api/complaints/:id/status` | Update complaint status |
| PUT | `/api/complaints/:id/notes` | Update resolution notes |
| DELETE | `/api/complaints/:id` | Delete a complaint |
| GET | `/api/analytics` | System-wide performance & department metrics |
