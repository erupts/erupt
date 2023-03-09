<template>
  <el-timeline :reverse="false" style="margin-top: 10px;" >
    <el-timeline-item v-for="(act, index) in activities"
                      :key="act.activityKey"
                      :type="timeLineType(act)"
                      size="large"
                      :timestamp="timestamp(act)" placement="top">
      <el-card v-if="act.tasks" shadow="never">
        <!-- 有任务的情况 -->
        <div slot="header" class="clearfix">
          <span>{{act.activityName}}</span>
        </div>
        <div>
          <div style="display: inline-block; margin-left: 10px;" v-for="task in act.tasks">
            <div style="display: inline-block;">
              <el-avatar style="background: #409EFF">{{task.finishUser}}</el-avatar>
            </div>
            <div style="display: inline-block; min-height: 50px; vertical-align: middle; margin-left: 10px;">
              <div>{{task.finishUserName}}</div>
              <div style="color: #909399; font-size: 14px; line-height: 20px;">{{task.finishDate}}</div>
            </div>
          </div>
        </div>
      </el-card>
      <el-card v-if="!act.tasks" shadow="never">
        <!-- 没有任务的情况 -->
        <span>{{act.activityName}}</span>
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
    timestamp(act) {
      if(act.activityKey===this.current) {
        return '现在'
      }else if(act.finishDate) {
        return act.finishDate;
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
