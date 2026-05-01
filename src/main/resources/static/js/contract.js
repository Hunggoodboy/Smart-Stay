document.addEventListener("DOMContentLoaded", () => {
    // 1. Điền ngày hiện tại
    const now = new Date();
    document.getElementById("signDay").value = now.getDate();
    document.getElementById("signMonth").value = now.getMonth() + 1;
    document.getElementById("signYear").value = now.getFullYear();

    const urlParams = new URLSearchParams(window.location.search);
    const roomId = urlParams.get('roomId');
    const tenantId = urlParams.get('tenantId'); // User ID từ yêu cầu thuê

    if (roomId && tenantId) {
        loadContractDraftInfo(roomId, tenantId);
    }
});

async function loadContractDraftInfo(roomId, tenantId) {
    try {
        const token = localStorage.getItem('smartstay_token');
        const response = await fetch(`/api/contract/draft?roomId=${roomId}&userId=${tenantId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error("Lỗi tải dữ liệu gợi ý!");

        const data = await response.json();

        // Gán giá trị vào Hidden IDs
        document.getElementById("landLordId").value = data.landLordId;
        document.getElementById("customerId").value = data.customerId;
        document.getElementById("roomId").value = data.roomId;

        // Điền thông tin hiển thị và Chữ ký
        document.getElementById("displayLandLordName").innerText = data.landLordName;
        document.getElementById("displayLandLordIdentity").innerText = data.landLordIdentityNumber;
        document.getElementById("displayLandLordAddress").innerText = data.landLordAddress;
        document.getElementById("signLandLordName").innerText = data.landLordName;

        // TẠO CHỮ KÝ TỰ ĐỘNG CHO CHỦ NHÀ
        document.getElementById("landLordSignature").innerText = data.landLordName;

        document.getElementById("displayCustomerName").innerText = data.customerName;
        document.getElementById("displayCustomerIdentity").innerText = data.customerIdentityNumber;
        document.getElementById("displayCustomerAddress").innerText = data.customerAddress;
        document.getElementById("signCustomerName").innerText = data.customerName;

        // Điền dữ liệu vào các ô nhập liệu (Input)
        document.getElementById("roomAddress").value = data.roomAddress || "";
        document.getElementById("roomArea").value = data.roomArea || 0;
        document.getElementById("monthlyRent").value = data.rentPrice || 0;
        document.getElementById("electricityRate").value = data.electricityPricePerKwh || 0;
        document.getElementById("waterRate").value = data.waterPricePerM3 || 0;
        document.getElementById("internetFee").value = data.internetFee || 0;
        document.getElementById("cleaningFee").value = data.cleaningFee || 0;
        document.getElementById("parkingFee").value = data.parkingFee || 0;

        document.getElementById("startDate").value = new Date().toISOString().split('T')[0];

    } catch (error) {
        console.error(error);
        alert("Lỗi tải dữ liệu: " + error.message);
    }
}

document.getElementById("contractForm").addEventListener("submit", async function(e) {
    e.preventDefault();

    const payload = {
        landLordId: parseInt(document.getElementById("landLordId").value),
        customerId: parseInt(document.getElementById("customerId").value),
        roomId: parseInt(document.getElementById("roomId").value),
        monthlyRent: parseFloat(document.getElementById("monthlyRent").value),
        depositAmount: parseFloat(document.getElementById("depositAmount").value),
        electricityRate: parseFloat(document.getElementById("electricityRate").value),
        waterRate: parseFloat(document.getElementById("waterRate").value),
        billingDate: parseInt(document.getElementById("billingDate").value),
        numOccupants: parseInt(document.getElementById("numOccupants").value),
        internetFee: parseFloat(document.getElementById("internetFee").value),
        parkingFee: parseFloat(document.getElementById("parkingFee").value),
        cleaningFee: parseFloat(document.getElementById("cleaningFee").value),
        startDate: document.getElementById("startDate").value,
        paymentCycle: parseInt(document.getElementById("paymentCycle").value),
        termsAndConditions: "Đã được ký điện tử bởi Bên Cho Thuê."
    };

    try {
        const token = localStorage.getItem('smartstay_token');
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
            window.location.href = "/landlord/requests";
        } else {
            alert("Lỗi khi tạo hợp đồng!");
        }
    } catch (error) {
        console.error(error);
        alert("Lỗi kết nối máy chủ!");
    }
});