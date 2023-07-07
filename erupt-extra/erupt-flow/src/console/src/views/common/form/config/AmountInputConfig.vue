<template>
  <div>
    <el-form-item label="提示文字">
      <el-input size="small" v-model="value.placeholder" placeholder="请设置提示语"/>
    </el-form-item>
    <el-form-item label="保留小数">
      <el-input-number controls-position="right" :precision="0" :max="3" :min="0" size="small" v-model="value.precision"  placeholder="小数位数"/>
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
