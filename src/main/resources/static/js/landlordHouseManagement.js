document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/landlord/houses';

    const grid = document.getElementById('houseCardGrid');
    const countLabel = document.getElementById('houseCountLabel');
    const searchInput = document.getElementById('houseSearchInput');
    const statusSelect = document.getElementById('houseStatusFilter');
    const sortSelect = document.getElementById('houseSortSelect');

    const detailModal = document.getElementById('houseDetailModal');
    const closeDetailButton = document.getElementById('closeHouseDetail');

    if (!grid || !detailModal) {
        return;
    }

    let cachedHouses = [];

    const STATUS_LABELS = {
        AVAILABLE: 'Đang trống',
        RENTED: 'Đã cho thuê',
        MAINTENANCE: 'Bảo trì'
    };

    const STATUS_STYLES = {
        AVAILABLE: {
            badge: 'text-red-100/80',
            gradient: 'linear-gradient(135deg, #0f172a 0%, #1d4ed8 55%, #38bdf8 100%)',
            priceClass: 'bg-emerald-50 text-emerald-700'
        },
        RENTED: {
            badge: 'text-violet-100/80',
            gradient: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 55%, #c084fc 100%)',
            priceClass: 'bg-amber-50 text-amber-700'
        },
        MAINTENANCE: {
            badge: 'text-slate-100/80',
            gradient: 'linear-gradient(135deg, #0f766e 0%, #14b8a6 55%, #99f6e4 100%)',
            priceClass: 'bg-slate-100 text-slate-700'
        }
    };

    const getToken = () => localStorage.getItem('smartstay_token');

    const escapeHtml = (value) => {
        if (!value) return '';
        return String(value)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;');
    };

    const formatMoney = (value) => {
        if (value == null) return 'Chưa cập nhật';
        const num = Number(value);
        if (!Number.isFinite(num)) return String(value);
        return `${num.toLocaleString('vi-VN')} VNĐ`;
    };

    const formatMonthlyRent = (value) => {
        const base = formatMoney(value);
        return base === 'Chưa cập nhật' ? base : `${base} / tháng`;
    };

    const formatArea = (value) => (value ? `${value} m²` : 'Chưa cập nhật');

    const normalizeText = (value) => String(value || '').toLowerCase().trim();

    const toNumber = (value) => {
        const num = Number(value);
        return Number.isFinite(num) ? num : 0;
    };

    const getTimestamp = (value) => {
        const date = new Date(value || 0);
        return Number.isNaN(date.getTime()) ? 0 : date.getTime();
    };

    const getStatusMeta = (status) => {
        const key = String(status || 'AVAILABLE').toUpperCase();
        const label = STATUS_LABELS[key] || key;
        const style = STATUS_STYLES[key] || STATUS_STYLES.AVAILABLE;
        return {
            code: key,
            label,
            badgeClass: style.badge,
            gradient: style.gradient,
            priceClass: style.priceClass
        };
    };

    const getAddress = (house) => house.fullAddress || house.address || 'Chưa có địa chỉ';

    const formatDate = (value) => {
        if (!value) return 'Chưa cập nhật';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return 'Chưa cập nhật';
        return date.toLocaleDateString('vi-VN');
    };

    const CONTRACT_STATUS_LABELS = {
        ACTIVE: 'Đang hiệu lực',
        PENDING: 'Chờ kích hoạt',
        EXPIRED: 'Hết hạn',
        TERMINATED: 'Đã chấm dứt',
        CANCELLED: 'Đã hủy'
    };

    const formatContractStatus = (value) => {
        if (!value) return 'Chưa có hợp đồng';
        const key = String(value).toUpperCase();
        return CONTRACT_STATUS_LABELS[key] || String(value);
    };

    const setCount = (value) => {
        if (countLabel) {
            countLabel.textContent = `${value} nhà`;
        }
    };

    const openModal = () => {
        detailModal.classList.remove('hidden');
        detailModal.classList.add('flex');
    };

    const closeModal = () => {
        detailModal.classList.add('hidden');
        detailModal.classList.remove('flex');
    };

    const fillModalFromCard = (card) => {
        const statusMeta = getStatusMeta(card.dataset.houseStatus);
        document.getElementById('detailHouseName').textContent = card.dataset.houseName || '-';
        document.getElementById('detailHouseAddress').textContent = card.dataset.houseAddress || '-';
        document.getElementById('detailHouseStatus').textContent = statusMeta.label;
        document.getElementById('detailHousePrice').textContent = card.dataset.housePrice || '-';
        document.getElementById('detailHouseArea').textContent = card.dataset.houseArea || '-';
        document.getElementById('detailHouseTenant').textContent = card.dataset.houseTenant || 'Chưa có';
        document.getElementById('detailTenantPhone').textContent = card.dataset.houseTenantPhone || 'Chưa có';
        document.getElementById('detailTenantEmail').textContent = card.dataset.houseTenantEmail || 'Chưa có';
        document.getElementById('detailHouseDescription').textContent = card.dataset.houseDescription || 'Chưa có mô tả';
        document.getElementById('detailContractCode').textContent = card.dataset.contractCode || 'Chưa có';
        document.getElementById('detailContractStatus').textContent = formatContractStatus(card.dataset.contractStatus);
        document.getElementById('detailContractStart').textContent = formatDate(card.dataset.contractStart);
        document.getElementById('detailContractEnd').textContent = formatDate(card.dataset.contractEnd);
        document.getElementById('detailContractDeposit').textContent = card.dataset.contractDeposit || 'Chưa cập nhật';
        document.getElementById('detailContractBilling').textContent = card.dataset.contractBilling
            ? `Ngày ${card.dataset.contractBilling} hàng tháng`
            : 'Chưa cập nhật';
    };

    const openHouseDetail = (card) => {
        fillModalFromCard(card);
        openModal();
    };

    const renderHouseCards = (houses) => {
        if (!Array.isArray(houses) || houses.length === 0) {
            grid.innerHTML = `
                <div class="col-span-full rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center">
                    <p class="text-base font-semibold text-gray-800 m-0">Chưa có nhà nào</p>
                    <p class="mt-2 text-sm text-gray-500">Tạo nhà mới để bắt đầu quản lý.</p>
                    <div class="mt-4">
                        <a href="/postRooms" class="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white no-underline hover:bg-slate-800">Đăng nhà mới</a>
                    </div>
                </div>`;
            return;
        }

        grid.innerHTML = '';
        houses.forEach((house) => {
            const statusMeta = getStatusMeta(house.status);
            const houseTitle = house.roomNumber ? `Phòng ${house.roomNumber}` : (house.roomType || 'Nhà chưa đặt tên');
            const title = escapeHtml(houseTitle);
            const address = escapeHtml(getAddress(house));
            const price = formatMonthlyRent(house.rentPrice);
            const area = formatArea(house.areaM2);
            const tenantName = house.tenant && house.tenant.fullName ? house.tenant.fullName : 'Chưa có';
            const tenantPhone = house.tenant && house.tenant.phoneNumber ? house.tenant.phoneNumber : 'Chưa có';
            const tenantEmail = house.tenant && house.tenant.email ? house.tenant.email : 'Chưa có';
            const description = escapeHtml(house.description || 'Chưa có mô tả');
            const contract = house.contract || {};

            const card = document.createElement('article');
            card.className = 'house-card overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md';
            card.dataset.houseId = house.id || '';
            card.dataset.houseStatus = statusMeta.code;
            card.dataset.houseName = houseTitle;
            card.dataset.houseAddress = getAddress(house);
            card.dataset.housePrice = price;
            card.dataset.houseArea = area;
            card.dataset.houseTenant = tenantName;
            card.dataset.houseTenantPhone = tenantPhone;
            card.dataset.houseTenantEmail = tenantEmail;
            card.dataset.houseDescription = house.description || '';
            card.dataset.contractCode = contract.contractCode || '';
            card.dataset.contractStatus = contract.status || '';
            card.dataset.contractStart = contract.startDate || '';
            card.dataset.contractEnd = contract.endDate || '';
            card.dataset.contractDeposit = contract.depositAmount != null ? formatMoney(contract.depositAmount) : '';
            card.dataset.contractBilling = contract.billingDate != null ? `${contract.billingDate}` : '';

            card.innerHTML = `
                <div class="flex h-40 items-end p-4 text-white" style="background: ${statusMeta.gradient};">
                    <div>
                        <p class="m-0 text-xs font-semibold uppercase tracking-[0.2em] ${statusMeta.badgeClass}">${statusMeta.label}</p>
                        <h4 class="m-0 mt-2 text-lg font-bold">${title}</h4>
                        <p class="m-0 mt-1 text-sm text-white/90">${address}</p>
                    </div>
                </div>
                <div class="space-y-4 p-4">
                    <div class="flex items-center justify-between gap-3 text-sm">
                        <span class="rounded-full px-3 py-1 font-semibold ${statusMeta.priceClass}">${price}</span>
                        <span class="text-gray-500">${area}</span>
                    </div>
                    <p class="m-0 text-sm leading-6 text-gray-600">${description}</p>
                    <p class="m-0 text-xs font-semibold text-slate-500">Người thuê: ${escapeHtml(tenantName)}</p>
                    <div class="flex gap-2">
                        <button type="button" class="house-detail-button inline-flex flex-1 items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white">Xem chi tiết</button>
                    </div>
                </div>`;

            grid.appendChild(card);
        });
    };

    const getFilteredPosts = (houses) => {
        let result = Array.isArray(houses) ? houses.slice() : [];

        const query = normalizeText(searchInput && searchInput.value);
        if (query) {
            result = result.filter((post) => {
                const title = normalizeText(post.roomNumber || post.roomType);
                const address = normalizeText(getAddress(post));
                return title.includes(query) || address.includes(query);
            });
        }

        const statusValue = statusSelect ? statusSelect.value : 'all';
        if (statusValue && statusValue !== 'all') {
            result = result.filter((post) => String(post.status || '').toUpperCase() === statusValue);
        }

        const sortValue = sortSelect ? sortSelect.value : 'newest';
        if (sortValue === 'priceAsc') {
            result.sort((a, b) => toNumber(a.rentPrice) - toNumber(b.rentPrice));
        } else if (sortValue === 'priceDesc') {
            result.sort((a, b) => toNumber(b.rentPrice) - toNumber(a.rentPrice));
        } else {
            result.sort((a, b) => getTimestamp(b.createdAt) - getTimestamp(a.createdAt));
        }

        return result;
    };

    const applyFilters = () => {
        const filtered = getFilteredPosts(cachedHouses);
        renderHouseCards(filtered);
        setCount(filtered.length);
    };

    const bindFilters = () => {
        if (searchInput) {
            searchInput.addEventListener('input', applyFilters);
        }
        if (statusSelect) {
            statusSelect.addEventListener('change', applyFilters);
        }
        if (sortSelect) {
            sortSelect.addEventListener('change', applyFilters);
        }
    };

    const loadHouseCards = async () => {
        grid.innerHTML = `
            <div class="col-span-full rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center text-sm text-gray-500">
                Đang tải danh sách nhà...
            </div>`;

        try {
            const token = getToken();
            const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
            const res = await fetch(API_URL, { headers, credentials: 'include' });
            if (res.status === 401 || res.status === 403) {
                grid.innerHTML = `
                    <div class="col-span-full rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center text-sm text-gray-500">
                          Lỗi khi tải dữ liệu quản lý nhà.
                    </div>`;
                return;
            }
            if (!res.ok) throw new Error('Fetch error');
            const houses = await res.json();
            cachedHouses = Array.isArray(houses) ? houses : [];
            applyFilters();
        } catch (err) {
            grid.innerHTML = `
                <div class="col-span-full rounded-2xl border border-red-100 bg-red-50 px-5 py-8 text-center text-sm text-red-600">
                    Lỗi khi tải dữ liệu quản lý nhà.
                </div>`;
            console.error(err);
        }
    };

    grid.addEventListener('click', (event) => {
        const button = event.target.closest('.house-detail-button');
        if (!button) return;
        const card = button.closest('.house-card');
        if (!card) return;
        openHouseDetail(card);
    });

    grid.addEventListener('dblclick', (event) => {
        const card = event.target.closest('.house-card');
        if (!card) return;
        openHouseDetail(card);
    });

    if (closeDetailButton) {
        closeDetailButton.addEventListener('click', closeModal);
    }

    detailModal.addEventListener('click', (event) => {
        if (event.target === detailModal) {
            closeModal();
        }
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            closeModal();
        }
    });

    bindFilters();
    loadHouseCards();
});
