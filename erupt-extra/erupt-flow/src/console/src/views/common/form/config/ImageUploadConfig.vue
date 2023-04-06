<template>
  <div>
    <el-form-item label="提示文字">
      <el-input size="small" v-model="value.placeholder" placeholder="请设置提示语"/>
    </el-form-item>
    <el-form-item label="数量限制">
      <tip slot="label" content="限制最大上传图片数量（为0则不限制）">数量限制</tip>
      <el-input-number class="max-fill" controls-position="right" :precision="0" size="small" v-model="value.maxNumber" placeholder="最多上传几张图片"/>
    </el-form-item>
    <el-form-item label="大小限制">
      <tip slot="label" content="限制单个图片最大大小-MB（为0则不限制）">大小限制</tip>
      <el-input-number class="max-fill" controls-position="right" :precision="1" size="small" v-model="value.maxSize" placeholder="单个文件最大大小"/>
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
