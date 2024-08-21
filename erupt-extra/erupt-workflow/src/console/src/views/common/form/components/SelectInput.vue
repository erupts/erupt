<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-select v-if="!expanding" v-model="_value" :placeholder="placeholder" class="max-fill" disabled size="medium"/>
      <el-radio-group v-else v-model="_value">
        <el-radio v-for="(op, index) in options" :key="index" :label="_opLabel(op)" :value="_opValue(op)" disabled>{{_opLabel(op)}}</el-radio>
      </el-radio-group>
    </div>
    <div v-else>
      <el-select v-if="!expanding" v-model="_value" :disabled="!editable" :placeholder="placeholder" class="max-fill" clearable size="medium">
        <el-option v-for="(op, index) in options" :key="index" :label="_opLabel(op)" :value="_opValue(op)"></el-option>
      </el-select>
      <el-radio-group v-else v-model="_value">
        <el-radio v-for="(op, index) in options" :key="index" :label="_opLabel(op)" :value="_opValue(op)">{{_opLabel(op)}}</el-radio>
      </el-radio-group>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "SelectInput",
  components: {},
  props:{
    value:{
      type: String,
      default: null
    },
    placeholder:{
      type: String,
      default: '请选择选项'
    },
    expanding:{
      type: Boolean,
      default: false
    },
    options:{
      type: Array,
      default: () => {
        return []
      }
    },
  },
  data() {
    return {}
  },
  methods: {}
}
</script>

<style scoped>

</style>
