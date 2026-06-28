export type Window = { start: string; end: string }; // "HH:mm" (UTC)
export type Availability = Record<string, Window[]>; // key = weekday 0(Sun)..6(Sat)

const toMin = (hhmm: string) => {
  const [h, m] = hhmm.split(":").map(Number);
  return h * 60 + m;
};
const pad = (n: number) => String(n).padStart(2, "0");
const fromMin = (min: number) => `${pad(Math.floor(min / 60))}:${pad(min % 60)}`;

/**
 * Free start times ("HH:mm", UTC) for a date: the weekday's open windows,
 * stepped by `stepMin`, with room for `durationMin`, minus times that overlap an
 * existing booking, minus times already past (when the date is today).
 */
export function computeSlots(opts: {
  date: string; // YYYY-MM-DD
  availability: Availability;
  durationMin: number;
  taken: { startAt: string; endAt: string }[];
  stepMin?: number;
  nowIso?: string;
}): string[] {
  const { date, availability, durationMin } = opts;
  const step = opts.stepMin ?? 30;
  const weekday = new Date(`${date}T00:00:00Z`).getUTCDay();
  const windows = availability[String(weekday)] ?? [];

  const takenIntervals = opts.taken.map((t) => {
    const s = new Date(t.startAt);
    const e = new Date(t.endAt);
    return [s.getUTCHours() * 60 + s.getUTCMinutes(), e.getUTCHours() * 60 + e.getUTCMinutes()] as const;
  });

  let nowMin = -1;
  if (opts.nowIso) {
    const now = new Date(opts.nowIso);
    if (now.toISOString().slice(0, 10) === date) nowMin = now.getUTCHours() * 60 + now.getUTCMinutes();
  }

  const slots: string[] = [];
  for (const w of windows) {
    const ws = toMin(w.start);
    const we = toMin(w.end);
    for (let t = ws; t + durationMin <= we; t += step) {
      if (t <= nowMin) continue;
      const overlaps = takenIntervals.some(([s, e]) => t < e && t + durationMin > s);
      if (!overlaps) slots.push(fromMin(t));
    }
  }
  return slots;
}
