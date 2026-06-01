export const API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
export async function getJson(path: string) { const res = await fetch(API + path); return res.json(); }
export async function postJson(path: string, body: unknown = {}) { const res = await fetch(API + path, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) }); return res.json(); }
export async function patchJson(path: string, body: unknown = {}) { const res = await fetch(API + path, { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) }); return res.json(); }
