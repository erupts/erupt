<template>
  <div>
    <el-form-item label="提示文字">
      <el-input v-model="value.placeholder" placeholder="请设置提示语" size="small"/>
    </el-form-item>
    <el-form-item label="数量限制">
      <tip slot="label" content="限制最大上传图片数量（为0则不限制）">数量限制</tip>
      <el-input-number v-model="value.maxNumber" :precision="0" class="max-fill" controls-position="right" placeholder="最多上传几张图片" size="small"/>
    </el-form-item>
    <el-form-item label="大小限制">
      <tip slot="label" content="限制单个图片最大大小-MB（为0则不限制）">大小限制</tip>
      <el-input-number v-model="value.maxSize" :precision="1" class="max-fill" controls-position="right" placeholder="单个文件最大大小" size="small"/>
    </el-form-item>
    <el-form-item label="图片压缩">
      <el-switch v-model="value.enableZip"></el-switch>
    </el-form-item>
  </div>
</template>

<script>
export default {
  name: "ImageUploadConfig",
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

<style lang="less" scoped>
/deep/ .el-form-item__label{
  padding: 0 12px 0 0;
}
</style>
