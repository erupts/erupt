<template>
  <div v-loading="loading" style="padding: 10px;">
    <div v-if="!loading">
      <p style="font: 20px Extra large;">{{formData.businessTitle}}</p>
      <p style="font: 14px Base; color: #909399;">{{formData.creatorName + " 发布于 " + formData.createDate}}</p>
      <!--渲染表单-->
      <form-render class="process-form" ref="form" :forms="forms" mode="PC" :formDisable="true" v-model="formData.formItems"/>
    </div>
    <div style="padding-bottom: 10px;" @click="showTimeLine = !showTimeLine">
      <el-button type="text" size="medium" style="color: #909399;">
        审批流程 <i :class="{'el-icon-arrow-down': !showTimeLine, 'el-icon-arrow-up': showTimeLine}"></i>
      </el-button>
    </div>
    <el-collapse v-show="showTimeLine">
      <el-timeline :reverse="false" style="margin-top: 10px;">
        <el-timeline-item
            v-for="(activity, index) in activities"
            :key="index"
            :icon="activity.icon"
            :type="activity.type"
            :color="activity.color"
            :size="activity.size"
            :timestamp="activity.timestamp">
          {{activity.content}}
        </el-timeline-item>
      </el-timeline>
    </el-collapse>
  </div>
</template>

<script>
import FormRender from '@/views/common/form/FormRender'
import FormDesignRender from '@/views/admin/layout/form/FormDesignRender'
import {getFormDetail} from '@/api/design'
import {getProcessInstance} from '@/api/process'

export default {
  name: "InitiateProcess",
  components: {FormDesignRender, FormRender},
  props: {
  },
  data() {
    return {
      dataId: null,
      mode: 'view',
      loading: false,
      formData: {},
      form: {
        formId: '',
        formName: "",
        logo: {},
        formItems: [],
        process: {},
        remark: ""
      },
      showTimeLine: false,
      activities: [{
        content: '支持使用图标',
        timestamp: '2018-04-12 20:46',
        size: 'large',
        type: 'primary',
        icon: 'el-icon-more'
      }, {
        content: '支持自定义颜色',
        timestamp: '2018-04-03 20:46',
        color: '#0bbd87'
      }, {
        content: '支持自定义尺寸',
        timestamp: '2018-04-03 20:46',
        size: 'large'
      }, {
        content: '默认样式的节点',
        timestamp: '2018-04-03 20:46'
      }]
    }
  },
  mounted() {
    this.dataId = this.$route.params && this.$route.params.dataId;
    this.mode = this.$route.params && this.$route.params.mode;
    this.loadFormData(this.dataId);
  },
  computed: {
    forms() {
      return this.$store.state.design.formItems;
    }
  },
  methods: {
    loadFormData(dataId) {
      this.loading = true
      getProcessInstance(dataId).then(rsp => {
        this.loading = false
        //加载数据
        this.formData = rsp.data;
        this.formData.formItems = JSON.parse(this.formData.formItems)
        //加载表单
        this.loadFormInfo(rsp.data.formId)
      }).catch(err => {
        this.loading = false
        this.$message.error(err)
      })
    },
    loadFormInfo(formId) {
      this.loading = true
      getFormDetail(formId).then(rsp => {
        this.loading = false
        let form = rsp.data;
        form.logo = JSON.parse(form.logo)
        form.settings = JSON.parse(form.settings)
        form.formItems = JSON.parse(form.formItems)
        form.process = JSON.parse(form.process)
        this.form = form
        //构建表单及校验规则
        this.$store.state.design = form
      }).catch(err => {
        this.loading = false
        this.$message.error(err)
      })
    },
    validate(call) {
      this.$refs.form.validate(call);
    },
    getFormData() {
      return this.formData;
    },
    complete() {

    }
  }
}
</script>

<style lang="less" scoped>
.process-form {
  /deep/ .el-form-item__label {
    padding: 0 0;
  }
}
</style>
