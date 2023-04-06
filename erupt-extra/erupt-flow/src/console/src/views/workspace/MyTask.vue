<template>
  <div v-loading="loading" class="myTask">
    <el-row style="margin-bottom: 20px">
      <el-col :xs="12" :sm="10" :md="8" :lg="6" :xl="4">
        <el-input size="medium" v-model="queryParam.keywords" placeholder="搜索待我审批的工单" clearable>
          <!--<i slot="prefix" class="el-input__icon el-icon-search"></i>-->
        </el-input>
      </el-col>
      <el-col :xs="12" :sm="10" :md="8" :lg="6" :xl="4" style="padding-left: 10px;">
        <el-button icon="el-icon-search" round @click="reloadDatas">搜索</el-button>
      </el-col>
    </el-row>

    <ul class="infinite-list-wrapper taskPanel"
        v-infinite-scroll="loadDatas"
        infinite-scroll-disabled="disabled">
      <li v-for="(task, index) in dataList">
        <el-card shadow="hover" class="taskCard">
          <div slot="header" class="clearfix">
            <el-link @click="showDetail(task)" :underline="false" style="font: 18px large;">{{task.taskName}}</el-link>
            <span style="font: 12px Extra Small; color: #909399; padding-left: 20px;"> {{task.createDate}}</span>
            <el-button style="float: right; padding: 3px 0" type="text"
                       @click="showDetail(task)">详情</el-button>
          </div>
          <div class="text item">
            <el-row>
              <el-col :xs="6" :sm="6" :md="6" :lg="6" :xl="4">
                <i class="el-icon-eleme ic avator" style="background: rgb(30, 144, 255);"> </i>
                <span class="taskCell" style="color: #909399;">{{task.formName}}</span>
              </el-col>
              <el-col :xs="6" :sm="6" :md="6" :lg="6" :xl="4">
                <span class="taskCell" style="color: #909399;">{{task.businessTitle}}</span>
              </el-col>
              <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="8">
                <span class="taskCell" style="color: #909399;">{{task.instCreatorName + ' 发起于 ' + task.instCreateDate}}</span>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </li>
      <div v-if="loading" style="text-align: center; color: #C0C4CC; padding: 10px; min-width: 30px; min-height: 50px;">加载中...</div>
      <div v-if="noMore" style="text-align: center; color: #C0C4CC; padding: 10px; min-width: 30px; min-height: 50px;">没有更多了~</div>
    </ul>

    <el-dialog :title="selectInst.businessTitle" width="800px" top="20px" :visible.sync="openItemDl" :close-on-click-modal="false">
      <TaskDetail ref="taskDetail" :task-id="selectInst.id" mode="audit" v-if="openItemDl"></TaskDetail>
      <span slot="footer" class="dialog-footer" style="padding-right: 20px;">
        <el-button @click="openItemDl = false">关 闭</el-button>
        <el-button type="danger" @click="refuse(selectInst.id)">拒 绝</el-button>
        <el-button type="primary" @click="complete(selectInst.id)">同 意</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>

import {completeTask, listMyTasks} from "@/api/process";
import TaskDetail from './TaskDetail';

export default {
  name: "MyTask",
  components: {TaskDetail},
  props: {
  },
  data() {
    return {
      selectInst: {},
      openItemDl: false,
      loading: false,
      queryParam: {
        keywords: '',//关键字搜索
        pageSize: 10,//每页条数
        pageIndex: 1,//当前页码
      },
      dataList: [],
      count: 0,//已加载的条数
      maxCount: 0,//最大条数
    }
  },
  mounted() {
    this.reloadDatas();
  },
  computed: {
    noMore () {
      return this.count >= this.maxCount;
    },
    disabled () {
      return this.loading || this.noMore
    }
  },
  methods: {
    //重新加载数据
    reloadDatas () {
      this.reset();
      this.loadDatas();
    },
    reset() {
      this.loading = true
      //重置所有
      this.queryParam = {//充值页数
        pageSize: 10,//每页条数
        pageIndex: 1,//当前页码
      }
      this.dataList = [];
      this.count = 0;//已加载的条数
      this.maxCount = 0;//最大条数
    },
    //加载数据
    loadDatas () {
      this.loading = true
      listMyTasks(this.queryParam).then(res => {
        this.loading = false
        res.data.list.map(t => {//追加到数据列表
          this.dataList.push(t);
          this.count += 1;
        })
        this.queryParam.pageIndex++;//页码+1
        this.maxCount = res.data.total;//设置最大条数
        //触发
        this.$emit("afterLoad", this.maxCount, this.dataList);
      }).finally(() => {this.loading=false});
    },
    showDetail(task) {
      this.selectInst = task;
      this.openItemDl = true;
    },
    complete(taskId) {
      this.$prompt('', '审批通过', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(( comfirm ) => {
        completeTask(taskId, comfirm.value).then(rsp => {
          this.$message.success(rsp.message);
          this.reloadDatas();
        });
      });
    },
    refuse(taskId) {
      this.$prompt('请输入拒绝意见', '您正在驳回审批', {
        type: "warning",
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(( comfirm ) => {
        this.$message.warning("暂不支持驳回");
        this.reloadDatas();
      });
    }
  }
}
</script>

<style lang="less" scoped>
.myTask {
  .taskPanel {
    overflow: auto;
    max-height: 700px;

    .taskCard {
      margin-bottom: 10px;
      margin-right: 20px;
      cursor: pointer;
    }
  }
  .avator {
    padding: 8px;
    border-radius: 8px;
    float: left;
    font-size: 20px;
    color: #ffffff;
    background: #38adff;
    height: 20px;
    line-height: 20px;
  }
  .taskCell {
    height: 35px;
    line-height: 35px;
    padding-left: 10px;
  }
}
</style>
