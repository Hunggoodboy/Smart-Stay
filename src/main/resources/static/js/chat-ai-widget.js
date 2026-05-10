(function () {
    // ─── Inject Google Fonts ───────────────────────────────────────────────────
    const fontLink = document.createElement("link");
    fontLink.rel = "stylesheet";
    fontLink.href =
        "https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700&family=DM+Sans:wght@300;400;500&display=swap";
    document.head.appendChild(fontLink);

    // ─── Inject Styles ────────────────────────────────────────────────────────
    const style = document.createElement("style");
    style.textContent = `
    :root {
      --chat-primary: #0f0f12;
      --chat-surface: #1a1a22;
      --chat-surface2: #22222e;
      --chat-border: rgba(255,255,255,0.07);
      --chat-accent: #1e3a8a;
      --chat-accent2: #3b82f6;
      --chat-text: #e8e8f0;
      --chat-muted: #6b6b80;
      --chat-user-bubble: linear-gradient(135deg, #1e3a8a, #3b82f6);
      --chat-ai-bubble: #22222e;
      --chat-radius: 20px;
      --chat-shadow: 0 32px 80px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.05);
    }

    #chat-ai-fab {
      position: fixed;
      bottom: 28px;
      right: 28px;
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
      box-shadow: 0 8px 32px rgba(59,130,246,0.45), 0 2px 8px rgba(0,0,0,0.3);
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 99999;
      transition: transform 0.3s cubic-bezier(.34,1.56,.64,1), box-shadow 0.3s ease;
      outline: none;
    }
    #chat-ai-fab:hover {
      transform: scale(1.1) rotate(-5deg);
      box-shadow: 0 12px 40px rgba(59,130,246,0.6), 0 2px 8px rgba(0,0,0,0.3);
    }
    #chat-ai-fab.is-open {
      transform: scale(0.9) rotate(90deg);
      background: linear-gradient(135deg, #3b82f6 0%, #1e3a8a 100%);
    }
    #chat-ai-fab svg { pointer-events: none; }

    /* Pulse ring */
    #chat-ai-fab::before {
      content: '';
      position: absolute;
      inset: -4px;
      border-radius: 50%;
      background: linear-gradient(135deg, #1e3a8a, #3b82f6);
      opacity: 0;
      animation: chat-pulse 2.5s ease-in-out infinite;
      z-index: -1;
    }
    @keyframes chat-pulse {
      0%   { transform: scale(1);   opacity: 0.6; }
      70%  { transform: scale(1.35); opacity: 0; }
      100% { transform: scale(1.35); opacity: 0; }
    }

    /* Notification dot */
    #chat-ai-fab .chat-dot {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 10px;
      height: 10px;
      background: #2dffb3;
      border-radius: 50%;
      border: 2px solid #0f0f12;
      box-shadow: 0 0 8px rgba(45,255,179,0.7);
    }

    /* ─── Window ─────────────────────────────────────────────────── */
    #chat-ai-window {
      position: fixed;
      bottom: 104px;
      right: 28px;
      width: 380px;
      height: 560px;
      background: var(--chat-primary);
      border-radius: var(--chat-radius);
      box-shadow: var(--chat-shadow);
      border: 1px solid var(--chat-border);
      display: flex;
      flex-direction: column;
      z-index: 99998;
      overflow: hidden;
      font-family: 'DM Sans', sans-serif;
      transform: scale(0.85) translateY(20px);
      opacity: 0;
      pointer-events: none;
      transform-origin: bottom right;
      transition: transform 0.35s cubic-bezier(.34,1.36,.64,1), opacity 0.25s ease;
    }
    #chat-ai-window.is-visible {
      transform: scale(1) translateY(0);
      opacity: 1;
      pointer-events: all;
    }

    /* Header */
    .chat-header {
      padding: 18px 20px 16px;
      background: var(--chat-surface);
      border-bottom: 1px solid var(--chat-border);
      display: flex;
      align-items: center;
      gap: 12px;
      flex-shrink: 0;
    }
    .chat-header-avatar {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: linear-gradient(135deg, #1e3a8a, #3b82f6);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      position: relative;
    }
    .chat-header-avatar::after {
      content: '';
      position: absolute;
      bottom: -1px;
      right: -1px;
      width: 10px;
      height: 10px;
      background: #2dffb3;
      border-radius: 50%;
      border: 2px solid var(--chat-surface);
    }
    .chat-header-info { flex: 1; min-width: 0; }
    .chat-header-name {
      font-family: 'Syne', sans-serif;
      font-weight: 700;
      font-size: 14px;
      color: var(--chat-text);
      letter-spacing: 0.02em;
    }
    .chat-header-status {
      font-size: 11px;
      color: #2dffb3;
      margin-top: 1px;
      font-weight: 500;
    }
    .chat-close-btn {
      width: 30px;
      height: 30px;
      border-radius: 8px;
      background: transparent;
      border: 1px solid var(--chat-border);
      color: var(--chat-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      flex-shrink: 0;
    }
    .chat-close-btn:hover {
      background: var(--chat-surface2);
      color: var(--chat-text);
    }

    /* Messages */
    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 20px 16px;
      display: flex;
      flex-direction: column;
      gap: 14px;
      scroll-behavior: smooth;
    }
    .chat-messages::-webkit-scrollbar { width: 4px; }
    .chat-messages::-webkit-scrollbar-track { background: transparent; }
    .chat-messages::-webkit-scrollbar-thumb {
      background: var(--chat-surface2);
      border-radius: 4px;
    }

    .chat-msg {
      display: flex;
      gap: 8px;
      align-items: flex-end;
      animation: chat-msg-in 0.3s cubic-bezier(.34,1.4,.64,1) both;
    }
    @keyframes chat-msg-in {
      from { opacity: 0; transform: translateY(10px) scale(0.96); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }
    .chat-msg.user { flex-direction: row-reverse; }

    .chat-msg-avatar {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      font-weight: 600;
      font-family: 'Syne', sans-serif;
    }
    .chat-msg.ai .chat-msg-avatar {
      background: linear-gradient(135deg, #1e3a8a, #3b82f6);
      color: white;
    }
    .chat-msg.user .chat-msg-avatar {
      background: var(--chat-surface2);
      color: var(--chat-muted);
      font-size: 11px;
    }

    .chat-bubble {
      max-width: 76%;
      padding: 11px 15px;
      border-radius: 16px;
      font-size: 13.5px;
      line-height: 1.6;
      word-break: break-word;
    }
    .chat-msg.ai .chat-bubble {
      background: var(--chat-ai-bubble);
      color: var(--chat-text);
      border-bottom-left-radius: 4px;
      border: 1px solid var(--chat-border);
    }
    .chat-msg.user .chat-bubble {
      background: var(--chat-user-bubble);
      color: white;
      border-bottom-right-radius: 4px;
    }

    /* Typing indicator */
    .chat-typing {
      display: flex;
      gap: 8px;
      align-items: flex-end;
      animation: chat-msg-in 0.3s ease both;
    }
    .chat-typing-avatar {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background: linear-gradient(135deg, #1e3a8a, #3b82f6);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 13px;
      font-weight: 700;
      font-family: 'Syne', sans-serif;
      flex-shrink: 0;
    }
    .chat-typing-bubble {
      background: var(--chat-ai-bubble);
      border: 1px solid var(--chat-border);
      border-radius: 16px;
      border-bottom-left-radius: 4px;
      padding: 13px 18px;
      display: flex;
      gap: 5px;
      align-items: center;
    }
    .chat-typing-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--chat-muted);
      animation: chat-typing-bounce 1.2s ease infinite;
    }
    .chat-typing-dot:nth-child(2) { animation-delay: 0.18s; }
    .chat-typing-dot:nth-child(3) { animation-delay: 0.36s; }
    @keyframes chat-typing-bounce {
      0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
      30%            { transform: translateY(-5px); opacity: 1; }
    }

    /* Welcome */
    .chat-welcome {
      text-align: center;
      padding: 16px 12px 8px;
    }
    .chat-welcome-icon {
      width: 52px;
      height: 52px;
      border-radius: 16px;
      background: linear-gradient(135deg, #1e3a8a, #3b82f6);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
      box-shadow: 0 8px 24px rgba(59,130,246,0.35);
    }
    .chat-welcome h3 {
      font-family: 'Syne', sans-serif;
      font-size: 15px;
      font-weight: 700;
      color: var(--chat-text);
      margin: 0 0 6px;
    }
    .chat-welcome p {
      font-size: 12.5px;
      color: var(--chat-muted);
      margin: 0;
      line-height: 1.5;
    }

    /* Input area */
    .chat-input-area {
      padding: 14px 16px;
      background: var(--chat-surface);
      border-top: 1px solid var(--chat-border);
      display: flex;
      gap: 10px;
      align-items: flex-end;
      flex-shrink: 0;
    }
    .chat-input-wrap {
      flex: 1;
      background: var(--chat-surface2);
      border: 1px solid var(--chat-border);
      border-radius: 14px;
      padding: 10px 14px;
      transition: border-color 0.2s;
    }
    .chat-input-wrap:focus-within {
      border-color: rgba(59,130,246,0.5);
      box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
    }
    #chat-ai-input {
      width: 100%;
      background: transparent;
      border: none;
      outline: none;
      color: var(--chat-text);
      font-family: 'DM Sans', sans-serif;
      font-size: 13.5px;
      resize: none;
      max-height: 100px;
      min-height: 20px;
      line-height: 1.5;
    }
    #chat-ai-input::placeholder { color: var(--chat-muted); }

    #chat-ai-send {
      width: 42px;
      height: 42px;
      border-radius: 12px;
      background: linear-gradient(135deg, #1e3a8a, #3b82f6);
      border: none;
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
      box-shadow: 0 4px 14px rgba(59,130,246,0.4);
    }
    #chat-ai-send:hover:not(:disabled) {
      transform: scale(1.08);
      box-shadow: 0 6px 20px rgba(59,130,246,0.55);
    }
    #chat-ai-send:disabled { opacity: 0.45; cursor: not-allowed; }
    #chat-ai-send:active:not(:disabled) { transform: scale(0.94); }

    /* Error */
    .chat-error-msg {
      font-size: 12px;
      color: #ff6b8a;
      text-align: center;
      padding: 6px 12px;
      background: rgba(255,107,138,0.08);
      border: 1px solid rgba(255,107,138,0.2);
      border-radius: 8px;
      margin: 0 4px;
    }

    @media (max-width: 480px) {
      #chat-ai-window {
        width: calc(100vw - 24px);
        right: 12px;
        bottom: 96px;
        height: 70vh;
      }
      #chat-ai-fab { bottom: 20px; right: 20px; }
    }
  `;
    document.head.appendChild(style);

    // ─── Build HTML ────────────────────────────────────────────────────────────
    // FAB Button
    const fab = document.createElement("button");
    fab.id = "chat-ai-fab";
    fab.setAttribute("aria-label", "Mở AI Chat");
    fab.innerHTML = `
    <div class="chat-dot"></div>
    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M12 2l2.4 7.6L22 12l-7.6 2.4L12 22l-2.4-7.6L2 12l7.6-2.4z"/>
    </svg>
  `;

    // Chat Window
    const win = document.createElement("div");
    win.id = "chat-ai-window";
    win.setAttribute("role", "dialog");
    win.setAttribute("aria-label", "AI Chat");
    win.innerHTML = `
    <div class="chat-header">
      <div class="chat-header-avatar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
        </svg>
      </div>
      <div class="chat-header-info">
        <div class="chat-header-name">AI Assistant</div>
        <div class="chat-header-status">● Đang trực tuyến</div>
      </div>
      <button class="chat-close-btn" id="chat-ai-close" aria-label="Đóng">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <div class="chat-messages" id="chat-ai-messages">
      <div class="chat-welcome">
        <div class="chat-welcome-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
          </svg>
        </div>
        <h3>Xin chào! Tôi là AI Assistant</h3>
        <p>Hỏi tôi bất cứ điều gì,<br/>tôi sẵn sàng giúp đỡ bạn.</p>
      </div>
    </div>

    <div class="chat-input-area">
      <div class="chat-input-wrap">
        <textarea
          id="chat-ai-input"
          placeholder="Nhập câu hỏi của bạn..."
          rows="1"
          autocomplete="off"
        ></textarea>
      </div>
      <button id="chat-ai-send" disabled aria-label="Gửi">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
      </button>
    </div>
  `;

    document.body.appendChild(fab);
    document.body.appendChild(win);

    // ─── Logic ─────────────────────────────────────────────────────────────────
    const messagesEl = document.getElementById("chat-ai-messages");
    const inputEl    = document.getElementById("chat-ai-input");
    const sendBtn    = document.getElementById("chat-ai-send");
    const closeBtn   = document.getElementById("chat-ai-close");

    let isOpen    = false;
    let isLoading = false;

    // Toggle open/close
    function toggleChat() {
        isOpen = !isOpen;
        fab.classList.toggle("is-open", isOpen);
        win.classList.toggle("is-visible", isOpen);
        if (isOpen) setTimeout(() => inputEl.focus(), 350);
    }

    fab.addEventListener("click", toggleChat);
    closeBtn.addEventListener("click", toggleChat);

    // Close on Escape
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && isOpen) toggleChat();
    });

    // Auto-resize textarea
    inputEl.addEventListener("input", () => {
        inputEl.style.height = "auto";
        inputEl.style.height = Math.min(inputEl.scrollHeight, 100) + "px";
        sendBtn.disabled = !inputEl.value.trim() || isLoading;
    });

    // Send on Enter (Shift+Enter = newline)
    inputEl.addEventListener("keydown", (e) => {
        // Fix lỗi nhấn phím bộ gõ tiếng Việt bị nhảy 2 lần
        if (e.isComposing || e.keyCode === 229) {
            return;
        }

        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            if (!sendBtn.disabled) sendMessage();
        }
    });

    sendBtn.addEventListener("click", sendMessage);

    function appendMessage(role, text) {
        const msg = document.createElement("div");
        msg.className = `chat-msg ${role}`;
        const avatarText = role === "ai" ? "AI" : "Me";
        msg.innerHTML = `
      <div class="chat-msg-avatar">${avatarText}</div>
      <div class="chat-bubble">${formatMessage(text)}</div>
    `;
        messagesEl.appendChild(msg);
        scrollToBottom();
        return msg;
    }

    function showTyping() {
        const el = document.createElement("div");
        el.className = "chat-typing";
        el.id = "chat-typing-indicator";
        el.innerHTML = `
      <div class="chat-typing-avatar">AI</div>
      <div class="chat-typing-bubble">
        <span class="chat-typing-dot"></span>
        <span class="chat-typing-dot"></span>
        <span class="chat-typing-dot"></span>
      </div>
    `;
        messagesEl.appendChild(el);
        scrollToBottom();
    }

    function removeTyping() {
        const el = document.getElementById("chat-typing-indicator");
        if (el) el.remove();
    }

    function showError(msg) {
        const el = document.createElement("div");
        el.className = "chat-error-msg";
        el.textContent = msg;
        messagesEl.appendChild(el);
        scrollToBottom();
        setTimeout(() => el.remove(), 5000);
    }

    function scrollToBottom() {
        requestAnimationFrame(() => {
            messagesEl.scrollTop = messagesEl.scrollHeight;
        });
    }

    function formatMessage(text) {
        // 1. Escape HTML cơ bản để chống lỗi hiển thị và XSS
        let safeText = text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/\n/g, "<br>");

        // 2. Dùng Regex tự động tìm URL bắt đầu bằng http hoặc https và bọc thẻ <a>
        const urlRegex = /(https?:\/\/[^\s<]+)/g;
        return safeText.replace(urlRegex, function(url) {
            return `<a href="${url}" target="_blank" style="color: #3b82f6; text-decoration: underline; font-weight: 600;">Xem chi tiết tại đây</a>`;
        });
    }

    function generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }


    async function sendMessage() {
        const question = inputEl.value.trim();
        if (!question || isLoading) return;

        let sId = localStorage.getItem('smartstay_chat_session');
        if (!sId) {
            sId = generateUUID();
            localStorage.setItem('smartstay_chat_session', sId);
        }

        inputEl.value = "";
        inputEl.style.height = "auto";
        sendBtn.disabled = true;
        isLoading = true;

        appendMessage("user", question);
        showTyping();

        try {
            // TỰ ĐỘNG MÓC TOKEN TỪ LOCAL STORAGE (KHÔNG CẦN PHỤ THUỘC VÀO HTML)
            const myToken = localStorage.getItem('smartstay_token');

            const reqHeaders = {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            };

            // Nếu có token thì tự động gắn vào Header
            if (myToken) {
                reqHeaders['Authorization'] = `Bearer ${myToken}`;
            }

            const response = await fetch('/api/chat-ai/answer', {
                method: 'POST',
                headers: reqHeaders,
                credentials: 'include',
                body: JSON.stringify({
                    question: question,
                    conversationId: sId
                })
            });

            removeTyping();

            if (!response.ok) {
                console.error("Lỗi Server:", await response.text());
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();
            const answer = data.answer || data.message || JSON.stringify(data);
            appendMessage("ai", answer);

        } catch (err) {
            removeTyping();
            console.error("[ChatAI] Error:", err);
            showError("⚠️ Không thể kết nối đến AI. Vui lòng thử lại.");
        } finally {
            isLoading = false;
            sendBtn.disabled = !inputEl.value.trim();
        }
    }
})();