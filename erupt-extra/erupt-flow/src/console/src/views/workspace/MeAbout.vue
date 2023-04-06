<template>
  <div v-loading="loading" class="meAbout">
    <el-row style="margin-bottom: 20px">
      <el-col :xs="12" :sm="10" :md="8" :lg="6" :xl="4">
        <el-input size="medium" v-model="queryParam.keywords" placeholder="搜索我发起的、我审批的、抄送我的工单" clearable>
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
      <li v-for="inst in dataList">
        <el-card shadow="hover" class="taskCard">
          <div slot="header" class="clearfix" style="padding-left: 10px;">
            <div :class="{'angle_mark':true
                        , 'angle_mark_color1': inst.tag==='发起'
                         , 'angle_mark_color2': inst.tag==='审批'
                         , 'angle_mark_color3': inst.tag==='抄送'
            }" v-if="inst.tag">
              <span>{{inst.tag}}</span>
            </div>
            <el-link @click="showDetail(inst)" :underline="false" style="font: 18px large;">{{inst.businessTitle}}</el-link>
            <el-button style="float: right; padding: 3px 0" type="text"
                       @click="showDetail(inst)">详情</el-button>
          </div>
          <div class="text item">
            <el-row>
              <el-col :xs="6" :sm="6" :md="6" :lg="6" :xl="4">
                <i class="el-icon-eleme ic avator"> </i>
                <span class="taskCell" style="color: #909399;">{{inst.formName}}</span>
              </el-col>
              <el-col :xs="12" :sm="12" :md="12" :lg="12" :xl="8">
                <span class="taskCell" style="color: #909399;">{{inst.creatorName + ' 发起于 ' + inst.createDate}}</span>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </li>
      <div v-if="loading" style="text-align: center; color: #C0C4CC; padding: 10px; min-width: 30px; min-height: 50px;">加载中...</div>
      <div v-if="noMore" style="text-align: center; color: #C0C4CC; padding: 10px; min-width: 30px; min-height: 50px;">没有更多了~</div>
    </ul>

    <el-dialog :title="selectInst.businessTitle" width="800px" top="20px" :visible.sync="openItemDl" :close-on-click-modal="false">
      <TaskDetail ref="taskDetail" :inst-id="selectInst.id" v-if="openItemDl"></TaskDetail>
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
          color: #fff;
          width: 100%;
          bottom: 4px;
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
  }
}
</style>
