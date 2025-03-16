<script setup lang="ts">
import {h, ref} from "vue";
import {Bubble, Conversations, Sender} from "ant-design-x-vue";
import 'highlight.js/styles/monokai.css';
import md from "./components/markdown.ts";
import {ChatApi, type ChatMessage} from "./api/chat.api.ts";
import {DeleteOutlined, RobotOutlined, UserOutlined} from '@ant-design/icons-vue';

let content = ref<string>("")
let recording = ref<boolean>(false);
let res = ref<string>("")
let messages = ref<ChatMessage[]>([])
let sending = ref<boolean>(false)
let sendDisabled = ref<boolean>(false)
let bubbles = ref(null);

ChatApi.messages(1, 10).then(res => {
  messages.value = res.data.data.slice().reverse();
  setTimeout(() => {
    bubbles.value.scrollTop = bubbles.value.scrollHeight;
  }, 100)
})


const accumulatedMarkdown = ref(''); // 用于累积接收到的 Markdown 数据

const send = (message: string) => {
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
  const eventSource = new EventSource(`/erupt-api/ai/chat/send?chatId=${1}&message=${message}`);

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

const messageRender = (content: string) => {
  return h('div', {innerHTML: md.render(content)});
};

const messageToBottom = () => {
  if (bubbles.value.scrollHeight - bubbles.value.scrollTop - bubbles.value.clientHeight < bubbles.value.clientHeight / 3) {
    bubbles.value.scrollTop = bubbles.value.scrollHeight;
  }
}

</script>

<template>
  <div style="height:100%;box-sizing: border-box;display: flex;">
    <div style="width: 200px;border-right:1px solid #f0f0f0">
      <Conversations :items="[{key:1,label:'xxx'},{key:12,label:'xxx2'}]"
                     :menu="{
                        items:[{
                          label: '删除',
                          key: 'delete',
                          danger: true,
                          icon: h(DeleteOutlined),
                        }]
                     }"
                     :defaultActiveKey="1" style="margin:0;padding: 8px"/>
    </div>
    <div style="flex: 1;display: flex;flex-direction: column;height:100%;padding: 0 8px 8px;box-sizing: border-box">
      <article ref="bubbles" style="padding: 8px 0;flex: 1 1 0;overflow: auto;height: calc(100% - 66px)">
        <Bubble v-if="messages.length" v-for="item in messages" style="margin-bottom: 8px" :key="item.id"
                :placement="item.senderType == 'MODEL' ? 'start' : 'end'"
                :content="item.content" :message-render="messageRender"
                :avatar="{ icon: h(item.senderType == 'MODEL'?RobotOutlined:UserOutlined),style: { background: '#87d068' } }"
                :loading="item.loading"/>
      </article>
      <Suggestion :loading='true'>
        <Sender
            :on-submit="send"
            :loading="sending"
            :disabled="sendDisabled"
            v-model:value="content"
        >
        </Sender>
      </Suggestion>
    </div>
  </div>
</template>

<style scoped lang="less">
@import './app.less';
</style>
