<script setup lang="ts">
import {computed, h, ref} from "vue";
import {Bubble, Sender} from "ant-design-x-vue";
import {message} from "ant-design-vue";
import 'highlight.js/styles/monokai.css';
import md from "./components/markdown.ts";

let content = ref<string>("")
let windowHeight = ref<number>(window.innerHeight);
let recording = ref<boolean>(false);
let res = ref<string>("")

window.onresize = () => {
  windowHeight.value = window.innerHeight;
}

const suggestions = [
  {label: 'Write a report', value: 'report'},
  {label: 'Draw a picture', value: 'draw'},
  {
    label: 'Check some knowledge',
    value: 'knowledge',
    children: [
      {
        label: 'About React',
        value: 'react',
      },
      {
        label: 'About Ant Design',
        value: 'antd',
      },
    ],
  },
];
const messageRender = (content: string) => {
  return h('div', {innerHTML: md.render(content)});
};


const accumulatedMarkdown = ref(''); // 用于累积接收到的 Markdown 数据
const renderedMarkdown = ref(''); // 用于存储渲染后的 HTML

const send = (message: string) => {
  content.value = "";
  const eventSource = new EventSource(`/erupt-api/ai/chat/send?chatId=${1}&message=${message}`); // 替换为你的 SSE 服务器地址

  // 监听消息事件
  eventSource.onmessage = function (event) {
    accumulatedMarkdown.value += JSON.parse(event.data).text;
    setTimeout(() => {
      renderedMarkdown.value = md.render(accumulatedMarkdown.value);
    }, 100)
  };

  // 监听错误事件
  eventSource.onerror = function (event) {
    console.error('发生错误:', event);
    // 可以在这里处理错误，例如重新连接
  };

  // 监听打开事件
  eventSource.onopen = function (event) {
    console.log('SSE 连接已打开');
    // 可以在这里执行一些初始化操作
  };

  // 可选：监听特定的自定义事件
  eventSource.addEventListener('customEvent', function (event) {
    console.log('收到自定义事件:', event.data);
    // 处理自定义事件
  });
}

const markdown = computed(() => {
  return md.render(content.value);
});


</script>

<template>
  <div style="padding:8px;height:100%;box-sizing: border-box" :style="{height: windowHeight+'px'}">
    <div style="display: flex;height:100%">
      <!--      <div style="width: 20%">-->
      <!--        <Conversations :items="conversations"/>-->
      <!--      </div>-->
      <div style="width: 100%;display: flex;flex-direction: column;height:100%">
        <article style="padding: 8px 0;flex: 1 1 0;overflow: auto;height: calc(100% - 66px)">
          <Bubble v-for="(item, index) in 20" :key="index" :placement="index % 2 === 0 ? 'start' : 'end'"
                  :content="'###  Good morning, how are you? - 123 -23'+index" style="margin-bottom: 8px"
                  :message-render="messageRender"
          />
          <Bubble :placement="'start'" :content="renderedMarkdown" :message-render="messageRender"
                  style="margin-bottom: 8px"
          />
        </article>
        <Suggestion :items="suggestions">
          <!--              :loading='true'-->
          <Sender
              :on-submit="send"
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

<style scoped>

</style>
