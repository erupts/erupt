<template>
  <div v-loading="loading" style="padding: 10px 20px;">
    <div v-if="!loading">
      <p style="font: 14px Base; color: #909399;">{{taskDetail.instCreatorName + " 发布于 " + taskDetail.instCreateDate}}</p>
      <form-render class="process-form" ref="form" mode="PC" :formDisable="formDisable"
                   :forms="taskDetail.formItems"
                   v-model="taskDetail.formData" @input="valChange"/>
    </div>
    <div style="padding-bottom: 10px;" @click="showTimeLine = !showTimeLine">
      <el-button type="text" size="medium" style="color: #909399;">
        审批流程 <i :class="{'el-icon-arrow-down': !showTimeLine, 'el-icon-arrow-up': showTimeLine}"></i>
      </el-button>
    </div>
    <div v-show="showTimeLine">
      <timeLine ref="timeLine" :current="taskDetail.activityKey"></timeLine>
    </div>
  </div>
</template>

<script>
import FormRender from '@/views/common/form/FormRender'
import FormDesignRender from '@/views/admin/layout/form/FormDesignRender'
import {completeTask, getInstDetail, getTaskDetail} from '@/api/process'
import TimeLine from '@/views/workspace/TimeLine'
import {getToken} from "@/api/auth";

export default {
  name: "InitiateProcess",
  components: {FormDesignRender, FormRender, TimeLine},
  props: {
    instId: {
      type: String,
      required: false
    },
    taskId: {
      type: String,
      required: false
    },
    mode: {
      type: String,
      default: 'view'
    }
  },
  data() {
    return {
      loading: false,
      formDisable: true,//是否禁用编辑
      taskDetail: {
        formItems: [],
        formData: {}
      },
      showTimeLine: false,
      count: 0,
    }
  },
  mounted() {
    this.loading = true
    this.formDisable = true;
    if(this.taskId) {//有任务，则使用任务刷新
      this.loadByTaskId(this.taskId);
    }else if(this.instId){//否则使用实例刷新
      this.loadByInstId(this.instId);
    }
  },
  computed: {
  },
  methods: {
    //根据任务查询数据
    loadByTaskId(taskId) {
      this.loading = true
      getTaskDetail(taskId).then( res => {
        this.loading = false;
        this.taskDetail = res.data || {};
        this.instId = res.data.processInstId;
      }).then(() => {
        this.$refs.timeLine.freshForInst(this.instId);
      }).finally(() => this.loading=false);
    },
    //根据实例查询数据
    loadByInstId(instId) {
      this.loading = true
      getInstDetail(instId).then( res => {
        this.loading = false
        this.taskDetail = res.data || {};
      }).then(() => {
        this.$refs.timeLine.freshForInst(this.instId);
      }).finally(() => this.loading=false);
    },
    validate(call) {
      this.$refs.form.validate(call);
    },
    getFormData() {
      return this.taskDetail.formData;
    },
    valChange(val) {
      console.log(val);
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
