<template>
  <div class="ai-chat-widget">
    <!-- 标题栏 -->
    <div class="chat-header" @click="toggleExpanded">
      <div class="header-left">
        <span class="ai-icon">🤖</span>
        <span class="ai-title">污水分析AI助手</span>
        <span class="status-indicator" :class="{ online: isOnline }"></span>
      </div>
      <div class="header-right">
        <span class="expand-icon">{{ isExpanded ? '−' : '+' }}</span>
      </div>
    </div>

    <!-- 展开后的聊天主区域 -->
    <div class="chat-body" v-if="isExpanded">
      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <!-- AI消息 -->
          <div v-if="msg.role === 'ai'" class="message-ai">
            <div class="avatar">AI</div>
            <div class="bubble">
              <div class="content">{{ msg.content }}</div>
              <div class="meta">{{ msg.time }}</div>
            </div>
          </div>
          <!-- 用户消息 -->
          <div v-else class="message-user">
            <div class="bubble">
              <div class="content">{{ msg.content }}</div>
              <div class="meta">{{ msg.time }}</div>
            </div>
            <div class="avatar">您</div>
          </div>
        </div>
        <!-- 思考状态指示器 -->
        <div v-if="isThinking" class="thinking-indicator">
          <div class="avatar">AI</div>
          <div class="bubble">
            <div class="thinking-text">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
              正在思考中...
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="quick-questions">
          <button
            v-for="(q, idx) in quickQuestions"
            :key="idx"
            class="quick-btn"
            @click="sendQuickQuestion(q)"
            :disabled="isThinking"
          >
            {{ q }}
          </button>
        </div>
        <div class="input-wrapper">
          <input
            v-model="userInput"
            type="text"
            placeholder="输入您的问题...（例如：当前水质如何？）"
            @keyup.enter="sendMessage"
            :disabled="isThinking"
            class="chat-input"
          />
          <button @click="sendMessage" :disabled="!userInput.trim() || isThinking" class="send-btn">
            <span v-if="!isThinking">发送</span>
            <span v-else class="sending">...</span>
          </button>
        </div>
        <div class="hint">
          💡 提示：可询问污染类型、处理建议、检测标准等专业问题
        </div>
      </div>
    </div>

    <!-- 折叠时的预览 -->
    <div v-else class="chat-preview">
      <div class="preview-text">
        <span v-if="lastMessage">
          {{ lastMessage.role === 'user' ? '您：' : 'AI：' }}{{ lastMessage.preview }}
        </span>
        <span v-else>点击展开与AI助手对话</span>
      </div>
      <div class="preview-unread" v-if="hasNewMessage && !isExpanded">●</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiChatWidget',
  data() {
    return {
      isExpanded: true, // 默认展开
      isOnline: true,
      isThinking: false,
      userInput: '',
      messages: [
        {
          role: 'ai',
          content: '您好！我是污水监控系统的AI助手。我可以帮您分析检测结果、解答处理工艺问题、评估污染风险等。请随时提问！',
          time: this.getCurrentTime(),
          preview: '您好！我是污水监控系统的AI助手...'
        }
      ],
      quickQuestions: [
        "当前水质等级？",
        "塑料瓶如何处理？",
        "化学污染应急措施",
        "生成检测报告模版"
      ],
      hasNewMessage: false
    };
  },
  computed: {
    lastMessage() {
      return this.messages.length > 0 ? this.messages[this.messages.length - 1] : null;
    }
  },
  methods: {
    toggleExpanded() {
      this.isExpanded = !this.isExpanded;
      if (this.isExpanded) {
        this.hasNewMessage = false;
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    },
    async sendMessage() {
      const question = this.userInput.trim();
      if (!question || this.isThinking) return;

      // 添加用户消息
      this.addMessage('user', question);
      this.userInput = '';
      this.isThinking = true;
      this.hasNewMessage = true;

      try {
        // 尝试调用真实后端API
        const aiResponse = await this.callDeepSeekAPI(question);
        this.addMessage('ai', aiResponse);
      } catch (error) {
        console.error('调用AI接口失败，使用模拟响应:', error);
        // 如果后端失败，使用模拟响应
        setTimeout(() => {
          const mockResponse = this.getMockResponse(question);
          this.addMessage('ai', mockResponse);
          this.isThinking = false;
        }, 800); // 模拟网络延迟
      }
    },
    async callDeepSeekAPI(question) {
      // 注意：这里假设您的后端已修复并运行在 http://localhost:8080
      // 如果后端未运行，此调用会失败，将转到 catch 块使用模拟数据
      const response = await fetch('http://localhost:8080/api/ai/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          question: question,
          userId: 'sewage_monitor_user'
        })
      });

      if (!response.ok) {
        throw new Error(`API请求失败: ${response.status}`);
      }

      const data = await response.json();
      this.isThinking = false;
      // 根据您的后端实际返回结构调整
      return data.answer || data.response || '已收到您的查询。';
    },
    sendQuickQuestion(question) {
      this.userInput = question;
      this.sendMessage();
    },
    addMessage(role, content) {
      const now = this.getCurrentTime();
      const preview = content.length > 20 ? content.substring(0, 20) + '...' : content;
      this.messages.push({
        role,
        content,
        time: now,
        preview
      });
      this.scrollToBottom();
    },
    getMockResponse(question) {
      // 根据问题关键词返回模拟的AI回答
      const lowerQuestion = question.toLowerCase();
      const responses = {
        '水质': '根据最新传感器数据，当前水质综合指数为72（中等）。主要超标参数为COD（化学需氧量），建议加强前处理。',
        '塑料': '识别到PET塑料瓶污染物。建议步骤：1. 机械打捞 2. 分类回收 3. 检查上游排放源。回收率可达85%以上。',
        '化学': '针对化学污染物泄露应急流程：1. 立即隔离污染区域 2. 启动中和剂投放系统 3. 上报环保部门 4. 持续监测pH值变化。',
        '报告': '【污水检测报告模版】\n一、概况\n二、检测指标（COD/BOD/氨氮/总磷）\n三、污染物识别\n四、风险等级评估\n五、处理建议\n需要我填充具体数据吗？',
        '标准': '国家《污水综合排放标准》（GB 8978-1996）主要限值：\n- COD：< 100 mg/L\n- BOD5：< 30 mg/L\n- 氨氮：< 15 mg/L\n- 总磷：< 0.5 mg/L',
        '风险': '当前系统评估风险等级：黄色（中等关注）。发现3类污染物，建议24小时内处理，并提交中期评估报告。',
        '处理': '常规处理工艺推荐：格栅 → 沉砂池 → 初沉池 → 生物反应池 → 二沉池 → 消毒池。针对您的情况，重点加强生物处理阶段。'
      };

      for (const [key, response] of Object.entries(responses)) {
        if (lowerQuestion.includes(key.toLowerCase())) {
          return response;
        }
      }

      // 默认回复
      return `已收到您关于"${question}"的查询。作为污水监控AI，我主要擅长：污染识别、处理工艺、风险评估、标准解读。您的问题已记录，如需更精准回答，请提供更多背景信息。`;
    },
    getCurrentTime() {
      const now = new Date();
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    }
  },
  mounted() {
    // 组件加载后自动滚动到底部
    this.scrollToBottom();
  }
};
</script>

