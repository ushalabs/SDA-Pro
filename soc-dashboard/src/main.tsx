import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  Bell,
  CheckCircle2,
  ClipboardList,
  DatabaseZap,
  FileClock,
  Gauge,
  Lock,
  Radio,
  Search,
  Shield,
  Siren,
  SlidersHorizontal,
  TerminalSquare,
  UserPlus
} from "lucide-react";
import { getJson, patchJson, postJson } from "./models/api";
import { useRealtime } from "./controllers/useRealtime";
import "./views/App.css";

type Screen =
  | "Main Dashboard"
  | "Live Alert Stream"
  | "Incident Queue"
  | "Incident Detail"
  | "Response Console"
  | "Threat Intel Lookup"
  | "Audit Logs"
  | "Settings";

type NavItem = { screen: Screen; icon: React.ReactNode };

const navItems: NavItem[] = [
  { screen: "Main Dashboard", icon: <Gauge size={18} /> },
  { screen: "Live Alert Stream", icon: <Radio size={18} /> },
  { screen: "Incident Queue", icon: <ClipboardList size={18} /> },
  { screen: "Incident Detail", icon: <Siren size={18} /> },
  { screen: "Response Console", icon: <TerminalSquare size={18} /> },
  { screen: "Threat Intel Lookup", icon: <Search size={18} /> },
  { screen: "Audit Logs", icon: <FileClock size={18} /> },
  { screen: "Settings", icon: <SlidersHorizontal size={18} /> }
];

function App() {
  const [auth, setAuth] = useState(() => localStorage.getItem("sda-pro-auth") === "active");
  const [screen, setScreen] = useState<Screen>("Main Dashboard");
  const [metrics, setMetrics] = useState<Record<string, unknown>>({});
  const [alerts, setAlerts] = useState<Record<string, unknown>[]>([]);
  const [incidents, setIncidents] = useState<Record<string, unknown>[]>([]);
  const [audit, setAudit] = useState<Record<string, unknown>[]>([]);
  const [selected, setSelected] = useState<Record<string, unknown> | null>(null);
  const [intel, setIntel] = useState<Record<string, unknown> | null>(null);
  const [busy, setBusy] = useState(false);
  const events = useRealtime();

  const refresh = async () => {
    const [metricData, alertData, incidentData, auditData] = await Promise.all([
      getJson("/dashboard/metrics"),
      getJson("/alerts"),
      getJson("/incidents"),
      getJson("/audit/events")
    ]);
    setMetrics(metricData);
    setAlerts(alertData);
    setIncidents(incidentData);
    setAudit(auditData);
    setSelected((current) => current ?? incidentData[0] ?? null);
  };

  useEffect(() => {
    refresh();
  }, []);

  async function runAction(action: () => Promise<unknown>) {
    setBusy(true);
    try {
      await action();
      await refresh();
    } finally {
      setBusy(false);
    }
  }

  const selectedIncident = selected ?? incidents[0] ?? null;
  const recentEvents = useMemo(() => events.map(sanitizeEvent), [events]);

  function completeAuth() {
    localStorage.setItem("sda-pro-auth", "active");
    setAuth(true);
  }

  function logout() {
    localStorage.removeItem("sda-pro-auth");
    setAuth(false);
    setScreen("Main Dashboard");
  }

  if (!auth) {
    return <AuthPortal onAuthenticated={completeAuth} />;
  }

  return (
    <div className="appShell">
      <aside className="sidebar">
        <div className="brandBlock">
          <div className="brandIcon"><Shield size={24} /></div>
          <div>
            <strong>SDA-Pro</strong>
            <span>Secure Operations</span>
          </div>
        </div>
        <nav className="navList">
          {navItems.map((item) => (
            <button className={screen === item.screen ? "active" : ""} onClick={() => setScreen(item.screen)} key={item.screen}>
              {item.icon}
              <span>{item.screen}</span>
            </button>
          ))}
        </nav>
        <div className="secureNote">
          <Lock size={16} />
          <span>IDs, payloads, credentials, and session tokens are redacted in analyst views.</span>
        </div>
      </aside>

      <main className="workspace">
        <header className="heroBar">
          <div>
            <p className="eyebrow">SOC command center</p>
            <h1>{screen}</h1>
          </div>
          <div className="statusPill">
            <span className="pulse" />
            Live demo stack online
          </div>
          <button className="logoutButton" onClick={logout}>Logout</button>
        </header>

        {screen === "Main Dashboard" && <Dashboard metrics={metrics} events={recentEvents} alerts={alerts} incidents={incidents} />}
        {screen === "Live Alert Stream" && (
          <AlertStream
            alerts={alerts}
            busy={busy}
            onSplunk={() => runAction(() => postJson("/ingest/webhook/splunk", { srcIp: "203.0.113.10", destHost: "finance-db", signature: "lateral-movement", severity: "CRITICAL" }))}
            onFirewall={() => runAction(() => postJson("/ingest/poll/firewall-demo"))}
          />
        )}
        {screen === "Incident Queue" && <IncidentQueue incidents={incidents} selected={selectedIncident} onPick={setSelected} />}
        {screen === "Incident Detail" && (
          <IncidentDetail
            incident={selectedIncident}
            busy={busy}
            onTriage={(id) => runAction(() => patchJson(`/incidents/${id}/state`, { action: "begin_triage", reason: "analyst triage" }))}
          />
        )}
        {screen === "Response Console" && (
          <ResponseConsole
            incident={selectedIncident}
            busy={busy}
            onRespond={(id) => runAction(() => postJson(`/incidents/${id}/respond`))}
          />
        )}
        {screen === "Threat Intel Lookup" && (
          <ThreatIntelLookup
            intel={intel}
            busy={busy}
            onLookup={() => runAction(async () => setIntel(await postJson("/threat-intel/reputation", { indicator: "203.0.113.10", type: "IP" })))}
          />
        )}
        {screen === "Audit Logs" && <AuditLogs audit={audit} />}
        {screen === "Settings" && <SettingsScreen />}
      </main>
    </div>
  );
}

