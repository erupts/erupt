<template>
  <div>
    <el-form-item label="提示文字">
      <el-input v-model="value.placeholder" placeholder="请设置提示语" size="small"/>
    </el-form-item>
    <el-form-item label="保留小数">
      <el-input-number v-model="value.precision" :max="3" :min="0" :precision="0" controls-position="right" placeholder="小数位数"  size="small"/>
       位
    </el-form-item>
    <el-form-item label="展示大写">
      <el-switch v-model="value.showChinese"></el-switch>
    </el-form-item>
  </div>
</template>

<script>
export default {
  name: "AmountInputConfig",
  components: {},
  props:{
    value:{
      type: Object,
      default: ()=>{
        return {}
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
