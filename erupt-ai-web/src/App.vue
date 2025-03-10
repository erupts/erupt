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
                  :content="'Good morning, how are you? - 123 -23'+index" style="margin-bottom: 8px"
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
    color: darken(@primary-color, 10%);
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
    margin: @base-margin 0;
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

  // 按钮
  button, input[type="button"], input[type="submit"], input[type="reset"] {
    display: inline-block;
    padding: @base-padding / 2 @base-padding;
    background-color: @primary-color;
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1em;
    transition: background-color 0.3s ease;

    &:hover, &:focus {
      background-color: darken(@primary-color, 10%);
    }
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

  // 表单
  form {
    margin: @base-margin 0;

    label {
      display: block;
      margin-bottom: @base-margin / 2;
      font-weight: bold;
    }
  }
}

</style>