function AuthPortal({ onAuthenticated }: { onAuthenticated: () => void }) {
  const [mode, setMode] = useState<"login" | "signup">("login");
  const [name, setName] = useState("SOC Analyst");
  const [email, setEmail] = useState("analyst@sda-pro.local");
  const [password, setPassword] = useState("password");
  const [confirm, setConfirm] = useState("password");
  const [message, setMessage] = useState("");

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setMessage("");
    if (mode === "signup" && password !== confirm) {
      setMessage("Passwords must match before creating an analyst account.");
      return;
    }
    if (mode === "login") {
      await postJson("/auth/login", { email, password });
    }
    onAuthenticated();
  }

  return (
    <section className="authPage">
      <div className="authIntro">
        <div className="brandIcon"><Shield size={28} /></div>
        <p className="eyebrow">SDA-Pro secure access</p>
        <h1>Security operations, protected from the first screen.</h1>
        <div className="authProof">
          <span><Lock size={16} /> Redacted session data</span>
          <span><Shield size={16} /> Mock integrations only</span>
          <span><Bell size={16} /> Realtime SOC events</span>
        </div>
      </div>
      <form className="loginCard" onSubmit={submit}>
        <div className="lockBadge">{mode === "login" ? <Lock size={22} /> : <UserPlus size={22} />}</div>
        <div className="authTabs">
          <button type="button" className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Login</button>
          <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>Sign up</button>
        </div>
        <h2>{mode === "login" ? "Analyst Login" : "Create Analyst"}</h2>
        <p>{mode === "login" ? "Sign in to enter the SOC dashboard." : "Create a demo analyst account for the presentation flow."}</p>
        {mode === "signup" && <label>Name<input value={name} onChange={(event) => setName(event.target.value)} /></label>}
        <label>Email<input value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" /></label>
        {mode === "signup" && <label>Confirm Password<input value={confirm} onChange={(event) => setConfirm(event.target.value)} type="password" /></label>}
        {message && <div className="formAlert">{message}</div>}
        <button className="primary full" type="submit">{mode === "login" ? "Login to SOC" : "Create account"}</button>
        {mode === "login" && <small className="demoHint">Demo: analyst@sda-pro.local / password</small>}
      </form>
    </section>
  );
}

