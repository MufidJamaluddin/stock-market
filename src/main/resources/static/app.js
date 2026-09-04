const API = "/api";

const dashboardBody = document.getElementById("dashboardBody");
const gainersTable = document.querySelector("#gainersTable tbody");
const losersTable = document.querySelector("#losersTable tbody");
const searchInput = document.getElementById("searchInput");
const searchResults = document.getElementById("searchResults");
const detailModal = document.getElementById("detailModal");
const detailBody = document.getElementById("detailBody");
const closeModal = document.getElementById("closeModal");
const liveStatus = document.getElementById("liveStatus");

let rowsBySymbol = {};

function fmt(n) {
  if (n === null || n === undefined) return "-";
  return Number(n).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function changeClass(v) {
  return v >= 0 ? "up" : "down";
}

function renderDashboardRow(stock) {
  const tr = document.createElement("tr");
  tr.id = "row-" + stock.symbol;
  tr.onclick = () => openDetail(stock.symbol);
  tr.innerHTML = rowHtml(stock);
  return tr;
}

function rowHtml(stock) {
  const chg = stock.changePercent ?? 0;
  return `
    <td>${stock.symbol}</td>
    <td>${stock.companyName}</td>
    <td>${stock.sector}</td>
    <td>${stock.exchange}</td>
    <td class="price">${fmt(stock.lastPrice)}</td>
    <td>${fmt(stock.dayOpen)}</td>
    <td>${fmt(stock.dayHigh)}</td>
    <td>${fmt(stock.dayLow)}</td>
    <td>${(stock.dayVolume ?? 0).toLocaleString()}</td>
    <td class="${changeClass(chg)}">${chg >= 0 ? "+" : ""}${fmt(chg)}%</td>
  `;
}

async function loadDashboard() {
  const res = await fetch(`${API}/stocks`);
  const stocks = await res.json();
  dashboardBody.innerHTML = "";
  rowsBySymbol = {};
  stocks.forEach(s => {
    rowsBySymbol[s.symbol] = s;
    dashboardBody.appendChild(renderDashboardRow(s));
  });
}

function renderMiniTable(tbody, list) {
  tbody.innerHTML = "";
  list.forEach(s => {
    const tr = document.createElement("tr");
    tr.onclick = () => openDetail(s.symbol);
    const chg = s.changePercent ?? 0;
    tr.innerHTML = `<td>${s.symbol}</td><td>${fmt(s.lastPrice)}</td><td class="${changeClass(chg)}">${chg >= 0 ? "+" : ""}${fmt(chg)}%</td>`;
    tbody.appendChild(tr);
  });
}

async function loadMovers() {
  const [gainers, losers] = await Promise.all([
    fetch(`${API}/stocks/gainers?limit=5`).then(r => r.json()),
    fetch(`${API}/stocks/losers?limit=5`).then(r => r.json())
  ]);
  renderMiniTable(gainersTable, gainers);
  renderMiniTable(losersTable, losers);
}

// ---- Live updates via Server-Sent Events ----
function connectLiveStream() {
  const es = new EventSource(`${API}/stocks/stream`);
  es.onopen = () => liveStatus.classList.replace("offline", "online");
  es.onerror = () => liveStatus.classList.replace("online", "offline");
  es.onmessage = (evt) => {
    try {
      const tick = JSON.parse(evt.data);
      applyTick(tick);
    } catch (e) { /* ignore malformed events */ }
  };
}

function applyTick(tick) {
  const stock = rowsBySymbol[tick.symbol];
  if (!stock) return;
  stock.lastPrice = tick.price;
  stock.changePercent = tick.changePercent;
  stock.dayVolume = (stock.dayVolume ?? 0) + (tick.volume ?? 0);
  const row = document.getElementById("row-" + tick.symbol);
  if (row) {
    row.innerHTML = rowHtml(stock);
    row.onclick = () => openDetail(stock.symbol);
    row.classList.add("flash");
    setTimeout(() => row.classList.remove("flash"), 400);
  }
}

// ---- Search ----
let searchDebounce;
searchInput.addEventListener("input", () => {
  clearTimeout(searchDebounce);
  const q = searchInput.value.trim();
  if (!q) {
    searchResults.classList.add("hidden");
    return;
  }
  searchDebounce = setTimeout(() => runSearch(q), 250);
});

async function runSearch(q) {
  const res = await fetch(`${API}/search?q=${encodeURIComponent(q)}`);
  const results = await res.json();
  searchResults.innerHTML = "";
  if (results.length === 0) {
    searchResults.innerHTML = `<div class="search-result-item">No matches</div>`;
  } else {
    results.forEach(r => {
      const div = document.createElement("div");
      div.className = "search-result-item";
      const chg = r.changePercent ?? 0;
      div.innerHTML = `<span>${r.symbol} · ${r.companyName}</span><span class="${changeClass(chg)}">${fmt(r.lastPrice)}</span>`;
      div.onclick = () => { searchResults.classList.add("hidden"); openDetail(r.symbol); };
      searchResults.appendChild(div);
    });
  }
  searchResults.classList.remove("hidden");
}

document.addEventListener("click", (e) => {
  if (!e.target.closest(".search-box")) searchResults.classList.add("hidden");
});

// ---- Detail modal ----
async function openDetail(symbol) {
  detailModal.classList.remove("hidden");
  detailBody.innerHTML = "Loading...";
  const res = await fetch(`${API}/stocks/${symbol}`);
  if (!res.ok) {
    detailBody.innerHTML = "Stock not found.";
    return;
  }
  const d = await res.json();
  const chg = d.changePercent ?? 0;
  detailBody.innerHTML = `
    <h2>${d.symbol} <span class="${changeClass(chg)}">${chg >= 0 ? "+" : ""}${fmt(chg)}%</span></h2>
    <div>${d.companyName} · ${d.sector} · ${d.exchange}${d.fromCache ? " · <em>(cached)</em>" : ""}</div>
    <div class="detail-grid">
      <div><div class="label">Last Price</div>${fmt(d.lastPrice)} ${d.currency}</div>
      <div><div class="label">Day Open</div>${fmt(d.dayOpen)}</div>
      <div><div class="label">Day High</div>${fmt(d.dayHigh)}</div>
      <div><div class="label">Day Low</div>${fmt(d.dayLow)}</div>
      <div><div class="label">Volume</div>${(d.dayVolume ?? 0).toLocaleString()}</div>
      <div><div class="label">Change %</div>${fmt(d.changePercent)}%</div>
    </div>
    <h3>Recent Ticks</h3>
    <div id="historyList"></div>
  `;
  const historyList = document.getElementById("historyList");
  (d.recentHistory || []).forEach(h => {
    const row = document.createElement("div");
    row.className = "history-row";
    row.innerHTML = `<span>${new Date(h.ts).toLocaleTimeString()}</span><span>${fmt(h.price)}</span><span>${(h.volume ?? 0).toLocaleString()}</span>`;
    historyList.appendChild(row);
  });
}

closeModal.onclick = () => detailModal.classList.add("hidden");
detailModal.addEventListener("click", (e) => { if (e.target === detailModal) detailModal.classList.add("hidden"); });

// ---- Init ----
loadDashboard().then(loadMovers);
connectLiveStream();
setInterval(loadMovers, 15000); // refresh gainers/losers periodically
