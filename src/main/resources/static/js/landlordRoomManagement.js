    /* ROOM MANAGEMENT - uses /api/room-management/room-summary */
    let cachedRooms = [];

    const roomFilterEls = {
        search: document.getElementById('houseSearchInput'),
        status: document.getElementById('houseStatusFilter'),
        sort:   document.getElementById('houseSortSelect'),
        count:  document.getElementById('houseCountLabel'),
        tbody:  document.getElementById('house-management-body')
    };

    // Map status values from real API (RENTED, AVAILABLE, etc.)
    function getStatusBadge(status) {
        const s = String(status || '').toUpperCase();
        const map = {
            AVAILABLE: { cls: 'badge-active',   label: 'Còn trống' },
            RENTED:    { cls: 'badge-rented',   label: 'Đã cho thuê' },
            OCCUPIED:  { cls: 'badge-rented',   label: 'Đã cho thuê' },
            ACTIVE:    { cls: 'badge-active',   label: 'Đang hoạt động' },
            DRAFT:     { cls: 'badge-draft',    label: 'Nháp' },
            INACTIVE:  { cls: 'badge-inactive', label: 'Tạm ẩn' },
        };
        return map[s] || { cls: 'badge-unknown', label: s || 'Không rõ' };
    }

    function fmtMoney(v) {
        if (v == null) return '-';
        return Number(v).toLocaleString('vi-VN') + ' VNĐ';
    }

    function fmtArea(v) { return v ? `${v} m²` : '-'; }

    function esc(str) {
        return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }

    // Helper to get a room's display name from real API fields
    function getRoomDisplayName(r) {
        return r.roomNumber || r.roomName || r.title || r.name || 'Phòng chưa đặt tên';
    }

    async function loadRoomsSummary() {
        const token = localStorage.getItem('smartstay_token');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        try {
            const res = await fetch('/api/room-management/room-summary', { headers, credentials: 'include' });
            if (!res.ok) throw new Error('API error');
            const data = await res.json();
            cachedRooms = Array.isArray(data) ? data : (data.rooms || data.data || []);

            // Update hero stats from this API
            const total    = cachedRooms.length;
            const rented   = cachedRooms.filter(r => String(r.status||'').toUpperCase() === 'RENTED' || String(r.status||'').toUpperCase() === 'OCCUPIED').length;
            const available = cachedRooms.filter(r => String(r.status||'').toUpperCase() === 'AVAILABLE').length;
            const pEl = document.getElementById('postsCount');
            const aEl = document.getElementById('activePostsCount');
            const rEl = document.getElementById('rentedPostsCount');
            if (pEl) pEl.textContent = total;
            if (aEl) aEl.textContent = available;
            if (rEl) rEl.textContent = rented;
            const cntBadge = document.getElementById('houseCountLabel');
            if (cntBadge) cntBadge.textContent = `${total} phòng`;

            applyRoomFilters();
        } catch (e) {
            console.error('loadRoomsSummary error:', e);
            if (roomFilterEls.tbody) {
                roomFilterEls.tbody.innerHTML = `<tr><td colspan="5"><div style="padding:1.5rem;color:#dc2626;font-size:0.82rem;">Không thể tải dữ liệu phòng. Vui lòng thử lại.</div></td></tr>`;
            }
        }
    }

    // Allow hero stat cards to filter table
    function filterRoomsByStatus(status) {
        if (roomFilterEls.status) roomFilterEls.status.value = status;
        applyRoomFilters();
        document.getElementById('house-management')?.scrollIntoView({ behavior: 'smooth' });
    }

    function getFilteredRooms() {
        let list = cachedRooms.slice();
        const q = (roomFilterEls.search?.value || '').toLowerCase().trim();
        if (q) {
            list = list.filter(r => {
                const name = String(r.roomNumber || r.roomName || r.title || r.name || '').toLowerCase();
                const addr = String(r.address || r.shortAddress || '').toLowerCase();
                const type = String(r.roomType || '').toLowerCase();
                return name.includes(q) || addr.includes(q) || type.includes(q);
            });
        }
        const sf = roomFilterEls.status?.value || 'all';
        if (sf !== 'all') list = list.filter(r => String(r.status || '').toUpperCase() === sf);

        const sv = roomFilterEls.sort?.value || 'newest';
        if (sv === 'priceAsc')   list.sort((a,b) => Number(a.rentPrice||a.monthlyRent||0) - Number(b.rentPrice||b.monthlyRent||0));
        else if (sv === 'priceDesc') list.sort((a,b) => Number(b.rentPrice||b.monthlyRent||0) - Number(a.rentPrice||a.monthlyRent||0));
        else list.sort((a,b) => new Date(b.createdAt||0) - new Date(a.createdAt||0));
        return list;
    }

    function renderRoomsTable(rooms) {
        if (!roomFilterEls.tbody) return;
        if (!rooms.length) {
            roomFilterEls.tbody.innerHTML = `<tr><td colspan="5"><div class="ss-empty"><svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg><p>Không tìm thấy phòng nào phù hợp.</p></div></td></tr>`;
            return;
        }
        roomFilterEls.tbody.innerHTML = rooms.map(r => {
            const badge = getStatusBadge(r.status);
            const name  = esc(getRoomDisplayName(r));
            const type  = esc((r.roomType || '').replace(/_/g,' '));
            const price = fmtMoney(r.rentPrice ?? r.monthlyRent);
            const area  = fmtArea(r.areaM2 || r.area);
            const rid   = r.id || r.roomId || '';
            const postId = r.postId || r.id || '';

            // Show tenant name if rented
            const tenantInfo = r.userName
                ? `<div style="font-size:0.7rem;color:#2563eb;margin-top:2px;">Khách: ${esc(r.userName)}</div>`
                : '';

            return `<tr style="cursor:pointer;" onclick="openRoomDetail(${rid})" title="Xem chi tiết phòng">
          <td>
            <div class="room-room-name">${name}</div>
            ${type ? `<div class="room-room-sub">${type}</div>` : ''}
            ${tenantInfo}
          </td>
          <td class="ss-table-price">${price}</td>
          <td style="color:#64748b;font-weight:600;">${area}</td>
          <td>
            <span class="badge ${badge.cls}">
              <span class="badge-dot"></span>${badge.label}
            </span>
          </td>
          <td onclick="event.stopPropagation()">
            <div style="display:flex;gap:0.4rem;">
              <button class="tbl-btn-copy" onclick="openRoomDetail(${rid})">Chi tiết</button>
              ${postId ? `<a href="/room-detail-management/${postId}" class="tbl-btn-view">Xem</a>` : ''}
            </div>
          </td>
        </tr>`;
        }).join('');
    }

    function applyRoomFilters() {
        const filtered = getFilteredRooms();
        renderRoomsTable(filtered);
        if (roomFilterEls.count) roomFilterEls.count.textContent = `${filtered.length} phòng`;
    }

    /* Room Detail Modal - GET /api/room-management/room-detail-management (body: roomId) */
    async function openRoomDetail(roomId) {
        if (!roomId) return;
        const modal = document.getElementById('room-detail-modal');
        const body  = document.getElementById('modal-room-body');
        const title = document.getElementById('modal-room-title');
        modal.classList.add('open');
        body.innerHTML = `<div class="ss-loading"><div class="ss-spinner"></div> Đang tải chi tiết phòng...</div>`;

        // First try from cache for instant display
        const cached = cachedRooms.find(r => (r.id || r.roomId) == roomId);
        if (cached) title.textContent = `Phòng ${getRoomDisplayName(cached)}`;

        const token = localStorage.getItem('smartstay_token');
        const headers = {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
        try {
            const res = await fetch('/api/room-management/room-detail-management', {
                method: 'GET',
                headers,
                credentials: 'include',
                // body: JSON.stringify(roomId)
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const raw = await res.json();
            const room = raw.data || raw.body || raw;

            title.textContent = `Phòng ${esc(room.roomNumber || room.roomName || room.title || '')}`;

            // Build detail rows
            const statusBadge = getStatusBadge(room.status);
            const badgeHtml = `<span class="badge ${statusBadge.cls}" style="font-size:0.75rem;"><span class="badge-dot"></span>${statusBadge.label}</span>`;

            body.innerHTML = `
          <div style="display:grid;gap:0.2rem;">
            ${modalRow('Số phòng', esc(room.roomNumber || '-'))}
            ${modalRow('Loại phòng', esc((room.roomType||'-').replace(/_/g,' ')))}
            ${modalRow('Giá thuê/tháng', fmtMoney(room.rentPrice ?? room.monthlyRent))}
            ${modalRow('Diện tích', fmtArea(room.areaM2 || room.area))}
            ${modalRowHtml('Trạng thái', badgeHtml)}
            ${room.userName ? modalRow('Khách đang thuê', esc(room.userName)) : ''}
            ${room.userEmail ? modalRow('Email khách thuê', esc(room.userEmail)) : ''}
            ${room.address ? modalRow('Địa chỉ', esc(room.address)) : ''}
            ${room.description ? modalRow('Mô tả', esc(room.description)) : ''}
            ${room.createdAt ? modalRow('Ngày tạo', new Date(room.createdAt).toLocaleDateString('vi-VN')) : ''}
          </div>
          ${!room.userName ? `
          <div style="margin-top:1.1rem;padding:0.85rem 1rem;background:#f0fdf4;border:1.5px solid #bbf7d0;border-radius:0.75rem;font-size:0.8rem;color:#16a34a;font-weight:700;display:flex;align-items:center;gap:0.5rem;">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            Phòng hiện đang trống - sẵn sàng cho thuê</div>` : ''}`;
        } catch (e) {
            // Fallback: hiển thị dữ liệu từ cache nếu API lỗi
            if (cached) {
                const statusBadge = getStatusBadge(cached.status);
                body.innerHTML = `
            <div style="margin-bottom:0.75rem;padding:0.5rem 0.75rem;background:#fef3c7;border-radius:0.5rem;font-size:0.75rem;color:#92400e;">Hiển thị dữ liệu tóm tắt do không tải được chi tiết đầy đủ.</div>
            <div style="display:grid;gap:0.2rem;">
              ${modalRow('Số phòng', esc(cached.roomNumber || '-'))}
              ${modalRow('Loại phòng', esc((cached.roomType||'-').replace(/_/g,' ')))}
              ${modalRow('Giá thuê/tháng', fmtMoney(cached.rentPrice ?? cached.monthlyRent))}
              ${modalRowHtml('Trạng thái', `<span class="badge ${statusBadge.cls}"><span class="badge-dot"></span>${statusBadge.label}</span>`)}
              ${cached.userName ? modalRow('Khách đang thuê', esc(cached.userName)) : ''}
              ${cached.userEmail ? modalRow('Email', esc(cached.userEmail)) : ''}
            </div>`;
            } else {
                body.innerHTML = `<div style="color:#dc2626;font-size:0.82rem;padding:1rem;">Không thể tải chi tiết phòng.</div>`;
            }
            console.error('openRoomDetail error:', e);
        }
    }

    function modalRow(key, val) {
        return `<div class="modal-row"><span class="modal-key">${key}</span><span class="modal-val">${val}</span></div>`;
    }
    function modalRowHtml(key, html) {
        return `<div class="modal-row"><span class="modal-key">${key}</span><div>${html}</div></div>`;
    }

    function closeRoomModal() {
        document.getElementById('room-detail-modal').classList.remove('open');
    }
    document.getElementById('room-detail-modal').addEventListener('click', function(e) {
        if (e.target === this) closeRoomModal();
    });

    /* Filter events */
    ['houseSearchInput','houseStatusFilter','houseSortSelect'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener(el.tagName === 'INPUT' ? 'input' : 'change', applyRoomFilters);
    });

    /* Init room management */
    document.addEventListener('DOMContentLoaded', loadRoomsSummary);
