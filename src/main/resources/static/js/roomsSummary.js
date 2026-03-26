/* ═══════════════════════════════════════════
   SmartStay · roomsSummary.js
   src/main/resources/static/js/roomsSummary.js

   Gọi REST API: GET /room-posted
   Response trả về List<RoomPostSummaryResponse>
   ═══════════════════════════════════════════ */

'use strict';

/* ─── Cấu hình ─── */
const API_URL   = '/room-posted';
const PAGE_SIZE = 9;

/* ─── State ─── */
let allRooms    = [];
let filtered    = [];
let currentPage = 1;
let isListView  = false;
let searchTerm  = '';
let filters     = { type: '', price: '', sort: 'newest' };

/* ─── DOM ─── */
const $       = id => document.getElementById(id);
const grid    = $('roomGrid');
const pager   = $('pagination');
const counter = $('resultsCount');
const tagsEl  = $('activeTags');
const heroCount = $('heroCount');

/* ═══════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════ */
const fmtShort = n => {
    if (n == null) return '—';
    n = Number(n);
    if (n >= 1_000_000) {
        const v = n / 1_000_000;
        return (Number.isInteger(v) ? v : v.toFixed(1)) + ' tr';
    }
    if (n >= 1_000) return Math.round(n / 1_000) + 'k';
    return String(n);
};

// Đã fix lỗi "sec is not defined" ở đây
const timeAgo = iso => {
    if (!iso) return '';
    const seconds = (Date.now() - new Date(iso).getTime()) / 1000;

    if (seconds < 60)     return 'Vừa đăng';
    if (seconds < 3600)   return `${Math.floor(seconds / 60)} phút trước`;
    if (seconds < 86400)  return `${Math.floor(seconds / 3600)} giờ trước`;
    if (seconds < 604800) return `${Math.floor(seconds / 86400)} ngày trước`;

    return new Date(iso).toLocaleDateString('vi-VN');
};

const typeLabel = t => ({
    PHONG_TRO:      'Phòng trọ',
    CHUNG_CU_MINI:  'Chung cư mini',
    CHUNG_CU:       'Chung cư',
    STUDIO:         'Studio',
    NHA_NGUYEN_CAN: 'Nhà nguyên căn',
}[t] || t || 'Khác');

const defaultAvatar = name =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'U')}&background=fde8d8&color=d4611c&size=60`;

const esc = s => !s ? '' : String(s)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');

const debounce = (fn, ms) => {
    let t;
    return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); };
};

/* ═══════════════════════════════════════════
   FETCH REST API
   ═══════════════════════════════════════════ */
async function loadRooms() {
    showSkeletons();
    try {
        const res = await fetch(API_URL);
        if (!res.ok) throw new Error(`Server trả về lỗi: ${res.status}`);
        allRooms = await res.json();

        heroCount.textContent = `${allRooms.length}+ phòng đang cho thuê`;
        applyFilters();
    } catch (err) {
        showError(err.message);
    }
}

/* ═══════════════════════════════════════════
   FILTER + SORT (client-side)
   ═══════════════════════════════════════════ */
function applyFilters() {
    const q = searchTerm.toLowerCase().trim();
    let mn = 0, mx = Infinity;
    if (filters.price) [mn, mx] = filters.price.split('-').map(Number);

    filtered = allRooms.filter(r => {
        if (filters.type && r.roomType !== filters.type) return false;
        const rent = Number(r.monthlyRent) || 0;
        if (filters.price && (rent < mn || rent > mx)) return false;

        if (q) {
            const hay = `${r.title || ''} ${r.shortAddress || ''}`.toLowerCase();
            if (!hay.includes(q)) return false;
        }
        return true;
    });

    if (filters.sort === 'price_asc')  filtered.sort((a, b) => Number(a.monthlyRent) - Number(b.monthlyRent));
    if (filters.sort === 'price_desc') filtered.sort((a, b) => Number(b.monthlyRent) - Number(a.monthlyRent));
    if (filters.sort === 'newest')     filtered.sort((a, b) => new Date(b.publishedAt || 0) - new Date(a.publishedAt || 0));

    currentPage = 1;
    renderPage();
    renderPager();
    renderCounter();
    renderTags();
}

/* ═══════════════════════════════════════════
   RENDER
   ═══════════════════════════════════════════ */
function renderPage() {
    if (filtered.length === 0) { showEmpty(); return; }
    const slice = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);
    grid.className = isListView ? 'room-grid list-view' : 'room-grid';
    grid.innerHTML = slice.map(buildCard).join('');
}

// Đã fix lỗi copy đè và cập nhật link "/rooms/${r.id}"
function buildCard(r) {
    const img = r.thumbnailUrl
        ? `<img src="${esc(r.thumbnailUrl)}" alt="${esc(r.title)}" loading="lazy"
            onerror="this.parentElement.innerHTML='${noImgEsc()}'">`
        : noImgHtml();

    const area = r.areaM2
        ? `<span class="card-meta-item">${icoArea()} ${r.areaM2} m²</span>` : '';

    const addr = r.shortAddress || '—';
    const avatar = r.landlordAvatarUrl || defaultAvatar(r.landlordName);
    const llName = r.landlordName || 'Chủ trọ';

    return `
