<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-select class="max-fill" v-if="!expanding" size="medium" multiple v-model="_value" disabled :placeholder="placeholder"/>
      <el-checkbox-group v-else v-model="_value">
        <el-checkbox disabled v-for="(op, index) in options" :key="index" :value="_opValue(op)" :label="_opLabel(op)">{{_opLabel(op)}}</el-checkbox>
      </el-checkbox-group>
    </div>
    <div v-else>
      <el-select :disabled="!editable" class="max-fill" v-if="!expanding" v-model="_value" multiple size="medium" clearable :placeholder="placeholder">
        <el-option v-for="(op, index) in options" :key="index" :value="_opValue(op)" :label="_opLabel(op)"></el-option>
      </el-select>
      <el-checkbox-group v-else v-model="_value">
        <el-checkbox v-for="(op, index) in options" :key="index" :value="_opValue(op)" :label="_opLabel(op)">{{_opLabel(op)}}</el-checkbox>
      </el-checkbox-group>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "MultipleSelect",
  components: {},
  props:{
    placeholder:{
      type: String,
      default: '请选择选项'
    },
    value:{
      type: Array,
      default: () => {
        return []
      }
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
  computed:{

  },
  data() {
    return {
      checks:[]
    }
  },
  methods: {}
}
</script>

<style scoped>

</style>