function Dashboard({ metrics, events, alerts, incidents }: { metrics: Record<string, unknown>; events: string[]; alerts: Record<string, unknown>[]; incidents: Record<string, unknown>[] }) {
  return (
    <>
      <section className="metricGrid">
        <Metric title="Alerts processed" value={metrics.alerts} icon={<Activity />} tone="blue" />
        <Metric title="Open incidents" value={metrics.openIncidents} icon={<Siren />} tone="red" />
        <Metric title="Total incidents" value={metrics.incidents} icon={<ClipboardList />} tone="violet" />
        <Metric title="MTTD" value={metrics.mttd} icon={<Gauge />} tone="green" />
        <Metric title="MTTR" value={metrics.mttr} icon={<CheckCircle2 />} tone="amber" />
      </section>
      <section className="dashboardGrid">
        <Panel title="Operational Picture" subtitle="Curated analyst summary">
          <div className="signalRows">
            <Signal label="Highest alert severity" value={maxSeverity(alerts)} tone={severityTone(maxSeverity(alerts))} />
            <Signal label="Active lifecycle stage" value={formatState(incidents[0]?.current_state ?? "No active incident")} tone="blue" />
            <Signal label="Mock integrations" value="Splunk, Firewall, VT, MISP ready" tone="green" />
          </div>
        </Panel>
        <Panel title="Realtime Event Feed" subtitle="Sensitive payloads redacted">
          {events.length ? events.map((event, index) => <div className="eventLine" key={index}>{event}</div>) : <EmptyState text="No live events yet. Trigger a mock alert to populate the stream." />}
        </Panel>
      </section>
    </>
  );
}

function AlertStream({ alerts, busy, onSplunk, onFirewall }: { alerts: Record<string, unknown>[]; busy: boolean; onSplunk: () => void; onFirewall: () => void }) {
  return (
    <>
      <div className="commandBar">
        <button className="primary" disabled={busy} onClick={onSplunk}>Mock Splunk Alert</button>
        <button className="secondary" disabled={busy} onClick={onFirewall}>Run Firewall Poll</button>
      </div>
      <div className="listGrid">
        {alerts.map((alert) => (
          <article className="recordCard" key={String(alert.id)}>
            <div className="recordTop">
              <SeverityBadge value={String(alert.severity ?? "LOW")} />
              <span className="maskedId">{maskId(alert.id)}</span>
            </div>
            <h3>{sourceName(alert)} alert</h3>
            <p>{safePayloadSummary(alert.normalized_payload_json, "asset")} flagged by {sourceName(alert)} normalization.</p>
            <div className="recordMeta">
              <span>Status: {String(alert.status ?? "INGESTED")}</span>
              <span>Received: {formatDate(alert.received_at)}</span>
            </div>
          </article>
        ))}
      </div>
    </>
  );
}

function IncidentQueue({ incidents, selected, onPick }: { incidents: Record<string, unknown>[]; selected: Record<string, unknown> | null; onPick: (incident: Record<string, unknown>) => void }) {
  if (!incidents.length) return <EmptyPanel text="No incidents yet. Trigger a mock alert first." />;
  return (
    <div className="kanban">
      {incidents.map((incident) => (
        <button className={`incidentTile ${selected?.id === incident.id ? "selected" : ""}`} onClick={() => onPick(incident)} key={String(incident.id)}>
          <div className="recordTop">
            <SeverityBadge value={String(incident.severity ?? "LOW")} />
          <span>{formatState(incident.current_state ?? "NEW")}</span>
          </div>
          <h3>{String(incident.title ?? "Security incident")}</h3>
          <p>{String(incident.description ?? "Correlated alert requiring analyst review")}</p>
          <span className="maskedId">Case {maskId(incident.id)}</span>
        </button>
      ))}
    </div>
  );
}

