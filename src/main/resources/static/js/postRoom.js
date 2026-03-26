/* =============================================
   SCRIPT.JS — Post Room Form
   Mirrors CreateRoomPostRequest + RoomPostController (with MultipartFile)
   ============================================= */

const API_ENDPOINT = '/api/post-room';

// ─── State ────────────────────────────────────
let currentStep = 1;
const TOTAL_STEPS = 5;

// Biến lưu trữ đối tượng File ảnh thật
let mainImageFileObj = null;
let galleryFilesArray = [];

// ─── Entry ────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    bindCharCounters();
    bindPriceListeners();
    bindAddressPreview();
    initDragAndDrop(); // Khởi tạo tính năng kéo/thả ảnh
    updateStepUI();
});

/* =============================================
   NAVIGATION
   ============================================= */
function nextStep(from) {
    if (!validateStep(from)) return;
    if (from < TOTAL_STEPS) {
        currentStep = from + 1;
        if (currentStep === TOTAL_STEPS) buildReview();
        updateStepUI();
        scrollToTop();
    }
}

function prevStep(from) {
    if (from > 1) {
        currentStep = from - 1;
        updateStepUI();
        scrollToTop();
    }
}

function goToStep(n) {
    // Only allow jumping to completed steps
    if (n < currentStep) {
        currentStep = n;
        updateStepUI();
        scrollToTop();
    }
}

function updateStepUI() {
    // Panels
    document.querySelectorAll('.step-panel').forEach(p => p.classList.remove('active'));
    const active = document.getElementById(`step-${currentStep}`);
    if (active) active.classList.add('active');

    // Sidebar nav
    document.querySelectorAll('.step-item').forEach((item, idx) => {
        const step = idx + 1;
        item.classList.remove('active', 'completed');
        if (step === currentStep) item.classList.add('active');
        else if (step < currentStep) item.classList.add('completed');
    });

    // Connectors
    document.querySelectorAll('.step-connector').forEach((c, idx) => {
        c.classList.toggle('filled', idx + 1 < currentStep);
    });
    const submitText = document.getElementById('submit-text');
    if (submitText) submitText.textContent = '🚀 Đăng bài ngay';
}

function scrollToTop() {
    document.querySelector('.main-content')?.scrollTo({ top: 0, behavior: 'smooth' });
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Allow clicking completed sidebar steps
document.querySelectorAll('.step-item').forEach(item => {
    item.addEventListener('click', () => {
        const n = parseInt(item.dataset.step);
        goToStep(n);
    });
});

/* =============================================
   VALIDATION
   ============================================= */
function validateStep(step) {
    clearErrors();
    let ok = true;

    if (step === 1) {
        const title = val('title');
        if (!title) { showError('title-error', 'Tiêu đề không được để trống'); ok = false; }
        else if (title.length < 10) { showError('title-error', 'Tiêu đề phải từ 10 ký tự trở lên'); ok = false; }
        mark('title', !title || title.length < 10);
    }

    if (step === 2) {
        const address = val('address');
        const city    = val('city');
        if (!address) { showError('address-error', 'Địa chỉ không được để trống'); ok = false; mark('address', true); }
        if (!city)    { showError('city-error', 'Vui lòng chọn tỉnh / thành phố'); ok = false; mark('city', true); }
    }

    if (step === 3) {
        const rent = parseFloat(val('monthlyRent'));
        if (!val('monthlyRent') || isNaN(rent) || rent <= 0) {
            showError('rent-error', 'Giá thuê phải lớn hơn 0'); ok = false; mark('monthlyRent', true);
        }
    }

    if (step === 4) {
        if (!mainImageFileObj && galleryFilesArray.length === 0) {
            showToast('⚠️ Bạn chưa thêm ảnh nào. Ảnh giúp tăng lượt xem đáng kể!', 'warning');
        }
    }

    return ok;
}

function val(id) { return (document.getElementById(id)?.value || '').trim(); }
function showError(id, msg) { const el = document.getElementById(id); if (el) el.textContent = msg; }
function clearErrors() { document.querySelectorAll('.field-error').forEach(e => e.textContent = ''); }
function mark(id, isError) {
    const el = document.getElementById(id);
    if (el) el.classList.toggle('error', isError);
}

/* =============================================
   CHAR COUNTERS & NUMBER STEPPERS
   ============================================= */
function bindCharCounters() {
    const titleEl = document.getElementById('title');
    const descEl  = document.getElementById('description');

    titleEl?.addEventListener('input', () => {
        document.getElementById('title-count').textContent = `${titleEl.value.length}/200`;
        mark('title', false);
        clearErrors();
    });

    descEl?.addEventListener('input', () => {
        document.getElementById('desc-count').textContent = `${descEl.value.length}/5000`;
    });
}

document.querySelectorAll('.stepper-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const input = document.getElementById(btn.dataset.target);
        if (!input) return;
        let v = parseInt(input.value) || 1;
        const min = parseInt(input.min) || 1;
        const max = parseInt(input.max) || 99;
        if (btn.dataset.op === 'plus')  v = Math.min(v + 1, max);
        if (btn.dataset.op === 'minus') v = Math.max(v - 1, min);
        input.value = v;
    });
});

