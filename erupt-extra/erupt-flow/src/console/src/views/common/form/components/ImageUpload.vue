<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <div class="design">
        <i class="el-icon-plus"></i>
      </div>
      <p>{{ placeholder }} {{ sizeTip }}</p>
    </div>
    <div v-else>
<!--      <el-select class="max-fill" v-show="false" v-model="_value" multiple size="medium" clearable :placeholder="placeholder">
        <el-option v-for="(op, index) in selectOptions" v-if="op" :key="index" :value="_opValue(op)" :label="_opLabel(op)"></el-option>
      </el-select>-->
      <el-upload :file-list="_value" :action="uploadUrl" :limit="maxSize" :multiple="maxSize > 0" with-credentials
                 list-type="picture-card"
                 :headers="{token: getToken()}"
                 :data="uploadParams"
                 :before-upload="beforeUpload"
                 :on-success="onSuccess"
                 :disabled="!editable"
                 accept=".jpg,.jepg,.png,.gif,.bmp,.svg"
      >
        <i slot="default" class="el-icon-plus"></i>
        <div slot="file" slot-scope="{file}">
          <el-image class="el-upload-list__item-thumbnail" :src="file.url">
          </el-image>
          <span class="el-upload-list__item-actions">
              <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                <i class="el-icon-zoom-in"></i>
              </span>
              <span v-if="editable" class="el-upload-list__item-delete" @click="handleDownload(file)">
                <i class="el-icon-download"></i>
              </span>
              <span v-if="editable" class="el-upload-list__item-delete" @click="handleRemove(file)">
                <i class="el-icon-delete"></i>
              </span>
            </span>
        </div>
        <div v-if="editable" slot="tip" class="el-upload__tip">{{ placeholder }} {{ sizeTip }}</div>
      </el-upload>

      <el-dialog title="" :visible.sync="viewImg" :destroy-on-close="true" append-to-body center>
        <div style="text-align: center;">
          <el-image :src="viewSrc" class="viewImg"></el-image>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'
import {uploadUrl, deleteFile} from '@/api/fileUpload'
import {getToken} from '@/api/auth'

export default {
  mixins: [componentMinxins],
  name: "ImageUpload",
  components: {},
  props: {
    value:{
      type: Array,
      default: () => {
        return []
      }
    },
    placeholder: {
      type: String,
      default: '请选择图片'
    },
    maxSize: {
      type: Number,
      default: 5
    },
    maxNumber:{
      type: Number,
      default: 10
    },
    enableZip: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    sizeTip() {
      return this.maxSize > 0 ? `| 每张图不超过${this.maxSize}MB` : ''
    },
  },
  data() {
    return {
      uploadUrl: uploadUrl,
      uploadParams: {},
      fileList: [],
      viewImg: false,
      viewSrc: '',
    }
  },
  mounted() {
  },
  methods: {
    getToken() {
      return getToken();
    },
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
      this._value=fileList;//删除前端文件
      deleteFile({//删除后端
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
      fileList.forEach(f => {
        if(f.url==file.url) {
          f.url = res.data.url;
          console.log(f.url);
        }
      })
      this._value=fileList;
    },
    handlePictureCardPreview(file) {
      this.viewSrc = file.url;
      this.viewImg = true;
    }
  }
}
</script>

<style lang="less" scoped>
.design {
  i {
    padding: 10px;
    font-size: xx-large;
    background: white;
    border: 1px dashed #8c8c8c;
  }
}
/deep/ .el-upload--picture-card{
  width: 80px;
  height: 80px;
  line-height: 87px;
}
/deep/ .el-upload-list__item{
  width: 80px;
  height: 80px;
  .el-upload-list__item-actions{
    &> span+span{
      margin: 1px;
    }
  }
}
.viewImg{
  max-width: 600px;
  max-height: 600px;
  width: auto;
  height: auto;
}
</style>