function IncidentDetail({ incident, busy, onTriage }: { incident: Record<string, unknown> | null; busy: boolean; onTriage: (id: string) => void }) {
  if (!incident) return <EmptyPanel text="Select an incident from the queue." />;
  const id = String(incident.id);
  return (
    <section className="detailGrid">
      <Panel title={String(incident.title ?? "Incident")} subtitle={`Case ${maskId(id)}`}>
        <div className="incidentHero">
          <SeverityBadge value={String(incident.severity ?? "LOW")} />
          <h2>{formatState(incident.current_state ?? "NEW")}</h2>
          <p>{String(incident.description ?? "No analyst description available.")}</p>
        </div>
      </Panel>
      <Panel title="Protected Case Data" subtitle="Internal IDs hidden from analyst workflow">
        <div className="facts">
          <Fact label="Affected asset" value={String(incident.affected_asset ?? "Unknown asset")} />
          <Fact label="Created" value={formatDate(incident.created_at)} />
          <Fact label="Updated" value={formatDate(incident.updated_at)} />
          <Fact label="Internal reference" value={maskId(id)} />
        </div>
      </Panel>
      <Panel title="Lifecycle Action" subtitle="State pattern demonstration">
        <button className="primary" disabled={busy || String(incident.current_state) !== "NEW"} onClick={() => onTriage(id)}>Begin Triage</button>
      </Panel>
    </section>
  );
}

function ResponseConsole({ incident, busy, onRespond }: { incident: Record<string, unknown> | null; busy: boolean; onRespond: (id: string) => void }) {
  if (!incident) return <EmptyPanel text="Select an incident before executing a response plan." />;
  return (
    <section className="responsePanel">
      <Panel title="Recommended Response Plan" subtitle="Strategy + Factory + Decorator + Proxy">
        <div className="playbook">
          <Step title="Select strategy" text="Critical cases use aggressive containment." />
          <Step title="Create actions" text="Block IP, isolate endpoint, and escalate are generated from the factory." />
          <Step title="Apply guards" text="Approval, audit, rollback, metrics, and authorization wrappers protect execution." />
        </div>
        <button className="primary" disabled={busy} onClick={() => onRespond(String(incident.id))}>Execute Protected Response</button>
      </Panel>
      <Panel title="Selected Case" subtitle={`Case ${maskId(incident.id)}`}>
        <div className="facts">
          <Fact label="Asset" value={String(incident.affected_asset ?? "Unknown")} />
          <Fact label="Severity" value={String(incident.severity ?? "LOW")} />
          <Fact label="State" value={formatState(incident.current_state ?? "NEW")} />
        </div>
      </Panel>
    </section>
  );
}

function ThreatIntelLookup({ intel, busy, onLookup }: { intel: Record<string, unknown> | null; busy: boolean; onLookup: () => void }) {
  return (
    <section className="responsePanel">
      <Panel title="Threat Intel Reputation" subtitle="Adapter + Proxy with cached, redacted lookup">
        <button className="primary" disabled={busy} onClick={onLookup}>Lookup Redacted Demo Indicator</button>
        {intel ? (
          <div className="intelResult">
            <DatabaseZap />
            <div>
              <strong>{String(intel.verdict ?? "UNKNOWN")}</strong>
              <span>Score {String(intel.score ?? "-")} via {String(intel.provider ?? "mock provider")}</span>
              <small>Indicator {maskIndicator(intel.indicator)}</small>
            </div>
          </div>
        ) : <EmptyState text="Run a lookup to see the protected reputation result." />}
      </Panel>
      <Panel title="Cache Protection" subtitle="No external API keys used">
        <div className="secureList">
          <span><Lock size={16} /> Provider credentials are mocked and hidden.</span>
          <span><Shield size={16} /> Repeat lookups are served through the proxy cache.</span>
          <span><Bell size={16} /> ThreatIntelUpdated events are published for subscribers.</span>
        </div>
      </Panel>
    </section>
  );
}

function AuditLogs({ audit }: { audit: Record<string, unknown>[] }) {
  return (
    <div className="auditList">
      {audit.map((entry, index) => (
        <article className="auditItem" key={`${entry.id}-${index}`}>
          <div>
            <strong>{String(entry.event_type ?? "AuditEvent")}</strong>
            <p>{auditSummary(entry)}</p>
          </div>
          <span>{formatDate(entry.created_at)}</span>
        </article>
      ))}
    </div>
  );
}

