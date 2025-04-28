<script setup lang="ts">
import {h, onMounted, ref} from "vue";
import {Bubble, Conversations, Sender} from "ant-design-x-vue";
import 'highlight.js/styles/monokai.css';
import md from "./components/markdown.ts";
import {type Agent, type Chat, ChatApi, type ChatMessage, type UserInfo} from "./api/chat.api.ts";
import {DeleteOutlined, PlusOutlined, RobotOutlined, UserOutlined} from '@ant-design/icons-vue';
import {getToken} from "./api/axios.config.ts";

const userInfo = ref<UserInfo>()
const chats = ref<Chat[]>([])
const agents = ref<Agent[]>([]);
const messages = ref<ChatMessage[]>([])
const selectChat = ref<number | null>();
const selectAgent = ref<Agent | null>();

const agentModel = ref<boolean>(false)

const bubbles = ref<HTMLDivElement>();
const content = ref<string>("")
const sending = ref<boolean>(false)
const sendDisabled = ref<boolean>(false)

const messagePage = ref<number>(1); // 消息的当前页码
const loadingMoreMessages = ref<boolean>(false); // 是否正在加载更多消息
const hasMoreMessages = ref<boolean>(true); // 是否还有更多消息

const params = new URLSearchParams(new URL(window.location.href).search);

ChatApi.userInfo().then(res => {
  userInfo.value = res;
})

const createConversation = () => {
  messages.value = [];
  selectChat.value = null;
  sending.value = false;
  sendDisabled.value = false;
}

const fetchChats = (after?: () => void) => {
  ChatApi.chats().then(res => {
    chats.value = res.data;
    if (res.data.length) {
      selectChat.value = res.data[0].id;
      onSelectChat(String(res.data[0].id), after)
    } else {
      clearStatus();
    }
  })
}


onMounted(() => {
  fetchChats();
  ChatApi.agents().then(it => {
    agents.value = it.data;
  })
})

const clearStatus = () => {
  selectChat.value = null;
  sending.value = false;
  sendDisabled.value = false;
  messagePage.value = 1; // 重置消息页码
  hasMoreMessages.value = true; // 重置是否有更多消息
  accumulatedMarkdown.value = ""; // 清空累积的 Markdown 数据
  messages.value = []; // 清空消息列表
}

const onSelectChat = (chatId: string, after?: () => void) => {
  clearStatus();
  selectChat.value = Number(chatId);
  fetchMessages(Number(chatId), true, after); // 加载第一页消息
};

const fetchMessages = (chatId: number, toBottom: boolean, after?: () => void) => {
  loadingMoreMessages.value = true; // 标记正在加载更多消息
  ChatApi.messages(chatId, 20, messagePage.value).then(res => {
    if (messagePage.value === 1) {
      // 如果是第一页，直接赋值
      messages.value = res.data.slice().reverse();
    } else {
      // 如果不是第一页，将新加载的消息追加到现有消息列表的前面
      messages.value = res.data.slice().reverse().concat(messages.value);
    }
    hasMoreMessages.value = res.data.length === 20; // 判断是否还有更多消息
    after && after();
    if (toBottom) {
      setTimeout(() => {
        //@ts-ignore
        bubbles.value.scrollTop = bubbles.value.scrollHeight; // 滚动到最新消息
      }, 100);
    }
  }).finally(() => {
    loadingMoreMessages.value = false; // 加载完成
  });
};

const accumulatedMarkdown = ref(''); // 用于累积接收到的 Markdown 数据

const send = (message: string) => {
  function start(chatId: number) {
    sending.value = true;
    content.value = "";
    messages.value.push({
      id: Math.random(),
      senderType: "USER",
      content: message,
      createTime: "",
      loading: false,
    } as ChatMessage)
    messages.value.push({
      id: Math.random(),
      senderType: "MODEL",
      content: "",
      createTime: "",
      loading: true,
    } as ChatMessage)
    setTimeout(() => {
      //@ts-ignore
      bubbles.value.scrollTop = bubbles.value.scrollHeight;
    }, 10)
    const eventSource = new EventSource(`/erupt-api/ai/chat/send?chatId=${chatId}&message=${message}&_token=${getToken()}&agentId=${selectAgent.value?.id || ''}&llmId=${params.get("llm")||''}`);

    eventSource.onmessage = (event) => {
      sending.value = false;
      sendDisabled.value = true;
      console.log(JSON.parse(event.data).text)
      accumulatedMarkdown.value += JSON.parse(event.data).text;
      let msg = messages.value[messages.value.length - 1];
      if (msg.loading) {
        msg.loading = false;
      }
      setTimeout(() => {
        msg.content = md.render(accumulatedMarkdown.value);
        messageToBottom();
        setTimeout(() => {
          messageToBottom()
        }, 50)
      }, 10);
    };

    eventSource.onerror = () => {
      setTimeout(() => {
        accumulatedMarkdown.value = "";
        sendDisabled.value = false;
        sending.value = false;
      }, 100)
      eventSource.close();
    };

    eventSource.onopen = () => {
      sendDisabled.value = true;
    };
  }

  if (null == selectChat.value) {
    ChatApi.createChat(message).then(res => {
      fetchChats(() => {
        start(res.data)
      });
    });
  } else {
    //@ts-ignore
    start(selectChat.value)
  }
}

