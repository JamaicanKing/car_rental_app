/* Shared helpers used by both pages: API base, session storage, fetch wrapper. */

const API = ''; // same-origin — the Java server serves this file and the API

const Session = {
  save(user) { localStorage.setItem('carrental_user', JSON.stringify(user)); },
  get() {
    const raw = localStorage.getItem('carrental_user');
    return raw ? JSON.parse(raw) : null;
  },
  clear() { localStorage.removeItem('carrental_user'); },
};

async function apiRequest(path, options = {}) {
  const res = await fetch(API + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : {};
  if (!res.ok) {
    throw new Error(data.error || 'Something went wrong. Please try again.');
  }
  return data;
}

function money(amount) {
  return '$' + Number(amount).toFixed(2);
}

function formatDate(iso) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}
