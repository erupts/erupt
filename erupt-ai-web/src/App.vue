<script setup lang="ts">
import {h, ref} from "vue";
import {Bubble, Conversations, type ConversationsProps, Sender} from "ant-design-x-vue";
import 'highlight.js/styles/monokai.css';
import md from "./components/markdown.ts";
import {type Chat, ChatApi, type ChatMessage, type UserInfo} from "./api/chat.api.ts";
import {DeleteOutlined, PlusOutlined, RobotOutlined, UserOutlined} from '@ant-design/icons-vue';
import {getToken} from "./api/axios.config.ts";

let userInfo = ref<UserInfo>()
let chats = ref<Chat[]>([])
let messages = ref<ChatMessage[]>([])
let selectChat = ref<number>();

let bubbles = ref(null);
let content = ref<string>()
let sending = ref<boolean>(false)
let sendDisabled = ref<boolean>(false)


ChatApi.userInfo().then(res => {
  userInfo.value = res;
})

const createConversation = () => {
  messages.value = [];
  selectChat.value = null;
}

const fetchChats = (after?: () => void) => {
  ChatApi.chats().then(res => {
    chats.value = res.data;
    if (res.data.length) {
      selectChat.value = res.data[0].id;
      onSelectChat(res.data[0].id, after)
    }
  })
}

fetchChats();

const onSelectChat = (chatId: number, after?: () => void) => {
  selectChat.value = chatId;
  ChatApi.messages(selectChat.value, 20).then(res => {
    messages.value = res.data.slice().reverse();
    after && after();
    setTimeout(() => {
      bubbles.value.scrollTop = bubbles.value.scrollHeight;
    }, 100)
  })
}

const accumulatedMarkdown = ref(''); // 用于累积接收到的 Markdown 数据

const send = (message: string) => {
  function start(chatId: number) {
    sending.value = true;
    content.value = "";
    messages.value.push({
      id: Math.random(),
      content: message,
      senderType: "USER",
      createTime: new Date()
    })
    messages.value.push({
      id: Math.random(),
      content: "",
      senderType: "MODEL",
      createTime: new Date(),
      loading: true
    })
    setTimeout(() => {
      bubbles.value.scrollTop = bubbles.value.scrollHeight;
    }, 10)
    const eventSource = new EventSource(`/erupt-api/ai/chat/send?chatId=${chatId}&message=${message}&_token=${getToken()}`);

    eventSource.onmessage = function (event) {
      sending.value = false;
      sendDisabled.value = true;
      accumulatedMarkdown.value += JSON.parse(event.data).text;
      let msg = messages.value[messages.value.length - 1];
      if (msg.loading) {
        msg.loading = false;
      }
      msg.content = md.render(accumulatedMarkdown.value);
      messageToBottom();
    };

    eventSource.onerror = function (event) {
      accumulatedMarkdown.value = "";
      sendDisabled.value = false;
      sending.value = false;
      eventSource.close();
    };

    eventSource.onopen = function (event) {
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
    start(selectChat.value)
  }
}

const messageRender = (content: string) => {
  if (!content) {
    return null;
  }
  return h('div', {innerHTML: md.render(content)});
};

const messageToBottom = () => {
  if (bubbles.value.scrollHeight - bubbles.value.scrollTop - bubbles.value.clientHeight < bubbles.value.clientHeight / 3) {
    bubbles.value.scrollTop = bubbles.value.scrollHeight;
  }
}

const conversationMenu: ConversationsProps['menu'] = (conversation) => {
  return {
    items: [{
      label: '删除',
      icon: h(DeleteOutlined),
      key: 'delete',
      danger: true
    }
    ],
    onClick: (item) => {
      if (item.key=='delete'){
        ChatApi.deleteChat(conversation.key as number).then(() => {
          fetchChats();
        })
      }
    }
  }
}

</script>

<template>
  <div style="height:100%;box-sizing: border-box;display: flex;">
    <div style="width: 200px;border-right:1px solid #f0f0f0;overflow: auto">
      <div style="padding: 8px">
        <a-button type="primary" style="width: 100%;" @click="createConversation()">
          <PlusOutlined/>
          新建会话
        </a-button>
      </div>
      <Conversations :items="chats.map(it => ({ label: it.title, key: it.id }))"
                     :menu="conversationMenu"
                     :activeKey="selectChat" @activeChange="onSelectChat" style="margin:0;padding: 8px"/>
    </div>
    <div style="flex: 1;display: flex;flex-direction: column;height:100%;padding: 0 8px 8px;box-sizing: border-box">
      <article ref="bubbles" style="padding: 8px 0;flex: 1 1 0;overflow: auto;height: calc(100% - 66px)">
        <Bubble v-if="messages.length" v-for="item in messages" style="margin-bottom: 8px" :key="item.id"
                :placement="item.senderType == 'MODEL' ? 'start' : 'end'"
                :content="item.content" :message-render="messageRender"
                :avatar="{
                  icon: item.senderType == 'MODEL'?h(RobotOutlined) : (userInfo ? userInfo.nickname.substring(0,1) : h(UserOutlined)),
                  style: { background: item.senderType == 'MODEL'?'#87d068':'#ccc' }
                }"
                :loading="item.loading"/>
      </article>
      <Suggestion :loading='true'>
        <Sender
            :on-submit="send"
            :loading="sending"
            :disabled="sendDisabled"
            v-model:value="content"
        >
<!--          <template #header>-->
<!--            <div style="background: #f0f0f0;padding: 8px;border-radius: 12px 12px 0 0">-->
<!--              123-->
<!--            </div>-->
<!--          </template>-->
        </Sender>
      </Suggestion>
    </div>
  </div>
</template>

<style scoped lang="less">
@import './app.less';
</style>
