<template>
  <div class="workspace">
    <el-tabs type="border-card" v-model="activeName">
      <el-tab-pane label="发起工单" name="tab1">
        <el-row style="margin-bottom: 20px">
          <el-col :xs="12" :sm="10" :md="8" :lg="6" :xl="4">
            <el-input size="medium" v-model="formList.inputs" placeholder="搜索表单" clearable>
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
            </el-input>
          </el-col>
        </el-row>
        <el-collapse v-model="actives">
          <el-collapse-item v-for="(group, index) in formList.list" :key="index"
                            :title="group.groupName" :name="group.groupName"
                            v-show="group.groupId >= 0">
            <div v-for="(item, index) in group.processDefs" :key="index" class="form-item" @click="enterItem(item)">
              <i :class="item.logo.icon" :style="'background: '+item.logo.background"></i>
              <div>
                <ellipsis hover-tip :content="item.formName"/>
                <span>发起审批</span>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>
      <el-tab-pane :label="'待我处理('+(maxCount||0)+')'" name="tab2">
        <el-row style="margin-bottom: 20px">
          <el-col :xs="12" :sm="10" :md="8" :lg="6" :xl="4">
            <el-input size="medium" v-model="queryParam.keywords" placeholder="搜索我的工单" clearable>
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
            </el-input>
          </el-col>
        </el-row>

        <ul class="infinite-list-wrapper"
            v-infinite-scroll="loadMyWorks"
            infinite-scroll-disabled="disabled"
            style="overflow:auto; max-height: 700px;">
          <li v-for="(task, index) in dataList">
            <el-card shadow="hover" style="margin-bottom: 10px; margin-right: 20px; cursor: pointer;">
              <div slot="header" class="clearfix">
                <span style="font: 18px large;">{{task.taskName}}</span>
                <span style="font: 12px Extra Small; color: #909399; padding-left: 20px;"> {{task.createDate}}</span>
                <el-button style="float: right; padding: 3px 0" type="text" @click="showDetail(task.processInstId, task.id, task.activityKey)">详情</el-button>
              </div>
              <div class="text item">
                <el-row>
                  <el-col :xs="6" :sm="6" :md="6" :lg="6" :xl="4">
                    <i class="el-icon-eleme ic" style="background: rgb(30, 144, 255);"> </i>
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

      </el-tab-pane>
      <el-tab-pane label="我发起的" name="tab3">
        <div class="no-data">暂无数据 😀</div>
      </el-tab-pane>
      <el-tab-pane label="关于我的" name="tab4">
        <div class="no-data">暂无数据 😀</div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="发起审批" width="800px" top="20px" :visible.sync="openItemDl" :close-on-click-modal="false">
      <initiate-process ref="processForm" :code="selectForm.id" v-if="openItemDl"></initiate-process>
      <span slot="footer" class="dialog-footer">
				<el-button @click="openItemDl = false">取 消</el-button>
				<el-button type="primary" @click="submitForm">提 交</el-button>
			</span>
    </el-dialog>
  </div>
</template>

<script>
import {getFormGroupsWithProcDef} from '@/api/design'
import InitiateProcess from "./InitiateProcess";
import {startByFormId, listMyTasks} from '@/api/process'
import {getToken} from "@/api/auth";

