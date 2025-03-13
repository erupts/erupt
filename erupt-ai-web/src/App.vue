<script setup lang="ts">
import {h, ref} from "vue";
import {Bubble, Sender} from "ant-design-x-vue";
import {message} from "ant-design-vue";
import 'highlight.js/styles/monokai.css';
import md from "./components/markdown.ts";
import {ChatApi, type ChatMessage} from "./api/chat.api.ts";

let content = ref<string>("")
let windowHeight = ref<number>(window.innerHeight);
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

window.onresize = () => {
  windowHeight.value = window.innerHeight;
}

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
  <div style="padding:8px;height:100%;box-sizing: border-box" :style="{height: windowHeight+'px'}">
    <div style="display: flex;height:100%">
      <div style="width: 100%;display: flex;flex-direction: column;height:100%">
        <article ref="bubbles" style="padding: 8px 0;flex: 1 1 0;overflow: auto;height: calc(100% - 66px)">
          <Bubble v-if="messages.length" v-for="item in messages" style="margin-bottom: 8px" :key="item.id"
                  :placement="item.senderType == 'MODEL' ? 'start' : 'end'"
                  :content="item.content" :message-render="messageRender"
                  :loading="item.loading"/>
        </article>
        <Suggestion :loading='true'>
          <Sender
              :on-submit="send"
              :loading="sending"
              :disabled="sendDisabled"
              v-model:value="content"
              :allowSpeech="{
                recording: recording.value,
                onRecordingChange: (nextRecording) => {
                  message.info(`Mock Customize Recording: ${nextRecording}`);
                  // recording.value = nextRecording;
                },
              }"
          >
          </Sender>
        </Suggestion>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@primary-color: #4285F4; // 主色调
@secondary-color: #f0f0f0; // 辅助色调
@hover-color: #f9f9f9; // 鼠标悬停背景色
@border-color: #ddd; // 边框颜色
@font-stack: Arial, sans-serif; // 字体堆叠
@base-padding: 15px; // 基础内边距
@base-margin: 10px; // 基础外边距

::v-deep(.ant-bubble) {
  font-family: @font-stack;
  color: @primary-color;
  max-width: 800px;
  margin: 0 auto;
  padding: @base-padding;
  line-height: 1.6;

  // 标题
  h1, h2, h3, h4, h5, h6 {
    margin: @base-margin 0;
    font-weight: bold;
    line-height: 1.2;

    &:hover {
      cursor: default;
    }
  }

  h1 {
    font-size: 2.5em;
  }

  h2 {
    font-size: 2em;
  }

  h3 {
    font-size: 1.75em;
  }

  h4 {
    font-size: 1.5em;
  }

  h5 {
    font-size: 1.25em;
  }

  h6 {
    font-size: 1em;
  }

  // 段落
  p {
    margin: 0;
    font-size: 1em;
  }

  // 链接
  a {
    color: @primary-color;
    text-decoration: none;
    transition: color 0.3s ease;

    &:hover, &:focus {
      color: darken(@primary-color, 20%);
      text-decoration: underline;
    }
  }

  // 列表
  ul, ol {
    margin: @base-margin 0;
    padding-left: 20px;

    li {
      margin-bottom: @base-margin / 2;
    }
  }

  // 图片
  img {
    max-width: 100%;
    height: auto;
    border-radius: 5px;
    margin: @base-margin 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  // 表格
  table {
    width: 100%;
    border-collapse: collapse;
    margin: @base-margin 0;
    font-size: 14px;
    color: #333;

    th, td {
      padding: @base-padding;
      border: 1px solid @border-color;
      text-align: left;
    }

    th {
      background-color: @secondary-color;
      font-weight: bold;
    }

    tbody {
      tr:nth-child(even) {
        background-color: @hover-color;
      }

      tr:hover {
        background-color: @hover-color;
      }
    }
  }

  // 代码块
  code {
    background-color: @secondary-color;
    padding: 2px 4px;
    border-radius: 3px;
    font-family: "Courier New", Courier, monospace;
  }

  pre {
    background-color: @hover-color;
    border: 1px solid @border-color;
    padding: @base-padding;
    overflow-x: auto;
    border-radius: 5px;
    font-family: "Courier New", Courier, monospace;
    margin: @base-margin 0;
  }

  // 引用
  blockquote {
    border-left: 4px solid @secondary-color;
    padding: @base-padding / 2 @base-padding;
    margin: @base-margin 0;
    font-style: italic;
    color: #666;
  }

  // 水平分割线
  hr {
    border: 0;
    height: 1px;
    background: #ddd;
    margin: @base-margin * 2 0;
  }
}

</style>
