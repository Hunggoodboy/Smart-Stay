function formatCurrencyVnd(value) {
    const amount = Number(value || 0);
    return amount.toLocaleString("vi-VN") + "đ";
}

function formatRelativeTime(value) {
    if (!value) return "Vua xong";
    const parsed = new Date(String(value).replace(" ", "T"));
    if (Number.isNaN(parsed.getTime())) return "Vua xong";

    const minutes = Math.floor((Date.now() - parsed.getTime()) / 60000);
    if (minutes < 1) return "Vua xong";
    if (minutes < 60) return `${minutes} phut truoc`;

    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours} gio truoc`;

    const days = Math.floor(hours / 24);
    if (days < 7) return `${days} ngay truoc`;

    return parsed.toLocaleDateString("vi-VN");
}

function escapeHtml(value) {
    if (value == null) return "";
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function setText(id, text) {
    const element = document.getElementById(id);
    if (element) element.textContent = text;
}

function renderMessages(messages) {
    const container = document.getElementById("message-notification-list");
    if (!container) return;

    if (!Array.isArray(messages) || messages.length === 0) {
        container.innerHTML = `
            <div class="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                Chua co thong bao tin nhan moi.
            </div>
        `;
        return;
    }

    const rows = messages.map((item) => {
        const unreadCount = Number(item.unreadCount || 0);
        const theme = unreadCount > 0 ? "rose" : "sky";
        const partner = escapeHtml(item.partnerName || "Nguoi dung");
        const content = escapeHtml(item.lastMessage || "Khong co noi dung");
        const timeLabel = formatRelativeTime(item.lastMessageAt);
        const unreadBadge = unreadCount > 0
            ? `<span class="inline-flex min-w-6 items-center justify-center rounded-full bg-rose-100 px-2 py-0.5 text-xs font-semibold text-rose-700">${unreadCount}</span>`
            : "";

        return `
            <div class="flex items-start gap-3 rounded-xl border border-${theme}-100 bg-${theme}-50/80 p-3">
                <span class="mt-0.5 h-2.5 w-2.5 rounded-full bg-${theme}-500"></span>
                <div class="min-w-0 flex-1">
                    <div class="flex items-start justify-between gap-2">
                        <p class="m-0 truncate text-sm font-semibold text-slate-900">Tin nhan moi tu ${partner}</p>
                        ${unreadBadge}
                    </div>
                    <p class="m-0 mt-1 text-sm text-slate-600">\"${content}\" - ${timeLabel}</p>
                </div>
            </div>
        `;
    });

    container.innerHTML = rows.join("");
}

function renderDashboard(data) {
    setText("total-rooms-value", String(data.totalRooms ?? 0));
    setText("occupied-rooms-value", String(data.occupiedRooms ?? 0));
    setText("expiring-contracts-value", String(data.expiringContracts ?? 0));
    setText("overdue-debts-value", String(data.overduePayments ?? 0));

    setText("occupancy-rate-text", `Ty le lap day: ${Number(data.occupancyRate || 0).toFixed(1)}%`);
    setText("expected-revenue-value", formatCurrencyVnd(data.expectedRevenue));
    setText("collected-revenue-value", formatCurrencyVnd(data.collectedRevenue));
    setText("pending-revenue-value", formatCurrencyVnd(data.pendingRevenue));

    renderMessages(data.recentMessages);
}

async function loadDashboardData() {
    const container = document.getElementById("message-notification-list");
    if (container) {
        container.innerHTML = `
            <div class="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                Dang tai thong so dashboard...
            </div>
        `;
    }

    try {
        const response = await fetch("/api/dashboard/overview", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ messageLimit: 3 })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        renderDashboard(data);
    } catch (error) {
        if (container) {
            container.innerHTML = `
                <div class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                    Khong the tai du lieu dashboard luc nay.
                </div>
            `;
        }
        console.error("Failed to load dashboard data", error);
    }
}

document.addEventListener("DOMContentLoaded", loadDashboardData);
