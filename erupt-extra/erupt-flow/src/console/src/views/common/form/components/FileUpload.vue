<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-button size="small" icon="el-icon-paperclip" round>选择文件</el-button>
      <ellipsis :row="1" :content="placeholder + sizeTip" hoverTip slot="tip" class="el-upload__tip"/>
    </div>
    <div v-else>
      <el-upload :file-list="_value" action="#" :limit="maxSize" with-credentials :multiple="maxSize > 0" :data="uploadParams"
                 :auto-upload="false" :before-upload="beforeUpload">
        <el-button :disabled="formDisable" size="small" icon="el-icon-paperclip" round>选择文件</el-button>
        <ellipsis :row="1" :content="placeholder + sizeTip" hoverTip slot="tip" class="el-upload__tip"/>
      </el-upload>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "ImageUpload",
  components: {},
  props: {
    placeholder: {
      type: String,
      default: '请选择附件'
    },
    value:{
      type: Array,
      default: () => {
        return []
      }
    },
    maxSize: {
      type: Number,
      default: 5
    },
    maxNumber:{
      type: Number,
      default: 10
    },
    fileTypes: {
      type: Array,
      default: () => {
        return []
      }
    }
  },
  computed: {
    sizeTip() {
      if (this.fileTypes.length > 0){
        return ` | 只允许上传[${String(this.fileTypes).replaceAll(",", "、")}]格式的文件，且单个附件不超过${this.maxSize}MB`
      }
      return this.maxSize > 0 ? ` | 单个附件不超过${this.maxSize}MB` : ''
    }
  },
  data() {
    return {
      disabled: false,
      uploadParams: {}
    }
  },
  methods: {
    beforeUpload(file){
      const alows = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'];
      if (alows.indexOf(file.type) === -1){
        this.$message.warning("存在不支持的图片格式")
      }else if(this.maxSize > 0 && file.size / 1024 / 1024 > this.maxSize){
        this.$message.warning(`单张图片最大不超过 ${this.maxSize}MB`)
      }else {
        return true
      }
      return false
    },
    handleRemove(file, fileList) {
      console.log(file, fileList);
    },
    handlePictureCardPreview(file) {
      console.log(file)
    },
    handleDownload(file) {
      console.log(file);
    }
  }
}
</script>

<style lang="less" scoped>

</style>