const messageRender = (content: string): any => {
  if (!content) {
    return null;
  }
  return h('div', {innerHTML: md.render(content)});
};

const messageToBottom = () => {
  //@ts-ignore
  if (bubbles.value.scrollHeight - bubbles.value.scrollTop - bubbles.value.clientHeight < bubbles.value.clientHeight / 3) {
    //@ts-ignore
    bubbles.value.scrollTop = bubbles.value.scrollHeight;
  }
}

const conversationMenu = (conversation) => {
  return {
    items: [{
      label: '删除',
      icon: h(DeleteOutlined),
      key: 'delete',
      danger: true
    }
    ],
    onClick: (item) => {
      if (item.key == 'delete') {
        ChatApi.deleteChat(conversation.key as number).then(() => {
          fetchChats();
        })
      }
    }
  }
}

const handleBubbleScroll = () => {
  if (bubbles.value) {
    //@ts-ignore
    if (bubbles.value.scrollTop <= 10 &&
        !loadingMoreMessages.value &&
        hasMoreMessages.value &&
        selectChat.value) {
      messagePage.value += 1;
      //@ts-ignore
      fetchMessages(selectChat.value, false); // 加载更多消息
    }
  }
};

const onSelectAgent = (agent: Agent) => {
  selectAgent.value = agent;
  agentModel.value = false;
  content.value = content.value.substring(0, content.value.length - 1)
}

</script>

<template>
  <div style="height:100%;box-sizing: border-box;display: flex;">
    <div style="width: 200px;flex-shrink: 0;border-right:1px solid #f0f0f0;overflow: auto">
      <div style="padding: 8px">
        <a-button type="primary" style="width: 100%;" @click="createConversation()">
          <PlusOutlined/>
          新建会话
        </a-button>
      </div>
      <Conversations :items="chats.map(it => ({ label: it.title, key: String(it.id) }))"
                     :menu="conversationMenu"
                     :activeKey="String(selectChat)" @activeChange="onSelectChat" style="margin:0;padding: 8px"/>
    </div>
    <div style="flex: 1;display: flex;flex-direction: column;height:100%;padding: 0 8px 8px;box-sizing: border-box">
      <article ref="bubbles" style="padding: 8px 0;flex: 1 1 0;overflow: auto;height: calc(100% - 66px)"
               @scroll="handleBubbleScroll">
        <Bubble v-if="messages.length" v-for="item in messages" style="margin-bottom: 8px" :key="item.id"
                :placement="item.senderType == 'MODEL' ? 'start' : 'end'"
                :content="item.content" :message-render="messageRender"
                :avatar="{
                  icon: item.senderType == 'MODEL'?h(RobotOutlined as any) : (userInfo ? userInfo.nickname.substring(0,1) : h(UserOutlined as any)),
                  style: { background: item.senderType == 'MODEL'?'#87d068':'#ccc' }
                } as any"
                :loading="item.loading"/>
      </article>
      <Suggestion>
        <Sender
            :on-submit="send"
            :loading="sending"
            :placeholder="'输入@召唤智能体'"
            :disabled="sendDisabled"
            @change="(nextVal) => {
                if (nextVal === '@') {
                  agentModel = true;
                }
              }"
            v-model:value="content"
        >
          <template #header>
            <div v-if="selectAgent"
                 style="display: flex;align-items: center;background: #f6f7f9;padding: 5px 5px 5px 15px;border-radius: 12px 12px 0 0;color: #838a95">
              <div>与 <span style="color: #000;font-weight: 400">{{ selectAgent.name }}</span> 对话</div>
              <a-button type="text" style="margin-left: auto;" @click="selectAgent = null">&times;</a-button>
            </div>
          </template>
        </Sender>
      </Suggestion>
      <a-modal v-model:open="agentModel" title="选择智能体" width="260px" :footer="null" style="top:50px"
               :body-style="{padding:0}">
        <div class="agent">
          <p class="item" v-for="agent in agents" @click="onSelectAgent(agent)">{{ agent.name }}</p>
        </div>
      </a-modal>
    </div>
  </div>
</template>

<style scoped lang="less">
@import './app.less';

.agent {
  max-height: 500px;
  overflow-y: auto;
  margin-top: 22px;

  .item {
    border-bottom: 1px solid #f0f0f0;
    padding: 12px 6px;
    cursor: pointer;
    margin: 0;
    transition: background 0.5s;

    &:hover {
      background: #f0f0f0;
    }
  }
}
</style>