export default {
  name: "workSpace",
  components: {InitiateProcess},
  data() {
    return {
      dataList: [],
      queryParam: {
        keywords: '',//关键字搜索
        pageSize: 10,//每页条数
        pageIndex: 1,//当前页码
      },
      count: 0,//已加载的条数
      maxCount: 0,//最大条数
      loading: false,
      activeName: 'tab1',
      openItemDl: false,
      selectForm: {},
      formItem: {},
      actives: [],
      formList: {
        list: [],
        inputs: '',
        searchResult: []
      },
      pending: {
        list: []
      }
    }
  },
  created() {
    //重新设置token
    sessionStorage.setItem("token", getToken());
  },
  mounted() {
    //这句话加上之后，从详情返回，会查询2次数据
    //this.activeName = (this.$route.params && this.$route.params.activeName)||'tab1';

    this.getGroups();
    this.initMyWorks();
  },
  computed: {
    noMore () {
      return this.count >= this.maxCount;
    },
    disabled () {
      return this.loading || this.noMore
    }
  },
  watch: {
    'formList.inputs' (newVal, oldVal) {
      this.getGroups();//关键字变更，重新查询
    }
  },
  methods: {
    getGroups() {
      getFormGroupsWithProcDef({keywords: this.formList.inputs}).then(rsp => {
        this.formList.list = rsp.data
        this.formList.list.forEach(group => {
          this.actives.push(group.groupName)
          group.processDefs.forEach(item => {
            item.logo = JSON.parse(item.logo)
          })
        })
        this.groups = rsp.data
      })
    },
    enterItem(item) {
      this.selectForm = item
      this.openItemDl = true
    },
    submitForm(){
      this.$refs.processForm.validate(valid => {
        if (valid) {
          startByFormId(this.selectForm.id, this.$refs.processForm.formData).then(rsp => {
            this.$message.success(rsp.message);
            this.openItemDl = false;
            this.initMyWorks()//重新查询一次
          })
        } else {
          this.$message.warning("请完善表单😥")
        }
      })
    },
    //初始化数据
    initMyWorks () {
      if(this.loading) {
        return ;
      }
      this.loading = true
      listMyTasks(this.queryParam).then(res => {
        this.loading = false
        this.queryParam.pageIndex = 1;//页码回到1
        this.maxCount = res.data.total;//最大条数
        this.dataList = res.data.list;
      });
    },
    //加载数据
    loadMyWorks () {
      if(this.loading) {
        return ;
      }
      this.loading = true
      listMyTasks(this.queryParam).then(res => {
        this.loading = false
        this.queryParam.pageIndex += 1;//每次页码+1
        this.maxCount = res.data.total;//最大条数
        res.data.list.map(t => {//追加到数据列表
          this.dataList.push(t);
          this.count += 1;
        })
      });
    },
    showDetail(instId, taskId, activityKey) {
      this.$router.push("/detail/"+instId+"/"+taskId+"/"+activityKey+"/audit?_token=" + getToken());
    }
  }
}
</script>

<style lang="less" scoped>
.workspace {
  padding: 50px 20px;
  position: relative;

  .back {
    position: absolute;
    left: 20px;
    top: 13px;
  }

  .no-data {
    text-align: center;
    padding: 50px 0;
    color: #656565;
    margin: 0 auto;
  }

  /deep/ .el-collapse {
    padding: 0 15px;
    background: #ffffff;

    .el-collapse-item__header {
      font-size: medium;
    }

    .el-collapse-item__wrap {
      padding: 20px 10px;
    }

    .el-tabs--border-card .el-tabs__content {
      padding: 40px 15px;
    }
  }

  .form-item {
    padding: 15px 10px;
    width: 200px;
    cursor: pointer;
    border: 1px solid #d9dada;
    border-radius: 5px;
    float: left;
    margin: 5px 10px;
    height: 37px;

    &:hover {
      border: 1px solid #448ed7;

      span {
        display: inline-block !important;
      }
    }

    i {
      padding: 8px;
      border-radius: 8px;
      float: left;
      font-size: 20px;
      color: #ffffff;
      background: #38adff;
      height: 20px;
      line-height: 20px;
    }

    div {
      height: 35px;
      line-height: 35px;

      div{
        display: inline-block;
        margin-left: 10px;
        width: 100px;
      }

      span {
        display: none;
        float: right;
        color: #38adff;
        font-size: 12px;
      }
    }

    /*span:nth-child(1) {
      float: left;
      margin: 5px 0 0 10px;
    }*/
    /*span:nth-child(1) {
      float: right;
      color: #448ed7;
      font-size: x-small;
      margin: 5px 0 5px 0;
    }*/
  }

  .infinite-list-item {
    background: #8c939d;
    margin: 5px 0px;
    padding: 5px;
  }
  .ic {
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

@media screen and (max-width: 800px) {
  .form-item {
    padding: 12px 10px !important;
    width: 150px !important;
    margin: 5px !important;

    &:hover {
      span:last-child {
        display: none !important;
      }
    }
  }
}
</style>
