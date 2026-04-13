// js/auth.js - Shared authentication and utility functions

const API_BASE = "http://localhost:8080";

// Save user info to session storage after login
function saveSession(user) {
    sessionStorage.setItem("userId", user.id);
    sessionStorage.setItem("userName", user.name);
    sessionStorage.setItem("userRole", user.role);
}

// Get current logged-in user info
function getSession() {
    return {
        userId: sessionStorage.getItem("userId"),
        userName: sessionStorage.getItem("userName"),
        userRole: sessionStorage.getItem("userRole")
    };
}

// Clear session and redirect to login
function logout() {
    sessionStorage.clear();
    window.location.href = "login.html";
}

// Guard: redirect to login if not logged in
function requireLogin() {
    const session = getSession();
    if (!session.userId) {
        window.location.href = "login.html";
        return null;
    }
    return session;
}

// Guard: redirect if wrong role
function requireRole(expectedRole) {
    const session = requireLogin();
    if (!session) return null;
    if (session.userRole !== expectedRole) {
        window.location.href = "login.html";
        return null;
    }
    return session;
}

// Show error message in a div
function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.textContent = message;
        el.style.display = "block";
    }
}

// Hide message div
function hideMessage(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.style.display = "none";
}

// Show success message
function showSuccess(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.textContent = message;
        el.style.display = "block";
    }
}

// Return a status badge HTML string
function statusBadge(status) {
    let cls = "badge-pending";
    if (status === "In Progress") cls = "badge-inprogress";
    else if (status === "Resolved") cls = "badge-resolved";
    return `<span class="badge ${cls}">${status}</span>`;
}

// Generic fetch wrapper - returns parsed JSON or throws error
async function apiFetch(url, options = {}) {
    const response = await fetch(API_BASE + url, {
        headers: { "Content-Type": "application/json" },
        ...options
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Request failed with status " + response.status);
    }

    // Some endpoints return empty body (DELETE, status update)
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }
    return null;
}