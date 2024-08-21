<template>
  <div v-loading="loading" class="meAbout">
    <el-row style="margin-bottom: 20px">
      <el-col :lg="6" :md="8" :sm="10" :xl="4" :xs="12">
        <el-input v-model="queryParam.keywords" clearable placeholder="搜索我发起的、我审批的、抄送我的工单" size="medium">
          <!--<i slot="prefix" class="el-input__icon el-icon-search"></i>-->
        </el-input>
      </el-col>
      <el-col :lg="6" :md="8" :sm="10" :xl="4" :xs="12" style="padding-left: 10px;">
        <el-button icon="el-icon-search" round @click="reloadDatas">搜索</el-button>
      </el-col>
    </el-row>

    <ul v-infinite-scroll="loadDatas"
        class="infinite-list-wrapper taskPanel"
        infinite-scroll-disabled="disabled">
      <li v-for="inst in dataList">
        <el-card :body-style="{ padding: '5px 10px' }" class="taskCard" shadow="hover">
          <div class="taskCardHeader" style="padding-left: 24px;">
            <div v-if="inst.tag" :class="{'angle_mark':true
                        , 'angle_mark_color1': inst.tag==='发起'
                         , 'angle_mark_color2': inst.tag==='审批'
                         , 'angle_mark_color3': inst.tag==='抄送'
            }">
              <span>{{inst.tag}}</span>
            </div>
            <el-row>
              <el-col :lg="6" :md="8" :sm="8" :xl="4" :xs="20">
                <el-link :underline="false" style="font: 16px large;" @click="showDetail(inst)">{{inst.businessTitle}}</el-link>
              </el-col>
              <el-col :lg="6" :md="8" :sm="8" :xl="8" :xs="4">
                <el-tag :type="getStatus(inst).type"  style="margin-left: 10px;">
                  {{ getStatus(inst).text }}
                </el-tag>
                <span v-if="inst.status==='FINISHED' || inst.status==='SHUTDOWN'" style="color: #909399;">
                  {{'结束于 ' + inst.finishDate}}
                </span>
              </el-col>
              <el-col :lg="12" :md="8" :sm="8" :xl="12" :xs="0" style="text-align: right;">
                <el-button class="taskCell" style="padding: 3px 20px" type="text" @click="showDetail(inst)">详情</el-button>
              </el-col>
            </el-row>
          </div>
          <div class="taskCardBody">
            <el-row>
              <el-col :lg="6" :md="6" :sm="6" :xl="4" :xs="6">
                <i :class="JSON.parse(inst.logo).icon + ' ic avator'" :style="'background: '+JSON.parse(inst.logo).background"></i>
                <span class="taskCell" style="color: #909399;" @click="showDetail(inst)">{{inst.formName}}</span>
              </el-col>
              <el-col :lg="18" :md="18" :sm="18" :xl="20" :xs="18">
                <span class="taskCell" style="color: #909399;">{{inst.creatorName + ' 发起于 ' + inst.createDate}}</span>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </li>
      <div v-if="loading" style="text-align: center; color: #C0C4CC; padding: 10px; min-width: 30px; min-height: 50px;">加载中...</div>
      <div v-if="noMore" style="text-align: center; color: #C0C4CC; padding: 10px; min-width: 30px; min-height: 50px;">没有更多了~</div>
    </ul>

    <el-dialog :close-on-click-modal="false" :title="selectInst.businessTitle" :visible.sync="openItemDl" top="20px" width="800px">
      <TaskDetail v-if="openItemDl" ref="taskDetail" :inst-id.sync="selectInst.id"></TaskDetail>
      <span slot="footer" class="dialog-footer" style="padding-right: 20px;">
        <el-button @click="openItemDl = false">关 闭</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>

import {getMineAbout} from "@/api/process";
import TaskDetail from './TaskDetail';

export default {
  name: "MeAbout",
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
    getStatus(inst) {
      if('RUNNING'===inst.status) {
        return {
          text: '审批中',
          type: 'primary'
        }
      }else if('PAUSE'===inst.status) {
        return {
          text: '暂停',
          type: 'warning'
        }
      }else if('FINISHED'===inst.status) {
        return {
          text: '已完成',
          type: 'success'
        }
      }else if('SHUTDOWN'===inst.status) {
        return {
          text: '已拒绝',
          type: 'danger'
        }
      }
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
      getMineAbout(this.queryParam).then(res => {
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
    showDetail(inst) {
      this.selectInst = inst;
      this.openItemDl = true;
    }
  }
}
</script>

<style lang="less" scoped>
.meAbout {
  .taskPanel {
    overflow: auto;

    .taskCard {
      margin-bottom: 10px;
      margin-right: 20px;
      min-height: 100px;
      overflow: hidden;
      position: relative;
      padding-left: 5px;

      .angle_mark_color1 {
        background-color: #90ee90;
      }

      .angle_mark_color2 {
        background-color: #00ced1;
      }

      .angle_mark_color3 {
        background-color: #1e90ff;
      }

      // 角标
      .angle_mark {
        position: absolute;
        top: -40px;
        left: -40px;
        width: 80px;
        height: 80px;
        transform: rotate(-45deg);
        // 角标文字
        span {
          position: absolute;
          display: inline-block;
          font-size: 10px;
          color: #fff;
          width: 100%;
          bottom: 6px;
          left: 0;
          text-align: center;
        }
      }

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
    font-size: 14px;
  }
  .taskCardHeader {
    border-bottom: 1px solid #e3e3e3;
    padding: 5px 10px;
  }
  .taskCardBody {
    padding: 10px;
  }
}
</style>