<style scoped>
.ai-chat-widget {
  font-family: 'Microsoft YaHei', 'Segoe UI', sans-serif;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  background: white;
  max-width: 380px;
  margin: 20px;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  user-select: none;
  transition: background 0.3s;
}

.chat-header:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-icon {
  font-size: 1.5em;
}

.ai-title {
  font-weight: bold;
  font-size: 1.1em;
}

.status-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #ccc;
}

.status-indicator.online {
  background-color: #4caf50;
  box-shadow: 0 0 5px #4caf50;
}

.expand-icon {
  font-weight: bold;
  font-size: 1.2em;
}

/* 折叠预览样式 */
.chat-preview {
  padding: 14px 20px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.preview-text {
  color: #666;
  font-size: 0.9em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
}

.preview-unread {
  color: #ff4757;
  font-size: 1.5em;
  margin-left: 10px;
}

/* 展开后的主区域 */
.chat-body {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.messages-container {
  flex-grow: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息通用样式 */
.message {
  max-width: 85%;
}

.message-ai, .message-user {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9em;
  color: white;
}

.message-ai .avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.message-user .avatar {
  background: #5a67d8;
}

.bubble {
  border-radius: 18px;
  padding: 12px 16px;
  position: relative;
  max-width: 100%;
  word-wrap: break-word;
}

.message-ai .bubble {
  background: white;
  border: 1px solid #e2e8f0;
  border-top-left-radius: 4px;
}

.message-user .bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-top-right-radius: 4px;
}

.content {
  font-size: 0.95em;
  line-height: 1.4;
}

.meta {
  font-size: 0.75em;
  color: #a0aec0;
  margin-top: 5px;
  text-align: right;
}

.message-user .meta {
  color: rgba(255, 255, 255, 0.8);
}

/* 思考状态 */
.thinking-indicator {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  opacity: 0.7;
}

.thinking-text {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #718096;
  font-style: italic;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #667eea;
  animation: pulse 1.5s infinite ease-in-out;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* 输入区域 */
.input-area {
  border-top: 1px solid #e2e8f0;
  padding: 16px;
  background: white;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.quick-btn {
  background: #edf2f7;
  border: 1px solid #cbd5e0;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 0.8em;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
}

.quick-btn:hover:not(:disabled) {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.quick-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.chat-input {
  flex-grow: 1;
  border: 1px solid #cbd5e0;
  border-radius: 24px;
  padding: 12px 18px;
  font-size: 0.95em;
  outline: none;
  transition: border-color 0.3s;
}

.chat-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.chat-input:disabled {
  background: #f7fafc;
  cursor: not-allowed;
}

.send-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 24px;
  padding: 12px 26px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  outline: none;
  min-width: 80px;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3);
}

.send-btn:disabled {
  background: #cbd5e0;
  cursor: not-allowed;
}

.sending {
  letter-spacing: 2px;
}

.hint {
  font-size: 0.8em;
  color: #a0aec0;
  text-align: center;
  margin-top: 8px;
}

/* 滚动条美化 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
}
</style>
