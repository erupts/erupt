<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-button size="small" icon="el-icon-paperclip" round>选择文件</el-button>
      <ellipsis :row="1" :content="placeholder + sizeTip" hoverTip slot="tip" class="el-upload__tip"/>
    </div>
    <div v-else>
      <el-upload :file-list="_value" :action="uploadUrl" :limit="maxSize" :multiple="maxSize > 0" with-credentials
                 :headers="{token: getToken()}"
                 :data="uploadParams"
                 :on-preview="handleDownload"
                 :before-upload="beforeUpload"
                 :on-success="onSuccess"
                 :disabled="!editable"
                 :auto-upload="true"
      >
        <el-button :disabled="!editable" size="small" icon="el-icon-paperclip" round>选择文件</el-button>
        <ellipsis :row="1" :content="placeholder + sizeTip" hoverTip slot="tip" class="el-upload__tip"/>
      </el-upload>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'
import {uploadUrl, deleteFile} from '@/api/fileUpload'
import {getToken} from "@/api/auth";

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
  mounted() {
  },
  data() {
    return {
      uploadUrl: uploadUrl,
      uploadParams: {},
    }
  },
  methods: {
    getToken() {
      return getToken();
    },
    beforeUpload(file){
      const alows = [
        'image/jpg',
        'image/jpeg',
        'image/png',
        'image/gif',
        'audio/mpeg',
        'video/mp4',
        'application/x-mpegURL',
        'video/x-ms-wmv',
        'video/x-msvideo',
        'video/x-flv',
        'video/x-ms-wmv',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'text/plain',
        'application/pdf',
        'application/vnd.ms-powerpoint',
        'application/zip',
        'application/x-zip-compressed',
        'application/x-rar',];
      if (file.type && alows.indexOf(file.type) === -1){
        this.$message.warning("禁止上传 "+file.type+" 文件")
      }else if(this.maxSize > 0 && file.size / 1024 / 1024 > this.maxSize){
        this.$message.warning(`单个文件最大不超过 ${this.maxSize}MB`)
      }else {
        return true
      }
      return false
    },
    handleRemove(file, fileList) {
      this._value=fileList;//删除前端文件
      deleteFile({
        path: file.url
      });
    },
    handleDownload(file) {
      const link = document.createElement('a');
      link.href = file.url;
      link.setAttribute('download', file.name);
      document.body.appendChild(link);
      link.click();
    },
    onSuccess(res, file, fileList) {
      file.url = res.data.url;//url设置为服务端的url
      this._value=fileList;
    },
  }
}
</script>

<style lang="less" scoped>

</style>
