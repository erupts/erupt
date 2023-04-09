<template>
  <div v-loading="loading" style="padding: 10px 20px;">
    <el-button icon="el-icon-back" class="back" type="info" size="mini" plain @click="back">返回
    </el-button>
    <div v-if="!loading">
      <p style="font: 20px Extra large;">{{taskDetail.businessTitle}}</p>
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

    <div v-if="mode==='audit'" style="padding-left: 25px; padding-top: 20px;">
      <el-button @click="back">取 消</el-button>
      <el-button type="primary" @click="complete(taskId)">完 成</el-button>
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
  },
  data() {
    return {
      procInstId: null,
      taskId: null,
      activityKey: null,
      mode: 'view',
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
    this.procInstId = this.$route.params && this.$route.params.procInstId;//实例id
    this.taskId = this.$route.params && this.$route.params.taskId;//任务id
    this.mode = this.$route.params && this.$route.params.mode;//查看模式
    if(this.taskId && this.taskId!=='null') {//有任务，则使用任务刷新
      this.formDisable = true;
      this.loadByTaskId(this.taskId);
    }else if(this.procInstId && this.procInstId!=='null'){//否则使用实例刷新
      this.formDisable = true;
      this.loadByInstId(this.procInstId);
    }
    //然后刷新时间线
    this.freshTimeLine();
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
      }).catch(e => this.loading=false);
    },
    //根据实例查询数据
    loadByInstId(instId) {
      this.loading = true
      getInstDetail(instId).then( res => {
        this.loading = false
        this.taskDetail = res.data || {};
      }).catch(e => this.loading=false);
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
      return this.taskDetail.formData;
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
      this.$router.push("/workspace?_token=" + getToken());
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
