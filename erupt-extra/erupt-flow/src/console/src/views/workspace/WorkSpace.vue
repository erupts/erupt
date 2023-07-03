<template>
  <div class="workspace">
    <el-tabs type="border-card" v-model="activeName" @tab-click="changeTab">
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
                            v-show="group.groupId >= 0 && group.processDefs.length > 0">
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
      <el-tab-pane :label="'待我处理('+(myTaskCount||0)+')'" name="tab2">
        <MyTask ref="myTask" @afterLoad="setMyTaskCount"></MyTask>
      </el-tab-pane>
      <el-tab-pane label="我的工单" name="tab3">
        <me-about ref="meAbout"></me-about>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="formTitle" width="800px" top="20px" :visible.sync="openItemDl" :close-on-click-modal="false">
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
import {startByFormId} from '@/api/process'
import MyTask from './MyTask'
import MeAbout from './MeAbout'

export default {
  name: "workSpace",
  components: {InitiateProcess, MyTask, MeAbout},
  data() {
    return {
      dataList: [],
      loading: false,
      openItemDl: false,
      formTitle: '',
      activeName: 'tab1',
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
      },
      myTaskCount: 0,
    }
  },
  mounted() {
    this.getGroups();
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
      this.formTitle = item.formName;
      this.selectForm = item
      this.openItemDl = true
    },
    submitForm() {
      this.$refs.processForm.validate(valid => {
        if (valid) {
          const loading = this.$loading({
            lock: true,
            text: 'Loading',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          startByFormId(this.selectForm.id, this.$refs.processForm.formData).then(rsp => {
            loading.close();
            this.$message.success(rsp.message);
            this.openItemDl = false;
            //重新查询一次，待办和已办
            this.$refs.myTask.reloadDatas()
            this.$refs.meAbout.reloadDatas();
          }).catch(e => {
            loading.close();
            this.$message.error(e.message);
          });
        } else {
          this.$message.warning("请完善表单😥")
        }
      })
    },
    setMyTaskCount(count) {
      this.myTaskCount = count;
    },
    changeTab(tab) {
      this.$refs.myTask.reloadDatas()
      this.$refs.meAbout.reloadDatas();
      return true;
    }
  }
}
</script>

<style lang="less" scoped>
.workspace {
  padding: 18px;
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
    padding: 12px 8px;
    background: #ffffff;

    .el-collapse-item__header {
      font-size: medium;
    }

    .el-tabs--border-card .el-tabs__content {
      padding: 40px 15px;
    }
  }

  .form-item {
    padding: 8px;
    width: 200px;
    cursor: pointer;
    border: 1px solid #d9dada;
    border-radius: 5px;
    float: left;
    margin-right: 8px;
    margin-bottom: 8px;
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

      div {
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
