<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-input size="medium" disabled :placeholder="placeholder"/>
    </div>
    <div v-else>
      <el-input size="medium" :disabled="formDisable" clearable v-model="_value" :placeholder="placeholder"/>
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
