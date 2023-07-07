<template>
  <el-tooltip :effect="isDark ? 'dark':'light'" :content="content" placement="top-start">
    <div>
      <slot></slot>
      <i class="el-icon-question" style="margin: 0 0px"></i>
    </div>
  </el-tooltip>
</template>

<script>
export default {
  install(Vue) {
    Vue.component('Tip', this)
  },
  name: "Tip",
  components: {},
  props:{
    isDark:{
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

<style scoped>

</style>
