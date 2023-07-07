<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-input size="medium" disabled :placeholder="placeholder" type="number"/>
    </div>
    <div v-else>
      <el-input v-model="_nvalue" size="medium" :disabled="!editable" clearable :placeholder="placeholder" type="number"/>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "NumberInput",
  components: {},
  props:{
    value:{
      type: String,
      default: null
    },
    placeholder:{
      type: String,
      default: '请输入数值'
    },
  },
  computed: {
    _nvalue: {
      get() {
        return Number(this.value);
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
