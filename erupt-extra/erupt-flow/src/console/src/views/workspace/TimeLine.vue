<template>
  <el-timeline :reverse="false" style="margin-top: 10px;" >
    <div v-if="activities.length<=0" style="padding-left: 10px;color: #909399;">
      <p>填写表单以预览时间线</p>
      <el-skeleton :rows="6" style="width: 480px" animated />
    </div>
    <el-timeline-item v-for="(act, index) in activities"
                      :key="act.activityKey"
                      :type="timeLineType(act)"
                      size="large"
                      :timestamp="act.createDate" placement="top">
      <el-card v-if="act.tasks" shadow="never">
        <!-- 有任务的情况 -->
        <div slot="header" class="clearfix">
          <span>{{act.activityName}}</span>
          <span style="font: 12px Extra Small; color: #909399; margin-left: 10px;">{{ (act.description||'')}}</span>
        </div>
        <div>
          <div style="display: inline-block; margin-left: 10px;" v-for="task in act.tasks">
            <div style="display: inline-block;">
              <el-avatar style="background: #409EFF">{{task.finishUser || task.taskOwner || task.assignee}}</el-avatar>
            </div>
            <div style="display: inline-block; min-height: 60px; vertical-align: middle; margin-left: 10px;">
              <div>{{task.finishUserName || task.taskOwner || task.assignee || '候选人'}}</div>
              <div v-if="task.finishDate" style="color: #67C23A; font-size: 14px; line-height: 20px;">{{task.finishDate }}</div>
              <div v-else style="color: #E6A23C; font-size: 14px; line-height: 20px;">{{ '审批中'}}</div>
            </div>
          </div>
        </div>
      </el-card>
      <el-card v-if="!act.tasks" shadow="never">
        <!-- 没有任务的情况 -->
        <span>{{act.activityName}}</span>
        <span style="font: 12px Extra Small; color: #909399; margin-left: 10px;">{{ (act.description||'')}}</span>
      </el-card>
    </el-timeline-item>
  </el-timeline>
</template>

<script>
import {timeLine, timeLinePreview} from '@/api/process'

export default {
  name: "TimeLine",
  components: {},
  props: {
    current: {
      default: 'root'
    }
  },
  data() {
    return {
      loading: false,
      activities: []
    }
  },
  mounted() {
  },
  computed: {
  },
  methods: {
    getActivities() {
      return this.activities;
    },
    timestamp(act) {
      if(act.activityKey===this.current) {
        return act.createDate;
      }else {
        return '';
      }
    },
    timeLineType(act) {
      if(act.activityKey===this.current) {
        return 'warning'
      }else if(act.finishDate) {
        return 'success';
      }else {
        return 'primary';
      }
    },
    fresh(defId, json) {
      this.loading = true;
      this.activities = []
      timeLinePreview(defId, json).then(res => {
        this.loading = false;
        this.activities = res.data;
      });
    },
    freshForInst(instId) {
      this.loading = true;
      this.activities = []
      timeLine(instId).then(res => {
        this.loading = false;
        this.activities = res.data;
      });
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
