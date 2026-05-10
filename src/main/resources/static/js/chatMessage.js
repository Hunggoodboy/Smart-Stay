const token = localStorage.getItem('smartstay_token');
const receiverId = localStorage.getItem('chat_receiver_id'); // Lấy tự động ID người nhận
let senderId = null;

const stompClient = new StompJs.Client({
    webSocketFactory: () => new SockJS('/gs-guide-websocket'),
    debug: function (str) { console.log(str); }
});

// ==================== KHỞI TẠO ====================
async function initChat() {
    if (!token) {
        window.location.href = '/login';
        return;
    }
    if (!receiverId) {
        alert('Không tìm thấy thông tin đối tác trò chuyện!');
        window.history.back();
        return;
    }

    try {
        // Lấy ID của chính mình
        const res = await fetch('/api/user/myid', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        senderId = await res.json();

        // Cập nhật UI cơ bản
        document.getElementById('headerName').innerText = `Đối tác #${receiverId}`;
        document.getElementById('headerAvatar').innerText = "C";

        // Bắt đầu kết nối WebSocket
        stompClient.activate();
    } catch (error) {
        alert("Lỗi xác thực người dùng");
    }
}

// ==================== WEBSOCKET EVENTS ====================
stompClient.onConnect = (frame) => {
    updateStatus(true);

    // Đăng ký nhận tin nhắn mới
    stompClient.subscribe('/topic/private/' + senderId, (message) => {
        const msg = JSON.parse(message.body);
        renderMessage(msg);
    });

    // Đăng ký nhận lịch sử
    stompClient.subscribe('/topic/history/' + senderId, (message) => {
        const messages = JSON.parse(message.body);
        const area = document.getElementById('messagesArea');
        area.innerHTML = ''; // Xóa chữ "Đang tải"

        if(messages.length === 0) {
            area.innerHTML = '<div style="text-align:center; color:#94a3b8; font-size:13px; margin-top:20px;">Hãy gửi lời chào đầu tiên! 👋</div>';
        } else {
            messages.forEach(msg => renderMessage(msg));
        }
    });
    setTimeout(() => {
        stompClient.publish({
            destination: '/app/chat.history',
            body: JSON.stringify({
                senderId: parseInt(senderId),
                receiverId: parseInt(receiverId)
            })
        });
    }, 500);
    // GỌI LỊCH SỬ NGAY KHI KẾT NỐI XONG
    stompClient.publish({
        destination: '/app/chat.history',
        body: JSON.stringify({
            senderId: parseInt(senderId),
            receiverId: parseInt(receiverId)
        })
    });
};

stompClient.onDisconnect = () => updateStatus(false);
stompClient.onWebSocketError = () => updateStatus(false);

// ==================== GỬI VÀ HIỂN THỊ ====================
function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();

    if (!content || !stompClient.connected) return;

    const request = {
        senderId: parseInt(senderId),
        receiverId: parseInt(receiverId),
        content: content,
        messageType: 'TEXT',
        chatType: 'PRIVATE'
    };

    stompClient.publish({
        destination: '/app/chat.private',
        body: JSON.stringify(request)
    });

    input.value = '';
    input.style.height = 'auto'; // Reset chiều cao ô nhập
}

function renderMessage(msg) {
    const isMe = msg.senderId === senderId;
    const area = document.getElementById('messagesArea');

    // Nếu có tin nhắn mới, xóa dòng "Hãy gửi lời chào"
    if(area.innerHTML.includes('Hãy gửi lời chào')) area.innerHTML = '';

    const timeString = msg.sentAt
        ? new Date(msg.sentAt).toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'})
        : new Date().toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'});

    const html = `
        <div class="msg-row ${isMe ? 'me' : 'them'}">
            <div class="bubble">${msg.content}</div>
            <div class="time">${timeString}</div>
        </div>
    `;

    area.insertAdjacentHTML('beforeend', html);
    area.scrollTop = area.scrollHeight; // Tự động cuộn xuống tin nhắn mới nhất
}

function updateStatus(isOnline) {
    const dot = document.getElementById('statusDot');
    const text = document.getElementById('statusText');
    if (isOnline) {
        dot.classList.add('online');
        text.innerText = 'Đang hoạt động';
    } else {
        dot.classList.remove('online');
        text.innerText = 'Mất kết nối';
    }
}

// Bắt sự kiện Enter để gửi tin nhắn
document.getElementById('messageInput').addEventListener('keydown', (e) => {
    // QUAN TRỌNG: Ngăn chặn việc gửi tin nhắn khi bộ gõ tiếng Việt đang tổ hợp chữ
    if (e.isComposing || e.keyCode === 229) {
        return;
    }

    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

// Khởi chạy khi vào trang
document.addEventListener('DOMContentLoaded', initChat);