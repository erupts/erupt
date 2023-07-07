<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-select class="max-fill" v-if="!expanding" size="medium" v-model="_value" disabled :placeholder="placeholder"/>
      <el-radio-group v-model="_value" v-else>
        <el-radio disabled v-for="(op, index) in options" :key="index" :value="_opValue(op)" :label="_opLabel(op)">{{_opLabel(op)}}</el-radio>
      </el-radio-group>
    </div>
    <div v-else>
      <el-select :disabled="!editable" class="max-fill" v-if="!expanding" v-model="_value" size="medium" clearable :placeholder="placeholder">
        <el-option v-for="(op, index) in options" :key="index" :value="_opValue(op)" :label="_opLabel(op)"></el-option>
      </el-select>
      <el-radio-group v-model="_value" v-else>
        <el-radio v-for="(op, index) in options" :key="index" :value="_opValue(op)" :label="_opLabel(op)">{{_opLabel(op)}}</el-radio>
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
