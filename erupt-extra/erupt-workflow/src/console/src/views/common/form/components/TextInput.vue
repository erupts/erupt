<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-input :placeholder="placeholder" disabled size="medium"/>
    </div>
    <div v-else>
      <el-input v-model="_value" :disabled="!editable" :placeholder="placeholder" clearable size="medium"/>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "TextInput",
  components: {},
  props: {
    value: {
      type: String,
      default: null
    },
    placeholder: {
      type: String,
      default: '请输入内容'
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
