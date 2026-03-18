const stompClient = new StompJs.Client({
    webSocketFactory: () => new SockJS('/gs-guide-websocket')
});

// ==================== LOAD CURRENT USER ====================

async function loadCurrentUserId() {
    const res = await fetch('/api/user/myid');
    const id = await res.json();
    $('#sender-id').val(id);
}

// ==================== CONNECT / DISCONNECT ====================

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
    setConnected(true);

    // Subscribe nhận tin nhắn mới
    stompClient.subscribe('/user/queue/private', (message) => {
        const msg = JSON.parse(message.body);
        showMessage(msg);
    });

    // Subscribe nhận lịch sử tin nhắn
    stompClient.subscribe('/user/queue/history', (message) => {
        const messages = JSON.parse(message.body);
        messages.forEach(msg => showMessage(msg));
    });

    // Load lịch sử ngay sau khi connect
    const senderId = $('#sender-id').val();
    const receiverId = $('#receiver-id').val();
    if (senderId && receiverId) {
        stompClient.publish({
            destination: '/app/chat.history',
            body: JSON.stringify({
                senderId: parseInt(senderId),
                receiverId: parseInt(receiverId)
            })
        });
    }
};

stompClient.onWebSocketError = (error) => {
    console.error('WebSocket error:', error);
    setConnected(false);
};

stompClient.onStompError = (frame) => {
    console.error('STOMP error:', frame.headers['message']);
    setConnected(false);
};

stompClient.onDisconnect = () => {
    setConnected(false);
};

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
}

// ==================== UI STATE ====================

function setConnected(connected) {
    const banner = document.getElementById('connection-banner');
    const bannerText = document.getElementById('banner-text');
    const bannerBtn = document.getElementById('banner-connect-btn');
    const status = document.getElementById('connection-status');
    const connectBtn = document.getElementById('connect-btn');
    const disconnectBtn = document.getElementById('disconnect-btn');

    if (connected) {
        banner.classList.add('connected');
        bannerText.textContent = 'Đã kết nối WebSocket';
        bannerBtn.textContent = 'Ngắt';
        bannerBtn.onclick = disconnect;
        status.textContent = 'Online';
        status.className = 'header-status online';
        connectBtn.classList.add('active');
        connectBtn.disabled = true;
        disconnectBtn.disabled = false;
    } else {
        banner.classList.remove('connected');
        bannerText.textContent = 'WebSocket chưa kết nối';
        bannerBtn.textContent = 'Kết nối';
        bannerBtn.onclick = connect;
        status.textContent = 'Offline';
        status.className = 'header-status';
        connectBtn.classList.remove('active');
        connectBtn.disabled = false;
        disconnectBtn.disabled = true;
    }
}

// ==================== SEND MESSAGE ====================

function sendMessage() {
    const content = $('#message-input').val().trim();
    const receiverId = $('#receiver-id').val();
    const senderId = $('#sender-id').val();

    if (!content) return;
    if (!receiverId || !senderId) {
        alert('Vui lòng nhập Receiver ID!');
        return;
    }
    if (!stompClient.connected) {
        alert('Chưa kết nối WebSocket!');
        return;
    }

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

    $('#message-input').val('');
    autoResizeTextarea();
}

// ==================== SHOW MESSAGE ====================

function showMessage(msg) {
    const currentUserId = parseInt($('#sender-id').val());
    const isMe = msg.senderId === currentUserId;

    $('#empty-state').hide();

    if (!isMe) {
        $('#receiver-name').text('User ' + msg.senderId);
        $('#receiver-avatar').text(String(msg.senderId).charAt(0).toUpperCase());
    }

    const time = msg.sentAt
        ? new Date(msg.sentAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
        : new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });

    const wrapper = $('<div>').addClass('msg-wrapper').addClass(isMe ? 'me' : 'them');
    const bubble = $('<div>').addClass('msg-bubble').text(msg.content);
    const meta = $('<div>').addClass('msg-meta').text(time);

    wrapper.append(bubble).append(meta);
    $('#typing-indicator').before(wrapper);

    const messagesEl = document.getElementById('messages');
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

// ==================== TEXTAREA AUTO RESIZE ====================

function autoResizeTextarea() {
    const textarea = document.getElementById('message-input');
    textarea.style.height = 'auto';
    textarea.style.height = Math.min(textarea.scrollHeight, 100) + 'px';
}

// ==================== EVENT LISTENERS ====================

$(function () {
    // Load current user ID ngay khi trang load
    loadCurrentUserId().then(() => {
        connect();
    });

    $('#connect-btn').click(connect);
    $('#disconnect-btn').click(disconnect);
    $('#banner-connect-btn').click(connect);

    $('#send-btn').click(sendMessage);

    $('#message-input').on('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    $('#message-input').on('input', autoResizeTextarea);

    // Load lại lịch sử khi đổi receiver
    $('#receiver-id').on('input', function () {
        const id = $(this).val();
        if (id) {
            $('#receiver-name').text('User ' + id);
            $('#receiver-avatar').text(id.charAt(0));

            if (stompClient.connected && $('#sender-id').val()) {
                $('#messages').find('.msg-wrapper').remove();
                $('#empty-state').show();
                stompClient.publish({
                    destination: '/app/chat.history',
                    body: JSON.stringify({
                        senderId: parseInt($('#sender-id').val()),
                        receiverId: parseInt(id)
                    })
                });
            }
        } else {
            $('#receiver-name').text('Chọn người nhận');
            $('#receiver-avatar').text('?');
        }
    });
});