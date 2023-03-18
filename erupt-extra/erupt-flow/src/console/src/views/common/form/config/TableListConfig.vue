<template>
  <div>
    <el-form-item label="提示文字">
      <el-input size="small" v-model="value.placeholder" placeholder="提醒添加记录的提示"/>
    </el-form-item>
    <el-form-item label="最大行数">
      <tip slot="label" content="允许添加多少条记录（为0则不限制）">最大行数</tip>
      <el-input-number controls-position="right" :precision="0" :max="100" :min="0" size="small" v-model="value.maxSize"  placeholder="限制条数"/>
    </el-form-item>
    <el-form-item label="布局方式">
      <el-radio name="layout" :label="true" v-model="value.rowLayout">按表格</el-radio>
      <el-radio name="layout" :label="false" v-model="value.rowLayout">按表单</el-radio>
    </el-form-item>
    <el-form-item label="展示合计">
      <el-switch v-model="value.showSummary"></el-switch>
      <el-select v-if="value.showSummary" style="width: 100%;" size="small" v-model="value.summaryColumns" multiple clearable placeholder="请选择合计项">
        <el-option :label="column.title" :value="column.id" v-for="column in columns" :key="column.id"/>
      </el-select>
    </el-form-item>
    <el-form-item label="展示边框">
      <el-switch v-model="value.showBorder"></el-switch>
    </el-form-item>
  </div>
</template>

<script>
export default {
  name: "TableListConfig",
  components: {},
  props:{
    value:{
      type: Object,
      default: ()=>{
        return {}
      }
    }
  },
  computed:{
    columns(){
      return this.value.columns.filter(c => c.valueType === 'Number')
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
