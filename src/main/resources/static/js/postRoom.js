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

// State lưu trữ tiện ích nội thất AI nhận diện / người dùng tùy chỉnh
let detectedInteriorsList = [];
let roomUtilitiesList = [];

// ─── Entry ────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    bindCharCounters();
    bindPriceListeners();
    bindAddressPreview();
    initDragAndDrop(); // Khởi tạo tính năng kéo/thả ảnh
    bindAIEvents();    // Khởi tạo các sự kiện AI (định giá & nội thất)
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

// Hàm gọi AI Flask
async function fetchAIAmenities() {
    const formData = new FormData();
    let hasFiles = false;

    // Lấy ảnh bìa từ biến state thực tế (đã hỗ trợ cả drag-drop)
    if (mainImageFileObj) {
        formData.append('file', mainImageFileObj);
        hasFiles = true;
    }

    // Lấy ảnh gallery từ biến state thực tế (đã hỗ trợ cả drag-drop)
    if (galleryFilesArray && galleryFilesArray.length > 0) {
        galleryFilesArray.forEach(file => {
            formData.append('file', file);
        });
        hasFiles = true;
    }

    // Nếu không có ảnh nào, hiển thị cảnh báo nhỏ
    if (!hasFiles) {
        showToast('⚠️ Vui lòng tải lên ít nhất một hình ảnh để AI nhận diện!', 'warning');
        return;
    }

    // Hiển thị UI Đang tải
    const loadingEl = document.getElementById('ai-loading');
    const resultsContainer = document.getElementById('ai-results');
    if (loadingEl) loadingEl.style.display = 'block';
    if (resultsContainer) resultsContainer.innerHTML = '';

    try {
        // GỌI SANG CỔNG 5000 CỦA FLASK (sử dụng hostname hiện tại)
        let host = window.location.hostname;
        if (host === 'localhost') {
            host = '127.0.0.1';
        }
        const response = await fetch(`http://${host}:5000/predict/image`, {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
        if (loadingEl) loadingEl.style.display = 'none';

        if (data.success) {
            detectedInteriorsList = data.detected_amenities || [];
            renderInteriorTags();
            // Tự động gọi dự đoán giá khi có dữ liệu nội thất mới từ AI
            fetchAIPricePrediction();
            showToast('🎉 Nhận diện tiện ích nội thất bằng AI thành công!', 'success');
        } else {
            if (resultsContainer) resultsContainer.innerHTML = `<span style="color: #dc3545; font-size: 13px;">Lỗi: ${data.error || 'Không nhận diện được.'}</span>`;
        }
    } catch (error) {
        if (loadingEl) loadingEl.style.display = 'none';
        if (resultsContainer) resultsContainer.innerHTML = '<span style="color: #dc3545; font-size: 13px;">Lỗi kết nối AI Server (cổng 5000).</span>';
        console.error("AI Error:", error);
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
        if (!mainImageFileObj && galleryFilesArray.length === 0) {
            showToast('⚠️ Bạn chưa thêm ảnh nào. Ảnh giúp tăng lượt xem đáng kể!', 'warning');
        }
    }

    if (step === 4) {
        const rent = parseFloat(val('monthlyRent'));
        if (!val('monthlyRent') || isNaN(rent) || rent <= 0) {
            showError('rent-error', 'Giá thuê phải lớn hơn 0'); ok = false; mark('monthlyRent', true);
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

    // Khởi chạy dự đoán giá AI cho Step 5 ngay lập tức
    fetchAIPricePrediction();
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
    const token = localStorage.getItem('smartstay_token');
    const btn = document.getElementById('submit-btn');
    if (!btn) return;

    btn.disabled = true;
    btn.innerHTML = '<span>⏳ Đang xử lý...</span>';

    // 1. Lấy dữ liệu Text cơ bản
    const bodyData = buildRequestBody();

    // ================== BẮT ĐẦU PHẦN THÊM MỚI (AI AMENITIES) ==================
    // Lấy chuỗi JSON từ input ẩn mà hàm AI đã lưu lúc nãy
    let aiAmenities = [];
    try {
        const aiInput = document.getElementById('detectedInteriors');
        if (aiInput && aiInput.value) {
            aiAmenities = JSON.parse(aiInput.value);
        }
    } catch (e) {
        console.warn("Không parse được tiện ích AI, gửi mảng rỗng.");
    }

    // Đóng gói đúng chuẩn cấu trúc InteriorRoomRequest.java bên Spring Boot
    const interiorData = {
        roomId: null, // Để null, Spring Boot (RoomPostService) sẽ tự động map ID phòng vừa tạo vào
        interiorName: aiAmenities
    };
    // ============================ KẾT THÚC THÊM MỚI ============================

    // 2. Khởi tạo FormData để gửi cả Text và File
    const formData = new FormData();

    // Gắn Part 1: request
    formData.append("request", new Blob([JSON.stringify(bodyData)], { type: "application/json" }));

    // Gắn Part 2 & 3: mainImage và gallery
    if (mainImageFileObj) formData.append("mainImage", mainImageFileObj);
    galleryFilesArray.forEach(file => formData.append("gallery", file));

    // THÊM MỚI - Gắn Part 4: interiors (Gửi cho Backend Spring Boot)
    formData.append("interiors", new Blob([JSON.stringify(interiorData)], { type: "application/json" }));

    try {
        const res = await fetch(API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData,
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

/* =============================================
   AI INTERIORS MANAGEMENT & PRICE PREDICTION
   ============================================= */

function addCustomInterior() {
    const input = document.getElementById('custom-interior-input');
    if (!input) return;
    const value = input.value.trim();
    if (!value) return;
    
    if (!detectedInteriorsList.includes(value)) {
        detectedInteriorsList.push(value);
        renderInteriorTags();
        fetchAIPricePrediction();
    }
    input.value = '';
}

function removeInteriorItem(item) {
    detectedInteriorsList = detectedInteriorsList.filter(i => i !== item);
    renderInteriorTags();
    fetchAIPricePrediction();
}

function renderInteriorTags() {
    const resultsContainer = document.getElementById('ai-results');
    const detectedInput = document.getElementById('detectedInteriors');
    if (!resultsContainer) return;
    
    if (detectedInput) {
        detectedInput.value = JSON.stringify(detectedInteriorsList);
    }
    
    if (detectedInteriorsList.length === 0) {
        resultsContainer.innerHTML = '<span style="color: #6c757d; font-style: italic; font-size: 13px;">Chưa có dữ liệu tiện ích nội thất. Hãy nhập thêm ở dưới!</span>';
        return;
    }
    
    resultsContainer.innerHTML = '';
    detectedInteriorsList.forEach(item => {
        const tag = document.createElement('span');
        tag.style.cssText = 'background: #e2f0fd; color: #007bff; padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; border: 1px solid #b8daff; display: inline-flex; align-items: center; gap: 8px;';
        
        const label = document.createElement('span');
        label.innerText = "✔️ " + item;
        
        const removeBtn = document.createElement('span');
        removeBtn.innerHTML = "✕";
        removeBtn.style.cssText = 'cursor: pointer; font-weight: bold; color: #dc3545; transition: color 0.2s;';
        removeBtn.addEventListener('mouseenter', () => removeBtn.style.color = '#bd2130');
        removeBtn.addEventListener('mouseleave', () => removeBtn.style.color = '#dc3545');
        removeBtn.onclick = () => removeInteriorItem(item);
        
        tag.appendChild(label);
        tag.appendChild(removeBtn);
        resultsContainer.appendChild(tag);
    });
}

async function fetchAIPricePrediction() {
    const acreage = parseFloat(val('areaM2'));
    const district = val('district');
    const city = val('city');
    const expectPrice = parseFloat(val('monthlyRent'));
    
    const infoWidget1 = document.getElementById('ai-price-info-1');
    const resultsWidget1 = document.getElementById('ai-price-results-1');
    const infoWidget2 = document.getElementById('ai-price-info-2');
    const resultsWidget2 = document.getElementById('ai-price-results-2');
    const loadingWidget1 = document.getElementById('ai-price-loading-1');
    const loadingWidget2 = document.getElementById('ai-price-loading-2');

    if (!acreage || !city || !expectPrice || isNaN(expectPrice) || expectPrice <= 0) {
        const guidanceMsg = "Nhập giá thuê, diện tích và địa chỉ để AI dự đoán xác suất thuê & gợi ý giá.";
        if (infoWidget1) { infoWidget1.style.display = 'block'; infoWidget1.innerText = guidanceMsg; }
        if (infoWidget2) { infoWidget2.style.display = 'block'; infoWidget2.innerText = guidanceMsg; }
        if (resultsWidget1) resultsWidget1.style.display = 'none';
        if (resultsWidget2) resultsWidget2.style.display = 'none';
        return;
    }

    if (infoWidget1) infoWidget1.style.display = 'none';
    if (infoWidget2) infoWidget2.style.display = 'none';
    if (loadingWidget1) loadingWidget1.style.display = 'flex';
    if (loadingWidget2) loadingWidget2.style.display = 'flex';
    if (resultsWidget1) resultsWidget1.style.display = 'none';
    if (resultsWidget2) resultsWidget2.style.display = 'none';

    try {
        let host = window.location.hostname;
        if (host === 'localhost') {
            host = '127.0.0.1';
        }
        
        const response = await fetch(`http://${host}:5000/predict/price`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                acreage: acreage,
                district: district || '',
                city: city,
                interior: detectedInteriorsList,
                utilities: roomUtilitiesList,
                expectPrice: expectPrice
            })
        });

        const data = await response.json();
        
        if (loadingWidget1) loadingWidget1.style.display = 'none';
        if (loadingWidget2) loadingWidget2.style.display = 'none';

        if (data.success && data.data) {
            const predPrice = data.data.predicted_price;
            const rentProb = data.data.rent_probability;
            
            let probPercent = 0;
            if (rentProb <= 1) {
                probPercent = Math.round(rentProb * 100);
            } else {
                probPercent = Math.round(rentProb);
            }
            
            const suggestedFormatted = fmt(Math.round(predPrice)) + " ₫";
            
            let comment = "";
            const priceDiff = expectPrice - predPrice;
            const percentDiff = (priceDiff / predPrice) * 100;
            
            if (percentDiff > 20) {
                comment = `⚠️ Mức giá mong muốn (${fmt(expectPrice)} ₫) đang cao hơn hẳn gợi ý AI (${suggestedFormatted}). Xác suất thuê giảm còn ${probPercent}%. Giảm giá giúp tiếp cận khách thuê nhanh hơn.`;
            } else if (percentDiff > 5) {
                comment = `💡 Giá mong muốn hơi cao hơn trung bình khu vực. Xác suất cho thuê thành công là ${probPercent}%.`;
            } else if (percentDiff < -10) {
                comment = `✨ Giá mong muốn (${fmt(expectPrice)} ₫) đang rất rẻ so với định giá AI (${suggestedFormatted}). Xác suất cho thuê cực cao (${probPercent}%).`;
            } else {
                comment = `✅ Mức giá ${fmt(expectPrice)} ₫ cực kỳ hợp lý, sát với định giá tối ưu của AI (${suggestedFormatted}). Xác suất thuê là ${probPercent}%.`;
            }

            updatePriceWidgetDOM(1, suggestedFormatted, probPercent, comment);
            updatePriceWidgetDOM(2, suggestedFormatted, probPercent, comment);
        } else {
            showPriceError("Không nhận được dữ liệu dự đoán từ AI.");
        }
    } catch (e) {
        console.error("Price Predict Error:", e);
        if (loadingWidget1) loadingWidget1.style.display = 'none';
        if (loadingWidget2) loadingWidget2.style.display = 'none';
        showPriceError("Không kết nối được tới AI Server dự đoán giá (cổng 5000).");
    }
}

function updatePriceWidgetDOM(index, suggestedPrice, probPercent, comment) {
    const resultsWidget = document.getElementById(`ai-price-results-${index}`);
    const suggestedEl = document.getElementById(`ai-price-suggested-${index}`);
    const probEl = document.getElementById(`ai-price-prob-${index}`);
    const probBar = document.getElementById(`ai-price-prob-bar-${index}`);
    const commentEl = document.getElementById(`ai-price-comment-${index}`);
    
    if (resultsWidget) resultsWidget.style.display = 'block';
    if (suggestedEl) suggestedEl.innerText = suggestedPrice;
    if (probEl) probEl.innerText = probPercent + "%";
    
    if (probBar) {
        probBar.style.width = probPercent + "%";
        if (probPercent >= 75) {
            probBar.style.background = 'linear-gradient(90deg, #28a745, #20c997)';
        } else if (probPercent >= 45) {
            probBar.style.background = 'linear-gradient(90deg, #007bff, #17a2b8)';
        } else {
            probBar.style.background = 'linear-gradient(90deg, #ffc107, #dc3545)';
        }
    }
    
    if (commentEl) commentEl.innerText = comment;
}

function showPriceError(msg) {
    const resultsWidget1 = document.getElementById('ai-price-results-1');
    const infoWidget1 = document.getElementById('ai-price-info-1');
    const resultsWidget2 = document.getElementById('ai-price-results-2');
    const infoWidget2 = document.getElementById('ai-price-info-2');
    
    if (resultsWidget1) resultsWidget1.style.display = 'none';
    if (infoWidget1) {
        infoWidget1.style.display = 'block';
        infoWidget1.innerHTML = `<span style="color: #dc3545; font-size: 13px;">❌ ${msg}</span>`;
    }
    
    if (resultsWidget2) resultsWidget2.style.display = 'none';
    if (infoWidget2) {
        infoWidget2.style.display = 'block';
        infoWidget2.innerHTML = `<span style="color: #dc3545; font-size: 13px;">❌ ${msg}</span>`;
    }
}

/* =============================================
   AI UTILITIES MANAGEMENT
   ============================================= */

function toggleUtility(val) {
    if (roomUtilitiesList.includes(val)) {
        roomUtilitiesList = roomUtilitiesList.filter(u => u !== val);
    } else {
        roomUtilitiesList.push(val);
    }
    renderUtilityTags();
    fetchAIPricePrediction();
}

function addCustomUtility() {
    const input = document.getElementById('custom-utility-input');
    if (!input) return;
    const value = input.value.trim();
    if (!value) return;
    
    if (!roomUtilitiesList.includes(value)) {
        roomUtilitiesList.push(value);
        renderUtilityTags();
        fetchAIPricePrediction();
    }
    input.value = '';
}

function removeUtilityItem(item) {
    roomUtilitiesList = roomUtilitiesList.filter(u => u !== item);
    
    // Uncheck popular checkbox if it was checked
    const checkboxes = document.querySelectorAll('input[name="utilities-checkbox"]');
    checkboxes.forEach(cb => {
        if (cb.value === item) {
            cb.checked = false;
        }
    });

    renderUtilityTags();
    fetchAIPricePrediction();
}

function renderUtilityTags() {
    const container = document.getElementById('utility-tags-container');
    const inputEl = document.getElementById('roomUtilities');
    if (!container) return;
    
    if (inputEl) {
        inputEl.value = JSON.stringify(roomUtilitiesList);
    }
    
    if (roomUtilitiesList.length === 0) {
        container.innerHTML = '<span style="color: #6c757d; font-style: italic; font-size: 13px;">Chưa chọn tiện ích nào.</span>';
        return;
    }
    
    container.innerHTML = '';
    roomUtilitiesList.forEach(item => {
        const tag = document.createElement('span');
        tag.style.cssText = 'background: #e6fffa; color: #0d9488; padding: 6px 12px; border-radius: 20px; font-size: 13px; font-weight: 500; border: 1px solid #99f6e4; display: inline-flex; align-items: center; gap: 8px;';
        
        const label = document.createElement('span');
        label.innerText = "⚡ " + item;
        
        const removeBtn = document.createElement('span');
        removeBtn.innerHTML = "✕";
        removeBtn.style.cssText = 'cursor: pointer; font-weight: bold; color: #dc3545; transition: color 0.2s;';
        removeBtn.addEventListener('mouseenter', () => removeBtn.style.color = '#bd2130');
        removeBtn.addEventListener('mouseleave', () => removeBtn.style.color = '#dc3545');
        removeBtn.onclick = () => removeUtilityItem(item);
        
        tag.appendChild(label);
        tag.appendChild(removeBtn);
        container.appendChild(tag);
    });
}

function bindAIEvents() {
    const customInput = document.getElementById('custom-interior-input');
    customInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            addCustomInterior();
        }
    });

    const customUtilityInput = document.getElementById('custom-utility-input');
    customUtilityInput?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            addCustomUtility();
        }
    });

    let pricePredictTimeout = null;
    const debouncedPricePrediction = () => {
        clearTimeout(pricePredictTimeout);
        pricePredictTimeout = setTimeout(fetchAIPricePrediction, 500);
    };

    document.getElementById('monthlyRent')?.addEventListener('input', debouncedPricePrediction);
    
    ['areaM2', 'district', 'city'].forEach(id => {
        document.getElementById(id)?.addEventListener('change', debouncedPricePrediction);
        document.getElementById(id)?.addEventListener('input', debouncedPricePrediction);
    });
}