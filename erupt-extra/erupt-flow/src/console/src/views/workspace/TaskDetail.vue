<template>
  <div v-loading="loading" style="padding: 10px 20px;">
    <div v-if="!loading">
      <p style="font: 14px Base; color: #909399;">{{taskDetail.instCreatorName + " 发布于 " + taskDetail.instCreateDate}}</p>
      <form-render class="process-form" ref="form" mode="PC"
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
import {getInstDetail, getTaskDetail} from '@/api/process'
import TimeLine from '@/views/workspace/TimeLine'

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
      myInstId: null,
      loading: false,
      taskDetail: {
        formItems: [],
        formData: {}
      },
      showTimeLine: false,
      count: 0
    }
  },
  mounted() {
    this.loading = true
    this.myInstId = this.instId;
    if(this.taskId) {//有任务，则使用任务刷新
      this.loadByTaskId(this.taskId);
    }else if(this.myInstId){//否则使用实例刷新
      this.loadByInstId(this.myInstId);
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
        let taskDetailTemp = res.data || {};
        //处理权限
        let node;
        if(node=this.findNode(taskDetailTemp.activityKey, taskDetailTemp.nodeConfig)) {
          let permMap = {};
          node.props.formPerms.forEach(p => {
            permMap[p.id] = p.perm;
          });
          this.setPerms(taskDetailTemp.formItems, permMap)
        }
        this.taskDetail = taskDetailTemp;
        this.myInstId = this.taskDetail.processInstId;
      }).then(() => {
        this.$refs.timeLine.freshForInst(this.myInstId);
      }).finally(() => this.loading=false);
    },
    findNode(key, node) {
      if(key==node.id) {
        return node;
      }else {
        let target;
        if(node.branchs && node.branchs.length>0) {
          for(let i in node.branchs) {
            if(target = this.findNode(key, node.branchs[i])) {
              return target;
            }
          }
        }else if(node.children) {
          if(target = this.findNode(key, node.children)) {
            return target;
          }
        }
      }
      return null;
    },
    setPerms(items, permMap) {
      items.forEach(item => {
        item.props.perm = permMap[item.id] || 'R';//默认只读
        if(item.props.perm=='E') {//可编辑
          item.props.editable = true;
        }else {//其他情况全部只读
          item.props.editable = false;
        }
        if(item.props.items) {
          this.setPerms(item.props.items, permMap);
        }
      })
    },
    //根据实例查询数据
    loadByInstId(myInstId) {
      this.loading = true
      getInstDetail(myInstId).then( res => {
        this.loading = false
        let taskDetailTemp = res.data || {};
        //处理权限
        this.setPerms(taskDetailTemp.formItems, {})
        this.taskDetail = taskDetailTemp;
      }).then(() => {
        this.$refs.timeLine.freshForInst(this.myInstId);
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