/* =============================================
   ADDRESS PREVIEW
   ============================================= */
function bindAddressPreview() {
    ['address','ward','district','city'].forEach(id => {
        document.getElementById(id)?.addEventListener('input', updateAddressPreview);
        document.getElementById(id)?.addEventListener('change', updateAddressPreview);
    });
}

function updateAddressPreview() {
    const parts = [val('address'), val('ward'), val('district'), val('city')].filter(Boolean);
    const preview = document.getElementById('preview-text');
    if (preview) {
        preview.textContent = parts.length
            ? parts.join(', ')
            : 'Địa chỉ sẽ hiển thị ở đây khi bạn điền thông tin...';
    }
}

/* =============================================
   PRICE SUMMARY
   ============================================= */
function bindPriceListeners() {
    ['monthlyRent','internetFee','parkingFee','cleaningFee'].forEach(id => {
        document.getElementById(id)?.addEventListener('input', updateCostSummary);
    });
    updateCostSummary();
}

function updateCostSummary() {
    const rent     = parseFloat(val('monthlyRent'))     || 0;
    const internet = parseFloat(val('internetFee'))     || 0;
    const parking  = parseFloat(val('parkingFee'))      || 0;
    const cleaning = parseFloat(val('cleaningFee'))     || 0;

    const items = [
        { label: 'Tiền thuê phòng', value: rent },
        { label: 'Phí Internet',    value: internet },
        { label: 'Phí giữ xe',      value: parking },
        { label: 'Phí vệ sinh',     value: cleaning },
    ].filter(i => i.value > 0);

    const total = items.reduce((s, i) => s + i.value, 0);

    const rows = document.getElementById('summary-rows');
    const totalEl = document.getElementById('total-estimate');
    if (!rows || !totalEl) return;

    rows.innerHTML = items.map(i => `
    <div class="summary-row">
      <span class="label">${i.label}</span>
      <span class="value">${fmt(i.value)} ₫</span>
    </div>`).join('');

    totalEl.textContent = total > 0 ? `${fmt(total)} ₫/tháng` : '—';
}

function fmt(n) {
    return n.toLocaleString('vi-VN');
}

/* =============================================
   DRAG & DROP IMAGE HANDLING (MAIN & GALLERY)
   ============================================= */
