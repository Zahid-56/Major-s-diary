/* Class portal for students: notes, quizzes and announcements from the CR. */
(() => {
  const C = window.MD;
  const icon = {
    file: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h4"/></svg>',
    link: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 14a5 5 0 007 0l3-3a5 5 0 00-7-7l-1 1"/><path d="M14 10a5 5 0 00-7 0l-3 3a5 5 0 007 7l1-1"/></svg>',
    quiz: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M9.5 9a2.5 2.5 0 015 .5c0 1.5-2.5 2-2.5 3.5M12 17h.01"/></svg>'
  };
  const state = { notes: [], quizzes: [], anns: [], subject: "All", view: get("md-class-view", "notes"), loaded: false };
  const lastSeen = get("md-class-seen", 0);

  async function api(path, opts = {}) {
    const r = await fetch(C.url + "/rest/v1/" + path, {
      ...opts,
      headers: { apikey: C.key, "Content-Type": "application/json", ...(opts.headers || {}) }
    });
    const txt = await r.text();
    const data = txt ? JSON.parse(txt) : null;
    if (!r.ok) throw new Error((data && (data.message || data.hint)) || "Network error");
    return data;
  }
  const fileUrl = (p, name) => C.url + "/storage/v1/object/public/notes/" + p.split("/").map(encodeURIComponent).join("/") + (name ? "?download=" + encodeURIComponent(name) : "");
  const when = d => new Intl.DateTimeFormat(undefined, { day: "numeric", month: "short", hour: "numeric", minute: "2-digit" }).format(new Date(d));
  const size = b => !b ? "" : b > 1048576 ? (b / 1048576).toFixed(1) + " MB" : Math.max(1, Math.round(b / 1024)) + " KB";
  const isNew = d => lastSeen && new Date(d).getTime() > lastSeen;
  const joined = () => (get("md-class", "") || "").toUpperCase() === C.classCode;

  /* ---------- tabs ---------- */
  function showTab(t) {
    $("tabRoutine").classList.toggle("on", t === "routine");
    $("tabClass").classList.toggle("on", t === "class");
    $("routineView").hidden = t !== "routine";
    $("classView").hidden = t !== "class";
    put("md-tab", t);
    if (t === "class") { renderClass(); load(); }
  }
  $("tabRoutine").onclick = () => showTab("routine");
  $("tabClass").onclick = () => showTab("class");

  /* ---------- data ---------- */
  async function load() {
    if (!joined()) return;
    try {
      const [notes, quizzes, anns] = await Promise.all([
        api("notes?select=*&order=created_at.desc"),
        api("quizzes?select=id,subject,title,description,time_limit_min,due_at,questions,created_at&published=eq.true&order=created_at.desc"),
        api("announcements?select=*&order=created_at.desc&limit=3")
      ]);
      Object.assign(state, { notes, quizzes, anns, loaded: true });
      renderClass();
      put("md-class-seen", Date.now());
    } catch (e) {
      state.loaded = true; state.error = true; renderClass();
    }
  }
  async function peek() {   // red dot on the Class tab when something new arrived
    if (!joined()) return;
    try {
      const [n, q] = await Promise.all([
        api("notes?select=created_at&order=created_at.desc&limit=1"),
        api("quizzes?select=created_at&published=eq.true&order=created_at.desc&limit=1")
      ]);
      const latest = Math.max(...[...n, ...q].map(x => new Date(x.created_at).getTime()), 0);
      $("classDot").classList.toggle("show", latest > (get("md-class-seen", 0) || 0) && $("classView").hidden);
    } catch (e) {}
  }

  /* ---------- render ---------- */
  function renderClass() {
    const v = $("classView");
    if (!joined()) {
      v.innerHTML = `<div class="join">
        <div class="eyebrow">Class portal</div>
        <h3>Join your class</h3>
        <p>Enter the class code your CR shared to get notes, quizzes and reminders.</p>
        <input id="joinCode" maxlength="12" placeholder="CODE" autocomplete="off" autocapitalize="characters">
        <div class="err" id="joinErr"></div>
        <button class="btn gold" id="joinBtn" style="width:100%">Join class</button>
      </div>`;
      const go = () => {
        const c = $("joinCode").value.trim().toUpperCase().replace(/\s+/g, "");
        if (c !== C.classCode) { $("joinErr").textContent = "That code isn't right — ask your CR."; return; }
        put("md-class", c); try { window.MajorsDiary && window.MajorsDiary.setClassJoined(true); } catch (e) {}
        toast("Welcome to " + C.className); renderClass(); load();
      };
      $("joinBtn").onclick = go; $("joinCode").onkeydown = e => { if (e.key === "Enter") go(); };
      return;
    }
    const subjects = ["All", ...C.subjects];
    const pick = x => state.subject === "All" || x.subject === state.subject;
    const notes = state.notes.filter(pick), quizzes = state.quizzes.filter(pick);
    const inApp = !!window.MajorsDiary;

    v.innerHTML = `
      <div class="cls-head"><div><small>Class portal</small><h2>${esc(C.className)}</h2></div>
        <button class="icon-btn" id="clsRefresh" aria-label="Refresh"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 11-3-6.7L21 8"/><path d="M21 3v5h-5"/></svg></button></div>
      ${state.anns.map(a => `<div class="ann"><small>From your CR · ${when(a.created_at)}</small>${esc(a.message)}</div>`).join("")}
      <div class="chips">${subjects.map(s => `<button class="chip${s === state.subject ? " on" : ""}" data-s="${esc(s)}">${esc(s === "All" ? "All" : C.short[s] || s)}</button>`).join("")}</div>
      <div class="seg"><button data-v="notes" class="${state.view === "notes" ? "on" : ""}">Notes · ${notes.length}</button><button data-v="quizzes" class="${state.view === "quizzes" ? "on" : ""}">Quizzes · ${quizzes.length}</button></div>
      <div id="clsList">${!state.loaded ? '<div class="muted">Loading…</div>' : state.error && !state.notes.length ? '<div class="muted">Can\'t reach the class right now. Check your internet and tap refresh.</div>' : state.view === "notes" ? notesHtml(notes) : quizHtml(quizzes)}</div>
      ${inApp ? "" : '<div class="notif"><span>Get class reminders and alerts for new notes &amp; quizzes on your phone.</span><a class="btn gold" style="text-decoration:none" href="https://github.com/Zahid-56/Major-s-diary/releases/latest/download/MajorsDiary.apk">Get app</a></div>'}
      <div style="text-align:center;margin-top:14px"><button class="btn ghost" id="leaveCls" style="font-size:12px;padding:7px 12px">Leave class</button></div>
      <a class="cr-link" href="admin.html">Are you the CR? <b>Open CR panel →</b></a>`;

    $("clsRefresh").onclick = () => { state.loaded = false; renderClass(); load(); };
    v.querySelectorAll(".chip").forEach(b => b.onclick = () => { state.subject = b.dataset.s; renderClass(); });
    v.querySelectorAll(".seg button").forEach(b => b.onclick = () => { state.view = b.dataset.v; put("md-class-view", state.view); renderClass(); });
    v.querySelectorAll("[data-quiz]").forEach(b => b.onclick = () => openQuiz(state.quizzes.find(q => q.id === b.dataset.quiz)));
    $("leaveCls").onclick = () => { if (confirm("Leave this class on this device?")) { put("md-class", null); try { window.MajorsDiary && window.MajorsDiary.setClassJoined(false); } catch (e) {} renderClass(); } };
  }

  function notesHtml(list) {
    if (!list.length) return '<div class="muted">No notes yet. Your CR will upload them here.</div>';
    return list.map(n => {
      const href = n.file_path ? fileUrl(n.file_path, n.file_name) : n.link;
      return `<div class="card">
        <div class="ic">${n.file_path ? icon.file : icon.link}</div>
        <div><div class="sub">${esc(C.short[n.subject] || n.subject)}${isNew(n.created_at) ? '<span class="new">NEW</span>' : ""}</div>
          <b>${esc(n.title)}</b>${n.description ? `<p>${esc(n.description)}</p>` : ""}
          <div class="meta">${when(n.created_at)}${n.file_size ? " · " + size(n.file_size) : ""}</div></div>
        ${href ? `<a class="go" href="${esc(href)}" target="_blank" rel="noopener">${n.file_path ? "Download" : "Open"}</a>` : ""}
      </div>`;
    }).join("");
  }

  function quizHtml(list) {
    if (!list.length) return '<div class="muted">No quizzes yet.</div>';
    return list.map(q => {
      const mine = get("md-quiz-" + q.id, null);
      const closed = q.due_at && Date.now() > new Date(q.due_at).getTime();
      const btn = mine ? `<span class="go done">${mine.score}/${mine.total} ✓</span>`
        : closed ? '<span class="go closed">Closed</span>'
        : `<button class="go" data-quiz="${q.id}">Start</button>`;
      return `<div class="card">
        <div class="ic">${icon.quiz}</div>
        <div><div class="sub">${esc(C.short[q.subject] || q.subject)}${isNew(q.created_at) ? '<span class="new">NEW</span>' : ""}</div>
          <b>${esc(q.title)}</b>${q.description ? `<p>${esc(q.description)}</p>` : ""}
          <div class="meta">${q.questions.length} questions${q.time_limit_min ? " · " + q.time_limit_min + " min" : ""}${q.due_at ? " · due " + when(q.due_at) : ""}</div></div>
        ${btn}
      </div>`;
    }).join("");
  }

  /* ---------- taking a quiz ---------- */
  let timer = null;
  function openQuiz(q) {
    if (!q) return;
    const me = get("md-student", { name: "", roll: "" });
    const sheet = $("qzSheet"), bgEl = $("qzSheetBg");
    $("qzTitle").textContent = q.title;
    $("qzSub").textContent = (C.short[q.subject] || q.subject) + " · " + q.questions.length + " questions";
    $("qzBody").innerHTML = `
      <div><span class="lbl">Your name</span><input class="fld" id="qzName" value="${esc(me.name)}" placeholder="e.g. Ali Khan"></div>
      <div><span class="lbl">Roll number</span><input class="fld" id="qzRoll" value="${esc(me.roll)}" placeholder="e.g. F23-0391"></div>
      ${q.questions.map((x, i) => `<div class="qcard"><h4><span>Q${i + 1}.</span>${esc(x.q)}</h4>
        ${x.options.map((o, j) => `<label class="opt-a"><input type="radio" name="q${i}" value="${j}">${esc(o)}</label>`).join("")}</div>`).join("")}`;
    $("qzFoot").innerHTML = `<span class="timer" id="qzTimer"></span><span class="sp"></span><button class="btn gold" id="qzSubmit">Submit</button>`;
    $("qzSubmit").onclick = () => submit(q, false);
    sheet.classList.add("open"); bgEl.classList.add("open"); document.body.style.overflow = "hidden";
    clearInterval(timer);
    if (q.time_limit_min) {
      const end = Date.now() + q.time_limit_min * 60000;
      const tick = () => {
        const left = Math.max(0, end - Date.now()), m = Math.floor(left / 60000), s = Math.floor(left / 1000) % 60;
        $("qzTimer").textContent = "⏱ " + m + ":" + String(s).padStart(2, "0");
        $("qzTimer").classList.toggle("low", left < 60000);
        if (left <= 0) { clearInterval(timer); submit(q, true); }
      };
      tick(); timer = setInterval(tick, 1000);
    } else $("qzTimer").style.display = "none";
  }
  function closeQuiz() {
    if ($("qzSubmit") && !confirm("Leave the quiz? Your answers will be lost.")) return;
    clearInterval(timer); $("qzSheet").classList.remove("open"); $("qzSheetBg").classList.remove("open"); document.body.style.overflow = "";
  }
  async function submit(q, auto) {
    const name = $("qzName").value.trim(), roll = $("qzRoll").value.trim().toUpperCase();
    if (!name || !roll) { toast("Enter your name and roll number"); if (!auto) return; }
    const answers = q.questions.map((_, i) => { const c = document.querySelector(`input[name="q${i}"]:checked`); return c ? +c.value : null; });
    const blank = answers.filter(a => a === null).length;
    if (!auto && blank && !confirm(blank + " question(s) unanswered. Submit anyway?")) return;
    put("md-student", { name, roll });
    $("qzSubmit").disabled = true; $("qzSubmit").textContent = "Submitting…";
    try {
      const r = await api("rpc/submit_quiz", { method: "POST", body: JSON.stringify({ p_quiz: q.id, p_name: name, p_roll: roll, p_answers: answers }) });
      clearInterval(timer);
      put("md-quiz-" + q.id, r);
      const pct = Math.round(r.score / Math.max(1, r.total) * 100);
      $("qzBody").innerHTML = `<div class="result"><div class="big">${r.score}/${r.total}</div>
        <h3 style="font-family:Fraunces,Georgia,serif;margin:12px 0 4px">${pct >= 80 ? "Excellent! 🎉" : pct >= 50 ? "Well done 👍" : "Keep practising 💪"}</h3>
        <p>Your result has been sent to the CR.</p></div>`;
      $("qzFoot").innerHTML = `<span class="sp"></span><button class="btn gold" id="qzDone">Done</button>`;
      $("qzDone").onclick = () => { $("qzSheet").classList.remove("open"); $("qzSheetBg").classList.remove("open"); document.body.style.overflow = ""; renderClass(); };
    } catch (e) {
      toast(e.message || "Couldn't submit — try again");
      if (/already submitted/i.test(e.message || "")) put("md-quiz-" + q.id, { score: "–", total: "–" });
      $("qzSubmit").disabled = false; $("qzSubmit").textContent = "Submit";
    }
  }
  $("qzClose").onclick = closeQuiz;

  /* ---------- start ---------- */
  try { if (joined() && window.MajorsDiary) window.MajorsDiary.setClassJoined(true); } catch (e) {}
  const start = location.hash === "#class" ? "class" : get("md-tab", "routine");
  showTab(start);
  peek();
  setInterval(() => { if (!document.hidden) { $("classView").hidden ? peek() : load(); } }, 5 * 60 * 1000);
  addEventListener("hashchange", () => { if (location.hash === "#class") showTab("class"); });
})();
