<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-input size="medium" disabled :placeholder="placeholder" show-word-limit :rows="2" type="textarea"/>
    </div>
    <div v-else>
      <el-input size="medium" v-model="_value" :disabled="!editable" clearable :maxlength="255" :placeholder="placeholder" show-word-limit :rows="3" type="textarea"/>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "TextareaInput",
  components: {},
  props:{
    value: {
      type: String,
      default: null
    },
    placeholder:{
      type: String,
      default: '请输入内容'
    }
  },
  computed: {
    _value: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit("input", val);
      }
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