<a class="room-card" href="/rooms/${r.id}">
  <div class="card-img-wrap">
    ${img}
    <span class="card-badge badge-active">Còn trống</span>
  </div>
  <div class="card-body">
    <div class="card-type">${typeLabel(r.roomType)}</div>
    <h3 class="card-title">${esc(r.title)}</h3>
    <div class="card-price">
      ${fmtShort(r.monthlyRent)}
      <span class="card-price-unit">₫/tháng</span>
    </div>
    <div class="card-meta">${area}</div>
    <div class="card-address">${icoPin()}<span>${esc(addr)}</span></div>
  </div>
  <div class="card-footer">
    <img src="${esc(avatar)}" alt="${esc(llName)}" class="landlord-avatar"
         onerror="this.src='${defaultAvatar(llName)}'">
    <span class="landlord-name">${esc(llName)}</span>
    <span class="card-date">${timeAgo(r.publishedAt)}</span>
  </div>
</a>`;
}

/* ─── Pagination ─── */
function renderPager() {
    const total = Math.ceil(filtered.length / PAGE_SIZE);
    if (total <= 1) { pager.innerHTML = ''; return; }

    const range = getRange(currentPage, total);
    let html = `<button class="page-btn" onclick="goPage(${currentPage - 1})"
    ${currentPage === 1 ? 'disabled' : ''}>${icoChevL()}</button>`;

    range.forEach(p => {
        html += p === '…'
            ? `<span class="page-dots">…</span>`
            : `<button class="page-btn ${p === currentPage ? 'active' : ''}"
           onclick="goPage(${p})">${p}</button>`;
    });

    html += `<button class="page-btn" onclick="goPage(${currentPage + 1})"
    ${currentPage === total ? 'disabled' : ''}>${icoChevR()}</button>`;

    pager.innerHTML = html;
}

function getRange(cur, total) {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    if (cur <= 4)         return [1, 2, 3, 4, 5, '…', total];
    if (cur >= total - 3) return [1, '…', total-4, total-3, total-2, total-1, total];
    return [1, '…', cur - 1, cur, cur + 1, '…', total];
}

window.goPage = p => {
    const total = Math.ceil(filtered.length / PAGE_SIZE);
    if (p < 1 || p > total) return;
    currentPage = p;
    renderPage();
    renderPager();
    document.querySelector('.results-header').scrollIntoView({ behavior: 'smooth', block: 'start' });
};

/* ─── Counter ─── */
function renderCounter() {
    if (filtered.length === 0) { counter.textContent = 'Không tìm thấy phòng nào'; return; }
    const s = (currentPage - 1) * PAGE_SIZE + 1;
    const e = Math.min(currentPage * PAGE_SIZE, filtered.length);
    counter.innerHTML = `Hiển thị <strong>${s}–${e}</strong> / <strong>${filtered.length}</strong> phòng`;
}

/* ─── Active tags ─── */
function renderTags() {
    const parts = [];
    if (searchTerm)
        parts.push(`<span class="tag">🔍 &ldquo;${esc(searchTerm)}&rdquo;
      <button class="tag-remove" onclick="clearSearch()">✕</button></span>`);
    if (filters.type)
        parts.push(`<span class="tag">Loại: ${typeLabel(filters.type)}
      <button class="tag-remove" onclick="removeFilter('type')">✕</button></span>`);
    if (filters.price) {
        const [a, b] = filters.price.split('-').map(Number);
        const lbl = b > 90_000_000 ? `Trên ${fmtShort(a)}` : `${fmtShort(a)} – ${fmtShort(b)}`;
        parts.push(`<span class="tag">Giá: ${lbl}
      <button class="tag-remove" onclick="removeFilter('price')">✕</button></span>`);
    }
    tagsEl.innerHTML = parts.join('');
}

/* ═══════════════════════════════════════════
   STATES
   ═══════════════════════════════════════════ */
function showSkeletons() {
    grid.className = 'room-grid';
    grid.innerHTML = Array.from({ length: PAGE_SIZE }, () => `
    <div class="skeleton-card">
      <div class="sk-img"></div>
      <div class="sk-body">
        <div class="sk-line w50"></div>
        <div class="sk-line h20"></div>
        <div class="sk-line w70"></div>
        <div class="sk-line w100"></div>
        <div class="sk-line w50"></div>
      </div>
    </div>`).join('');
    pager.innerHTML   = '';
    counter.innerHTML = 'Đang tải…';
}

function showEmpty() {
    grid.innerHTML = `<div class="state-wrap">
    <div class="state-icon">${icoSearch(48)}</div>
    <p class="state-title">Không tìm thấy phòng nào</p>
    <p class="state-sub">Thử thay đổi bộ lọc hoặc từ khoá khác nhé.</p>
    <button class="btn-ghost" onclick="resetAll()" style="margin-top:12px">Xoá tất cả bộ lọc</button>
  </div>`;
    pager.innerHTML = '';
}

function showError(msg) {
    grid.innerHTML = `<div class="state-wrap">
    <div class="state-icon" style="color:#e53e3e;opacity:1">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="12" y1="8" x2="12" y2="12"/>
        <line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
    </div>
    <p class="state-title">Không thể tải dữ liệu</p>
    <p class="state-sub">Kiểm tra lại Spring Boot đang chạy.</p>
    <code style="font-size:12px;color:var(--ink-3);margin-top:4px">${esc(msg)}</code>
    <button class="btn-primary" onclick="loadRooms()" style="margin-top:16px">Thử lại</button>
  </div>`;
    pager.innerHTML   = '';
    counter.textContent = 'Lỗi tải dữ liệu';
}

/* ═══════════════════════════════════════════
   ACTIONS & LISTENERS
   ═══════════════════════════════════════════ */
window.removeFilter = key => {
    filters[key] = '';
    ({ type: $('filterType'), price: $('filterPrice') })[key].value = '';
    applyFilters();
};
window.clearSearch = () => { searchTerm = ''; $('searchInput').value = ''; applyFilters(); };
window.resetAll = () => {
    searchTerm = '';
    $('searchInput').value = '';
    filters = { type: '', price: '', sort: 'newest' };
    $('filterType').value  = '';
    $('filterPrice').value = '';
    $('filterSort').value  = 'newest';
    applyFilters();
};

const debouncedSearch = debounce(v => { searchTerm = v; applyFilters(); }, 280);
$('searchInput').addEventListener('input',  e => debouncedSearch(e.target.value));
$('searchBtn').addEventListener('click', () => { searchTerm = $('searchInput').value; applyFilters(); });
$('searchInput').addEventListener('keydown', e => { if (e.key === 'Enter') { searchTerm = e.target.value; applyFilters(); } });
$('filterType').addEventListener('change',  e => { filters.type  = e.target.value; applyFilters(); });
$('filterPrice').addEventListener('change', e => { filters.price = e.target.value; applyFilters(); });
$('filterSort').addEventListener('change',  e => { filters.sort  = e.target.value; applyFilters(); });
$('btnReset').addEventListener('click', resetAll);
$('btnGrid').addEventListener('click', () => {
    isListView = false;
    $('btnGrid').classList.add('active'); $('btnList').classList.remove('active'); renderPage();
});
$('btnList').addEventListener('click', () => {
    isListView = true;
    $('btnList').classList.add('active'); $('btnGrid').classList.remove('active'); renderPage();
});
$('hamburger').addEventListener('click', () => {
    $('navInner').classList.toggle('open');
});

/* ═══════════════════════════════════════════
   SVG ICONS
   ═══════════════════════════════════════════ */
const icoPin    = () => `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;margin-top:1px"><path d="M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1 1 16 0z"/><circle cx="12" cy="10" r="3"/></svg>`;
const icoArea   = () => `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>`;
const icoChevL  = () => `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>`;
const icoChevR  = () => `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>`;
const icoSearch = (s = 40) => `<svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>`;

const noImgHtml = () => `<div class="card-no-img" style="display:flex; flex-direction:column; align-items:center; justify-content:center; height:100%; color:var(--ink-4); background:var(--tag-bg);">
  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
  <span style="font-size:12px; margin-top:8px;">Chưa có ảnh</span></div>`;

const noImgEsc = () => noImgHtml()
    .replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n\s*/g, '');

/* ═══════════════════════════════════════════
   INIT
   ═══════════════════════════════════════════ */
loadRooms();