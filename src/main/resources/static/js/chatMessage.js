const token = localStorage.getItem('smartstay_token');
let currentReceiverId = localStorage.getItem('chat_receiver_id'); // Lấy tự động ID người nhận nếu có sẵn
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

    try {
        // Lấy ID của chính mình
        const res = await fetch('/api/user/myid', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        senderId = await res.json();

        // Fetch danh sách summary
        await fetchSummaries();

        // Bắt đầu kết nối WebSocket
        stompClient.activate();

        // Nếu có sẵn receiverId (từ trang khác chuyển sang), mở luôn
        if (currentReceiverId) {
            // Cần lấy tên của receiverId từ list nếu có, nếu không thì để mặc định
            openChat(currentReceiverId, "Đối tác #" + currentReceiverId);
        }
    } catch (error) {
        alert("Lỗi xác thực người dùng");
    }
}

// ==================== FETCH SUMMARIES ====================
async function fetchSummaries() {
    try {
        const response = await fetch('/api/chat/summary', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        const listContainer = document.getElementById('conversationList');
        
        if (response.status === 404) {
            listContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #94a3b8; font-size: 14px;">Chưa có tin nhắn nào.</div>';
            return;
        }

        if (!response.ok) throw new Error('Failed to fetch summary');

        const summaries = await response.json();
        renderSummaries(summaries);
        
        // Cập nhật tên nếu đã có receiverId
        if (currentReceiverId) {
            const summary = summaries.find(s => s.partnerId == currentReceiverId);
            if (summary) {
                document.getElementById('headerName').innerText = summary.partnerName || ("Đối tác #" + currentReceiverId);
                document.getElementById('headerAvatar').innerText = (summary.partnerName ? summary.partnerName.charAt(0).toUpperCase() : "?");
            }
        }
    } catch (error) {
        console.error("Lỗi lấy danh sách tin nhắn:", error);
        document.getElementById('conversationList').innerHTML = '<div style="padding: 20px; text-align: center; color: #ef4444; font-size: 14px;">Không thể tải danh sách.</div>';
    }
}

function renderSummaries(summaries) {
    const listContainer = document.getElementById('conversationList');
    listContainer.innerHTML = '';
    
    if (summaries.length === 0) {
        listContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #94a3b8; font-size: 14px;">Chưa có tin nhắn nào.</div>';
        return;
    }
    
    summaries.forEach(item => {
        // item: partnerId, partnerName, message, timestamp
        const timeString = item.timestamp ? new Date(item.timestamp).toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'}) : '';
        const name = item.partnerName || `Người dùng #${item.partnerId}`;
        const initial = name.charAt(0).toUpperCase();
        const isActive = (item.partnerId == currentReceiverId) ? 'active' : '';
        
        const html = `
            <div class="conversation-item ${isActive}" onclick="openChat('${item.partnerId}', '${name.replace(/'/g, "\\'")}')" id="conv-${item.partnerId}">
                <div class="conv-avatar">${initial}</div>
                <div class="conv-info">
                    <div style="display: flex; justify-content: space-between; align-items: baseline;">
                        <div class="conv-name">${name}</div>
                        <div class="conv-time">${timeString}</div>
                    </div>
                    <div class="conv-msg">${item.message || 'Chưa có tin nhắn'}</div>
                </div>
            </div>
        `;
        listContainer.insertAdjacentHTML('beforeend', html);
    });
}

function openChat(partnerId, partnerName) {
    currentReceiverId = partnerId;
    localStorage.setItem('chat_receiver_id', partnerId); // Lưu lại
    
    // Cập nhật UI active
    document.querySelectorAll('.conversation-item').forEach(el => el.classList.remove('active'));
    const activeItem = document.getElementById(`conv-${partnerId}`);
    if (activeItem) activeItem.classList.add('active');
    
    // Ẩn Empty state, hiện Chat Area
    document.getElementById('emptyChat').style.display = 'none';
    const chatArea = document.getElementById('chatArea');
    chatArea.style.display = 'flex';
    
    // Mobile toggle
    document.getElementById('chatContainer').classList.add('show-chat');
    
    // Cập nhật Header Chat
    document.getElementById('headerName').innerText = partnerName;
    document.getElementById('headerAvatar').innerText = partnerName ? partnerName.charAt(0).toUpperCase() : "?";
    
    // Xóa tin nhắn cũ, hiện loading
    const area = document.getElementById('messagesArea');
    area.innerHTML = '<div style="text-align: center; color: #94a3b8; font-size: 13px; margin-top: 40px;">Đang tải lịch sử trò chuyện...</div>';
    
    // Nếu WebSocket đã connect, gửi yêu cầu lịch sử ngay
    if (stompClient.connected) {
        requestHistory();
    }
}

function closeChatMobile() {
    document.getElementById('chatContainer').classList.remove('show-chat');
}

function requestHistory() {
    if (!currentReceiverId || !senderId) return;
    stompClient.publish({
        destination: '/app/chat.history',
        body: JSON.stringify({
            senderId: parseInt(senderId),
            receiverId: parseInt(currentReceiverId)
        })
    });
}

// ==================== WEBSOCKET EVENTS ====================
stompClient.onConnect = (frame) => {
    updateStatus(true);

    // Đăng ký nhận tin nhắn mới
    stompClient.subscribe('/topic/private/' + senderId, (message) => {
        const msg = JSON.parse(message.body);
        
        // Nếu tin nhắn thuộc về cuộc hội thoại đang mở, render nó
        if (msg.senderId == currentReceiverId || msg.receiverId == currentReceiverId) {
            renderMessage(msg);
        }
        
        // Cập nhật lại danh sách summary (để đưa tin nhắn này lên đầu / cập nhật text)
        fetchSummaries();
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
        
        // Cuộn xuống cuối
        setTimeout(() => { area.scrollTop = area.scrollHeight; }, 100);
    });
    
    // Nếu có receiverId được chọn sẵn, gọi lịch sử
    if (currentReceiverId) {
        setTimeout(requestHistory, 500);
    }
};

stompClient.onDisconnect = () => updateStatus(false);
stompClient.onWebSocketError = () => updateStatus(false);

// ==================== GỬI VÀ HIỂN THỊ ====================
function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();

    if (!content || !stompClient.connected || !currentReceiverId) return;

    const request = {
        senderId: parseInt(senderId),
        receiverId: parseInt(currentReceiverId),
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
        if(dot) dot.classList.add('online');
        if(text) text.innerText = 'Đang hoạt động';
    } else {
        if(dot) dot.classList.remove('online');
        if(text) text.innerText = 'Mất kết nối';
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