function SettingsScreen() {
  const integrations = ["Splunk", "CrowdStrike", "Firewall", "VirusTotal", "MISP", "Slack", "PagerDuty", "Email", "Active Directory"];
  return (
    <section className="settingsGrid">
      {integrations.map((name) => (
        <article className="integrationCard" key={name}>
          <div className="integrationIcon"><Lock size={18} /></div>
          <h3>{name}</h3>
          <p>Mock integration enabled. API keys are not stored in the browser.</p>
          <span>Credential status: protected</span>
        </article>
      ))}
    </section>
  );
}

function Panel({ title, subtitle, children }: { title: string; subtitle: string; children: React.ReactNode }) {
  return <section className="panel"><div className="panelHeader"><div><h2>{title}</h2><p>{subtitle}</p></div></div>{children}</section>;
}

function Metric({ title, value, icon, tone }: { title: string; value: unknown; icon: React.ReactNode; tone: string }) {
  return <article className={`metric metric-${tone}`}><div>{icon}</div><span>{title}</span><strong>{String(value ?? "-")}</strong></article>;
}

function Signal({ label, value, tone }: { label: string; value: string; tone: string }) {
  return <div className="signal"><span>{label}</span><strong className={`text-${tone}`}>{value}</strong></div>;
}

function SeverityBadge({ value }: { value: string }) {
  return <span className={`severity severity-${value.toLowerCase()}`}>{value}</span>;
}

function Fact({ label, value }: { label: string; value: string }) {
  return <div className="fact"><span>{label}</span><strong>{value}</strong></div>;
}

function Step({ title, text }: { title: string; text: string }) {
  return <div className="step"><CheckCircle2 size={18} /><div><strong>{title}</strong><span>{text}</span></div></div>;
}

function EmptyPanel({ text }: { text: string }) {
  return <section className="panel"><EmptyState text={text} /></section>;
}

function EmptyState({ text }: { text: string }) {
  return <div className="emptyState">{text}</div>;
}

function maskId(value: unknown) {
  const raw = String(value ?? "");
  if (!raw) return "protected";
  return `...${raw.slice(-6)}`;
}

function maskIndicator(value: unknown) {
  const raw = String(value ?? "");
  if (!raw) return "redacted";
  const parts = raw.split(".");
  if (parts.length === 4) return `${parts[0]}.${parts[1]}.x.x`;
  return `${raw.slice(0, 3)}***`;
}

function sanitizeEvent(event: string) {
  return event
    .replace(/[0-9a-f]{8}-[0-9a-f-]{27,}/gi, "[case-id-redacted]")
    .replace(/203\.0\.113\.\d+/g, "203.0.113.x")
    .replace(/198\.51\.100\.\d+/g, "198.51.100.x")
    .replace(/payload=\{.*\}/, "payload=[redacted]");
}

function formatDate(value: unknown) {
  if (!value) return "Pending";
  const date = new Date(String(value));
  if (Number.isNaN(date.getTime())) return "Pending";
  return date.toLocaleString([], { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}

function sourceName(alert: Record<string, unknown>) {
  const payload = parsePayload(alert.normalized_payload_json);
  return String(payload.sourceType ?? "security source").toUpperCase();
}

function safePayloadSummary(payload: unknown, key: string) {
  const parsed = parsePayload(payload);
  return String(parsed[key] ?? "Protected asset");
}

function parsePayload(payload: unknown): Record<string, unknown> {
  if (typeof payload !== "string") return {};
  try {
    return JSON.parse(payload);
  } catch {
    return {};
  }
}

function maxSeverity(rows: Record<string, unknown>[]) {
  const order = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
  return rows.map((row) => String(row.severity ?? "LOW")).sort((a, b) => order.indexOf(b) - order.indexOf(a))[0] ?? "LOW";
}

function severityTone(value: string) {
  return value === "CRITICAL" ? "red" : value === "HIGH" ? "amber" : "green";
}

function auditSummary(entry: Record<string, unknown>) {
  const entity = String(entry.entity_type ?? "system");
  return `${entity} event recorded. Actor and entity references are protected for analyst display.`;
}

function formatState(value: unknown) {
  return String(value).replace(/_/g, " ");
}

createRoot(document.getElementById("root")!).render(<App />);