function initDragAndDrop() {
    // 1. Ảnh Bìa (Main Image)
    const mainDropzone = document.getElementById('main-dropzone');
    const mainInput = document.getElementById('mainImageFile');
    if(mainDropzone && mainInput) {
        mainInput.addEventListener('change', (e) => handleMainFile(e.target.files[0]));
        mainDropzone.addEventListener('dragover', (e) => { e.preventDefault(); mainDropzone.classList.add('dragover'); });
        mainDropzone.addEventListener('dragleave', () => mainDropzone.classList.remove('dragover'));
        mainDropzone.addEventListener('drop', (e) => {
            e.preventDefault(); mainDropzone.classList.remove('dragover');
            if (e.dataTransfer.files.length > 0) handleMainFile(e.dataTransfer.files[0]);
        });
    }

    // 2. Gallery Ảnh
    const galleryDropzone = document.getElementById('gallery-dropzone');
    const galleryInput = document.getElementById('galleryFiles');
    if(galleryDropzone && galleryInput) {
        galleryInput.addEventListener('change', (e) => handleGalleryFiles(e.target.files));
        galleryDropzone.addEventListener('dragover', (e) => { e.preventDefault(); galleryDropzone.classList.add('dragover'); });
        galleryDropzone.addEventListener('dragleave', () => galleryDropzone.classList.remove('dragover'));
        galleryDropzone.addEventListener('drop', (e) => {
            e.preventDefault(); galleryDropzone.classList.remove('dragover');
            handleGalleryFiles(e.dataTransfer.files);
        });
    }
}

function handleMainFile(file) {
    if (!file || !file.type.startsWith('image/')) return;
    mainImageFileObj = file;
    document.getElementById('main-prompt').style.display = 'none';
    const wrap = document.getElementById('main-preview-wrap');
    document.getElementById('main-preview-img').src = URL.createObjectURL(file);
    wrap.style.display = 'block';
}

function removeMainImage(e) {
    e.stopPropagation();
    mainImageFileObj = null;
    document.getElementById('mainImageFile').value = '';
    document.getElementById('main-preview-wrap').style.display = 'none';
    document.getElementById('main-prompt').style.display = 'block';
}

function handleGalleryFiles(files) {
    for (let file of files) {
        if (!file.type.startsWith('image/')) continue;
        if (galleryFilesArray.length >= 10) { showToast('Tối đa 10 ảnh!', 'warning'); break; }
        galleryFilesArray.push(file);
    }
    renderGalleryFiles();
}

function removeGalleryFile(index) {
    galleryFilesArray.splice(index, 1);
    renderGalleryFiles();
}

function renderGalleryFiles() {
    const grid = document.getElementById('gallery-grid');
    if (galleryFilesArray.length === 0) {
        grid.innerHTML = `<div class="gallery-empty"><span>📷</span><p>Chưa có ảnh gallery</p></div>`;
        return;
    }
    grid.innerHTML = galleryFilesArray.map((file, i) => `
      <div class="gallery-item">
          <img src="${URL.createObjectURL(file)}" alt="Gallery">
          <span class="gallery-item-num">${i + 1}</span>
          <button type="button" class="gallery-item-remove" onclick="removeGalleryFile(${i})">✕</button>
      </div>
  `).join('');
}


/* =============================================
   REVIEW PANEL — BUILD
   ============================================= */
function buildReview() {
    const container = document.getElementById('review-content');
    if (!container) return;

    const address = [val('address'), val('ward'), val('district'), val('city')].filter(Boolean).join(', ');
    const rent    = parseFloat(val('monthlyRent')) || 0;
    const deposit = parseFloat(val('depositAmount')) || 0;
    const area    = val('areaM2');
    const occ     = val('maxOccupants');
    const rtype   = document.getElementById('roomType')?.selectedOptions[0]?.text || '—';

    // Dùng URL.createObjectURL để render ảnh từ File đối tượng
    const thumbHtml = mainImageFileObj
        ? `<img class="review-thumb" src="${URL.createObjectURL(mainImageFileObj)}" alt="Ảnh bìa" />`
        : `<div class="review-thumb-placeholder">🏠</div>`;

    container.innerHTML = `
    ${thumbHtml}
    <div class="review-body">
      <div class="review-title-row">
        <p class="review-title">${val('title') || '(Chưa có tiêu đề)'}</p>
        <p class="review-price">${fmt(rent)} ₫<small>/tháng</small></p>
      </div>
      <p class="review-address">📍 ${address || '(Chưa có địa chỉ)'}</p>
      <div class="review-meta-grid">
        <div class="review-meta-item">
          <p class="review-meta-label">Loại phòng</p>
          <p class="review-meta-value">${rtype}</p>
        </div>
        <div class="review-meta-item">
          <p class="review-meta-label">Diện tích</p>
          <p class="review-meta-value">${area ? area + ' m²' : '—'}</p>
        </div>
        <div class="review-meta-item">
          <p class="review-meta-label">Số người tối đa</p>
          <p class="review-meta-value">${occ || '—'} người</p>
        </div>
        <div class="review-meta-item">
          <p class="review-meta-label">Tiền cọc</p>
          <p class="review-meta-value">${deposit > 0 ? fmt(deposit) + ' ₫' : '—'}</p>
        </div>
        <div class="review-meta-item">
          <p class="review-meta-label">Số ảnh gallery</p>
          <p class="review-meta-value">${galleryFilesArray.length} ảnh</p>
        </div>
        <div class="review-meta-item">
          <p class="review-meta-label">Giá điện</p>
          <p class="review-meta-value">${fmt(parseFloat(val('electricityPricePerKwh')) || 3500)} ₫/kWh</p>
        </div>
      </div>
    </div>`;

}

