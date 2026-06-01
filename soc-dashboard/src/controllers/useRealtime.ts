import { useEffect, useState } from "react";
import { API } from "../models/api";

export function useRealtime() {
  const [events, setEvents] = useState<string[]>([]);
  useEffect(() => {
    const es = new EventSource(API + "/dashboard/events");
    const add = (name: string) => (event: MessageEvent) => setEvents(prev => [name + ": " + event.data, ...prev].slice(0, 8));
    ["AlertIngested","AlertEnriched","IncidentCreated","IncidentStateChanged","ResponseActionExecuted","ThreatIntelUpdated","NotificationDispatched"].forEach(name => es.addEventListener(name, add(name)));
    return () => es.close();
  }, []);
  return events;
}
