<template>
  <div :class="{'line': row === 1, 'lines': row > 1}"
       :title="hoverTip ? content: null"
       :style="{'--row':row}">
    <slot name="pre"></slot>
    {{content}}
  </div>
</template>

<script>
//超出指定行数自动隐藏文字
export default {
  name: "Ellipsis",
  install(Vue){
    Vue.component('ellipsis', this)
  },
  components: {},
  props:{
    row: {
      type: Number,
      default: 1
    },
    hoverTip:{
      type: Boolean,
      default: false
    },
    content:{
      type: String,
      default: ''
    }
  },
  data() {
    return {}
  },
  methods: {}
}
</script>

<style lang="less" scoped>

.line{
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.lines{
  display: -webkit-box;
  word-break: break-all;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: var(--row);
  -webkit-box-orient: vertical;
}
</style>