/* =============================================
   BUILD REQUEST BODY — mirrors CreateRoomPostRequest
   ============================================= */
function buildRequestBody() {
    return {
        // Thông tin cơ bản
        title:            val('title'),
        description:      val('description') || null,
        monthlyRent:      parseFloat(val('monthlyRent'))     || null,
        depositAmount:    parseFloat(val('depositAmount'))   || null,
        areaM2:           parseFloat(val('areaM2'))          || null,
        maxOccupants:     parseInt(val('maxOccupants'))      || 1,
        roomType:         val('roomType')                    || null,

        // Địa chỉ
        address:          val('address'),
        ward:             val('ward')     || null,
        district:         val('district') || null,
        city:             val('city'),

        // Giá dịch vụ
        electricityPricePerKwh: parseFloat(val('electricityPricePerKwh')) || null,
        waterPricePerM3:        parseFloat(val('waterPricePerM3'))        || null,
        internetFee:            parseFloat(val('internetFee'))            || 0,
        parkingFee:             parseFloat(val('parkingFee'))             || 0,
        cleaningFee:            parseFloat(val('cleaningFee'))            || 0,

        };
}

/* =============================================
   SUBMIT
   ============================================= */
async function submitForm() {
    const btn = document.getElementById('submit-btn');
    if (!btn) return;

    btn.disabled = true;
    btn.innerHTML = '<span>⏳ Đang xử lý...</span>';

    // 1. Lấy dữ liệu Text (Không còn chứa link ảnh cũ nữa)
    const bodyData = buildRequestBody();

    // 2. Khởi tạo FormData để gửi cả Text và File
    const formData = new FormData();
    formData.append("request", new Blob([JSON.stringify(bodyData)], { type: "application/json" }));

    if (mainImageFileObj) formData.append("mainImage", mainImageFileObj);
    galleryFilesArray.forEach(file => formData.append("gallery", file));

    try {
        const res = await fetch(API_ENDPOINT, {
            method: 'POST',
            body: formData, // Trình duyệt tự động set Header multipart/form-data
        });

        if (res.ok) {
            showToast('🎉 Đăng bài thành công!', 'success');
            setTimeout(() => {
                return window.location.href = '/';
            }, 1500);
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(`❌ Lỗi: ${err.message || 'Có lỗi xảy ra'}`, 'error');
            btn.disabled = false;
            btn.innerHTML = '<span id="submit-text">🚀 Đăng bài ngay</span>';
        }
    } catch (e) {
        showToast('❌ Không thể kết nối đến server!', 'error');
        btn.disabled = false;
        btn.innerHTML = '<span id="submit-text">🚀 Đăng bài ngay</span>';
    }
}

/* =============================================
   TOAST
   ============================================= */
let toastTimer = null;

function showToast(msg, type = '') {
    const el = document.getElementById('toast');
    if (!el) return;
    clearTimeout(toastTimer);
    el.textContent = msg;
    el.className = `toast ${type}`;
    requestAnimationFrame(() => el.classList.add('show'));
    toastTimer = setTimeout(() => el.classList.remove('show'), 3200);
}