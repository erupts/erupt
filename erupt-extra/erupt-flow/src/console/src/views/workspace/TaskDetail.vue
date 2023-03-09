<template>
  <div v-loading="loading" style="padding: 10px;">
    <el-button icon="el-icon-back" class="back" type="info" size="mini" plain @click="back">返回
    </el-button>
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
    <div v-show="showTimeLine">
      <timeLine ref="timeLine" :current="activityKey"></timeLine>
    </div>

    <div v-if="mode==='audit'" style="padding-left: 20px; padding-top: 20px;">
      <el-button @click="back">取 消</el-button>
      <el-button type="primary" @click="complete(taskId)">完 成</el-button>
    </div>
  </div>
</template>

<script>
import FormRender from '@/views/common/form/FormRender'
import FormDesignRender from '@/views/admin/layout/form/FormDesignRender'
import {getFormDetail} from '@/api/design'
import {completeTask, getProcessInstance} from '@/api/process'
import TimeLine from '@/views/workspace/TimeLine'

export default {
  name: "InitiateProcess",
  components: {FormDesignRender, FormRender, TimeLine},
  props: {
  },
  data() {
    return {
      procInstId: null,
      taskId: null,
      activityKey: null,
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
      showTimeLine: false
    }
  },
  mounted() {
    this.procInstId = this.$route.params && this.$route.params.procInstId;
    this.taskId = this.$route.params && this.$route.params.taskId;
    this.activityKey = this.$route.params && this.$route.params.activityKey;
    this.mode = this.$route.params && this.$route.params.mode;
    this.loadFormData(this.procInstId);
    this.freshTimeLine();
  },
  computed: {
    forms() {
      return this.$store.state.design.formItems;
    }
  },
  methods: {
    loadFormData(procInstId) {
      this.loading = true
      getProcessInstance(procInstId).then(rsp => {
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
    freshTimeLine() {
      this.$nextTick(() => {
        this.$refs.timeLine.freshForInst(this.procInstId);
      });
    },
    validate(call) {
      this.$refs.form.validate(call);
    },
    getFormData() {
      return this.formData;
    },
    complete(taskId) {
      this.$prompt('请输入审批意见', '完成任务', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(( comfirm ) => {
        completeTask(taskId, comfirm.value).then(rsp => {
          this.$message.success(rsp.message);
          this.back();
        });
      });
    },
    back() {
      this.$router.push({ name: 'workspace', params: { activeName: 'tab2' } });
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
