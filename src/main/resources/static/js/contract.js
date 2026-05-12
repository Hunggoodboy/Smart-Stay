document.addEventListener("DOMContentLoaded", () => {
    const now = new Date();
    document.getElementById("signDay").value   = now.getDate();
    document.getElementById("signMonth").value = now.getMonth() + 1;
    document.getElementById("signYear").value  = now.getFullYear();

    const urlParams  = new URLSearchParams(window.location.search);
    const roomPostId = urlParams.get('roomPostId');
    const tenantId   = urlParams.get('tenantId');

    if (roomPostId && tenantId) {
        loadContractDraftInfo(roomPostId, tenantId);
    }
});

async function loadContractDraftInfo(roomPostId, tenantId) {
    try {
        const token    = localStorage.getItem('smartstay_token');
        const response = await fetch(`/api/contract/draft?roomPostId=${roomPostId}&userId=${tenantId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Lỗi tải dữ liệu!");

        const d = await response.json();   // d = toàn bộ JSON từ backend

        // ── Hidden IDs ────────────────────────────────────────────────
        document.getElementById("landLordId").value = d.landLordId;
        document.getElementById("customerId").value = d.customerId;
        document.getElementById("roomPostId").value = d.roomPostId;

        // ── BÊN A: Chủ nhà ───────────────────────────────────────────
        setVal("inputLandLordName",     d.landLordName);               // "Lê Thị Mai"
        setVal("inputLandLordIdentity", d.landLordIdentityNumber);     // "079182005678"
        setVal("inputLandLordAddress",  d.landLordAddress);            // "45 Lê Lợi, Quận 3, TP.HCM"

        // Chữ ký điện tử tự động từ tên chủ nhà
        document.getElementById("landLordSignature").innerText = d.landLordName || "";
        document.getElementById("signLandLordName").innerText  = d.landLordName || "";

        // ── BÊN B: Người thuê ─────────────────────────────────────────
        setVal("inputCustomerName",     d.customerName);               // "Lê Văn Cường"
        setVal("inputCustomerIdentity", d.customerIdentityNumber);     // null → bỏ qua
        setVal("inputCustomerAddress",  d.customerAddress);            // "77 Bùi Thị Xuân, Quận 1, TP.HCM"

        document.getElementById("signCustomerName").innerText  = d.customerName || "";

        // ── Điều 1: Thông tin phòng ───────────────────────────────────
        // Ghép đầy đủ: roomAddress + ward + district + city
        const fullAddress = [d.roomAddress || d.address, d.ward, d.district, d.city]
            .filter(Boolean).join(", ");
        setVal("roomAddress", fullAddress);   // "Số 5, Tổ 3 Đông Ngạc, Đông Ngạc, Bắc Từ Liêm, Hà Nội"
        setVal("roomArea",    d.areaM2 ?? d.roomArea);   // 18.0

// ── Điều 2: Tài chính ─────────────────────────────────────────
        const today = new Date();
        const nextYear = new Date(today);
        nextYear.setFullYear(today.getFullYear() + 1); // Cộng thêm 1 năm cho ngày kết thúc

        setVal("startDate", today.toISOString().split('T')[0]);
        setVal("endDate",   nextYear.toISOString().split('T')[0]); // Gán giá trị mặc định 1 năm

        setVal("monthlyRent",    d.rentPrice);       // 2300000
        setVal("depositAmount",  d.depositAmount ?? 0);

        // ── Phí dịch vụ ───────────────────────────────────────────────
        setVal("electricityRate", d.electricityPricePerKwh);  // 3500
        setVal("waterRate",       d.waterPricePerM3);          // 22000
        setVal("internetFee",     d.internetFee);              // 70000
        setVal("parkingFee",      d.parkingFee);               // 30000
        setVal("cleaningFee",     d.cleaningFee);              // 15000

        // ── Số người tối đa ───────────────────────────────────────────
        setVal("numOccupants", d.maxOccupants ?? 1);           // 2

        // ── Sync chữ ký realtime khi người dùng tự sửa tên ──────────
        document.getElementById("inputLandLordName").addEventListener("input", function () {
            document.getElementById("landLordSignature").innerText = this.value;
            document.getElementById("signLandLordName").innerText  = this.value;
        });
        document.getElementById("inputCustomerName").addEventListener("input", function () {
            document.getElementById("signCustomerName").innerText  = this.value;
        });

    } catch (err) {
        console.error(err);
        alert("Lỗi tải dữ liệu: " + err.message);
    }
}

/** Set giá trị vào input theo id — bỏ qua nếu null/undefined */
function setVal(id, value) {
    const el = document.getElementById(id);
    if (el && value !== null && value !== undefined) el.value = value;
}

// ── Submit tạo hợp đồng ───────────────────────────────────────────────────
document.getElementById("contractForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const payload = {
        landLordId:         parseInt(document.getElementById("landLordId").value),
        customerId:         parseInt(document.getElementById("customerId").value),
        roomPostId:         parseInt(document.getElementById("roomPostId").value),
        monthlyRent:        parseFloat(document.getElementById("monthlyRent").value),
        depositAmount:      parseFloat(document.getElementById("depositAmount").value),
        electricityRate:    parseFloat(document.getElementById("electricityRate").value),
        waterRate:          parseFloat(document.getElementById("waterRate").value),
        internetFee:        parseFloat(document.getElementById("internetFee").value),
        parkingFee:         parseFloat(document.getElementById("parkingFee").value),
        cleaningFee:        parseFloat(document.getElementById("cleaningFee").value),
        numOccupants:       parseInt(document.getElementById("numOccupants").value),
        billingDate:        parseInt(document.getElementById("billingDate").value),
        paymentCycle:       parseInt(document.getElementById("paymentCycle").value),
        startDate:          document.getElementById("startDate").value,
        termsAndConditions: "Đã được ký điện tử bởi Bên Cho Thuê."
    };

    try {
        const token    = localStorage.getItem('smartstay_token');
        const response = await fetch("/api/contract/create", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert("Hợp đồng đã được ký và gửi thành công!");
            window.location.href = "/MyRentalRequest";
        } else {
            alert("Lỗi khi tạo hợp đồng!");
        }
    } catch (err) {
        console.error(err);
        alert("Lỗi kết nối máy chủ!");
    }
});