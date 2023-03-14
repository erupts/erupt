<template>
  <div class="from-panel" ref="panel">
    <div class="from-title">
      <el-button icon="el-icon-back" type="info" size="mini" circle plain style="margin-right: 15px" @click="$router.push('/')"></el-button>
      <span>流程面板</span>
      <div>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="newProcess('')">新建表单</el-button>
        <el-button icon="el-icon-plus" @click="addGroup" size="mini">新建分组</el-button>
      </div>
    </div>
    <draggable :list="groups" group="group" handle=".group-sort" filter=".undrag" @start="" @end="groupSort"
               :options="{animation: 300, chosenClass:'choose', sort:true, scroll: true}">
      <div :class="{'form-group':true, 'undrag': false}" v-for="(group, gidx) in groups" :key="gidx">
        <div class="form-group-title">
          <span>{{group.groupName}}</span>
          <span>({{group.items.length}})</span>
          <i v-if="group.groupId>0" class="el-icon-rank group-sort" :title="'长按拖动可对分组排序'"></i>
          <div v-if="group.groupId>0">
            <el-dropdown>
              <el-button type="text" icon="el-icon-setting" style="color: #8c939d">编辑分组</el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item icon="el-icon-edit-outline" @click.native="editGroup(group)">修改名称</el-dropdown-item>
                <el-dropdown-item icon="el-icon-delete" @click.native="delGroup(group)">删除分组</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <draggable style="width: 100%; min-height:25px" :list="group.items"
                   :group="'group_'+group.groupId" handle=".form-sort" filter=".undrag" @start="movingGroup = group" @end="formSort"
                   :options="{animation: 300, chosenClass:'choose', scroll: true, sort:true}">
          <div :class="{'form-group-item':true, 'undrag': false}" v-for="(item, index) in group.items" :key="index">
            <div class="form-sort" title="长按拖动进行排序">
              <i :class="item.logo.icon" :style="'background: '+item.logo.background"></i>
              <span>{{item.formName}}</span>
            </div>
            <div class="desp">{{item.remark}}</div>
            <div>
              <span>最后更新：{{item.updated}}</span>
            </div>
            <div>
              <!-- 正常的按钮组 -->
              <div v-if="!item.isStop">
                <el-button type="text" icon="el-icon-edit-outline" size="mini" @click="editFrom(item, group)">编辑</el-button>
                <el-button type="text" icon="el-icon-close" size="mini" @click="stopFrom(item)">停用</el-button>

                <el-popover placement="left" trigger="click" width="400" style="margin-left: 10px" @show="moveSelect === null">
                  <el-radio-group v-model="moveSelect" size="mini">
                    <el-radio :label="g.groupId" border v-for="g in groups" :key="g.id" v-show="g.groupId > 0"
                              :disabled="g.groupId === group.groupId" style="margin: 10px;">{{g.groupName}}</el-radio>
                  </el-radio-group>
                  <div style="text-align: right; margin: 0">
                    <el-button type="primary" size="mini" @click="moveFrom(item)">确定</el-button>
                  </div>
                  <el-button slot="reference" type="text" icon="el-icon-folder" size="mini" >移动到
                  </el-button>
                </el-popover>
              </div>
              <!-- 被禁用的按钮组 -->
              <div v-if="item.isStop">
                <el-button type="text" icon="el-icon-edit-outline" size="mini" @click="editFrom(item, group)">编辑</el-button>
                <el-button type="text" icon="el-icon-check" size="mini" @click="stopFrom(item)">启用</el-button>
                <el-button type="text" icon="el-icon-remove" size="mini" @click="removeForm(item)">删除</el-button>
              </div>

            </div>
          </div>
        </draggable>
        <div style="text-align: center" v-if="(group.items === undefined || group.items.length === 0) && group.groupId===0">
          <p style="color: #C0C4CC">没有分组的流程会显示在此处</p>
        </div>
        <div style="text-align: center" v-if="(group.items === undefined || group.items.length === 0) && group.groupId===-1">
          <p style="color: #C0C4CC">停用的流程会显示在此处</p>
        </div>
        <div style="text-align: center" v-if="(group.items === undefined || group.items.length === 0)&&group.groupId>0">
          <el-button style="padding-top: 0" type="text" icon="el-icon-plus" @click="newProcess(group.groupId)">创建新表单</el-button>
        </div>
      </div>
    </draggable>
  </div>
</template>

<script>
import draggable from "vuedraggable";
import {
  createGroup,
  getFormGroups,
  groupItemsSort,
  groupSort,
  removeForm,
  removeGroup,
  updateForm,
  updateGroup
} from '@/api/design'
import {getToken} from "@/api/auth";

export default {
  name: "FormsPanel",
  components: {draggable},
  data() {
    return {
      moveSelect: '',
      movingGroup: {},
      visible: false,
      groups: []
    }
  },
  created() {
    //重新设置token
    sessionStorage.setItem("token", getToken());
  },
  mounted() {
    this.getGroups()
  },
  methods: {
    getGroups() {
      getFormGroups().then(rsp => {
        this.groups = rsp.data
        //解析图标json
        this.groups.forEach(group => {
          group.items.forEach(item => {
            item.logo = JSON.parse(item.logo)
          })
        })
      });
    },
    newProcess(groupId) {
      this.$store.commit("setTemplate", this.getTemplateData());
      this.$store.commit("setIsEdit", false);
      this.$router.push("/admin/design?_token="+getToken() +"&groupId=" + groupId);//默认为其他分组
    },
    groupSort() {
      if(this.groups.length<=0) {
        return ;
      }
      groupSort(this.groups.map( g => g.groupId)).then(rsp => {
        //this.$message.success(rsp.message)
        this.getGroups()
      }).catch(err => {
        this.getGroups()
        this.$message.error(err.response.message)
      })
    },
    formSort() {
      if(this.movingGroup.items.length<=0) {
        return ;
      }
      groupItemsSort(this.movingGroup.items.map( g => g.formId)).then(rsp => {
        //this.$message.success(rsp.message)
        this.movingGroup = {}
        this.getGroups()
      }).catch(err => {
        this.movingGroup = {}
        this.getGroups()
        this.$message.error(err.response.message)
      })
    },
    addGroup() {
      this.$prompt('请输入要添加的组名', '新的分组名', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPattern: /^[\u4E00-\u9FA5A-Za-z0-9\\-]{1,30}$/,
        inputErrorMessage: '分组名不能为空且长度小于30',
        inputPlaceholder: '请输入分组名'
      }).then(({value}) => {
        createGroup(value).then(rsp => {
          this.$message.success(rsp.message)
          this.getGroups()
        }).catch(err => this.$message.error(err.response.data))
      })
    },
    delGroup(group) {
      if(group.items && group.items.length>0) {
        this.$message.warning('分组下有表单，禁止删除');
        return;
      }
      this.$confirm('确定要删除分组 ' + group.groupName + '?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeGroup( group.groupId).then(rsp => {
          this.$message.success(rsp.message)
          this.getGroups()
        }).catch(err => this.$message.error(err.response.message))
      })
    },
    editGroup(group) {
      this.$prompt('请输入新的组名', '修改分组名', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPattern: /^[\u4E00-\u9FA5A-Za-z0-9\\-]{1,30}$/,
        inputErrorMessage: '分组名不能为空且长度小于30',
        inputPlaceholder: '请输入分组名',
        inputValue: group.name
      }).then(({value}) => {
        updateGroup(group.groupId, {groupId: group.groupId, groupName: value}).then(rsp => {
          this.$message.success(rsp.message)
          this.getGroups()
        }).catch(err => this.$message.error(err.response.data))
      })
    },
    getTemplateData(data, group){
      return data
    },
    removeForm(item) {
      this.$confirm('删除流程后无法恢复，是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        removeForm(item).then(rsp => {
          this.$message.success(rsp.message)
          this.getGroups()
          this.moveSelect = null
        }).catch(err => this.$message.error(err.response.message))
      });
    },
    editFrom(item, group) {
      this.$router.push("/admin/design?code=" + item.formId + "&_token=" + getToken());
    },
    stopFrom(item) {
      if(item.isStop) {
        this.$confirm('启用后将会进入 “其他” 分组，是否继续？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          updateForm(item.formId,{formId: item.formId, groupId: '0'}).then(rsp => {
            this.$message.success(rsp.message)
            this.getGroups()
            this.moveSelect = null
          }).catch(err => this.$message.error(err.response.message))
        })
      }else {
        this.$confirm('流程停用后将会移到 “已停用” 分组，您可以再次启用或者删除它，是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          updateForm(item.formId,{formId: item.formId, groupId: '-1'}).then(rsp => {
            this.$message.success(rsp.message)
            this.getGroups()
            this.moveSelect = null
          }).catch(err => this.$message.error(err.response.message))
        })
      }
    },
    moveFrom(item) {
      if (item.isStop) {//被禁用的流程，禁止移动
        this.$message.warning('停用的流程禁止移动，你可以将其启用后移动')
        return;
      }
      if (this.moveSelect === null || this.moveSelect === ''){
        this.$message.error('请选择分组')
        return;
      }
      updateForm(item.formId,{formId: item.formId, groupId: this.moveSelect}).then(rsp => {
        //this.$message.success(rsp.message)
        this.getGroups()
        this.moveSelect = null
      }).catch(err => this.$message.error(err.response.message))
    },
  }
}
</script>

<style lang="less" scoped>
body {
  background: #ffffff !important;
}

.undrag {
  background: #ebecee !important;
}

.from-panel {
  padding: 50px 100px;
  min-width: 500px;
  background: #ffffff;

  /deep/ .from-title {
    div {
      float: right;

      .el-button {
        border-radius: 15px;
      }
    }
  }

  //height: 100vh;
}

.choose {
  background: #e9ebee;
}

.form-group {
  margin: 20px 0;
  padding: 5px 0px;
  border-radius: 10px;
  //border: 1px solid #d3d3d3;
  box-shadow: 1px 1px 10px 0 #d2d2d2;

  &:hover {
    box-shadow: 1px 1px 12px 0 #b3b3b3;
  }

  .form-group-title {
    padding: 5px 20px;
    height: 40px;
    line-height: 40px;
    border-bottom: 1px solid #d3d3d3;

    .el-icon-rank {
      display: none;
    }

    .form-sort, .group-sort{
      cursor: move;
    }

    &:hover {
      .el-icon-rank {
        display: inline-block;
      }
    }

    div {
      display: inline-block;
      float: right;
    }

    span:first-child {
      margin-right: 5px;
      font-size: 15px;
      font-weight: bold;
    }

    span:nth-child(2) {
      color: #656565;
      font-size: small;
      margin-right: 10px;
    }

    /deep/ .el-button {
      color: #404040;
      margin-left: 20px;

      &:hover {
        color: #2b2b2b;
      }
    }
  }

  .form-group-item:nth-child(1) {
    border-top: none !important;
  }

  .form-group-item {
    color: #3e3e3e;
    font-size: small;
    padding: 10px 0;
    margin: 0 20px;
    height: 40px;
    position: relative;
    line-height: 40px;
    border-top: 1px solid #d3d3d3;

    div {
      display: inline-block;
    }

    i {
      border-radius: 10px;
      padding: 7px;
      font-size: 20px;
      color: #ffffff;
      margin-right: 10px;
    }

    div:nth-child(1) {
      float: left;
    }

    div:nth-child(2) {
      position: absolute;
      color: #7a7a7a;
      font-size: 12px;
      left: 200px;
      max-width: 300px;
      overflow: hidden;
    }

    div:nth-child(3) {
      position: absolute;
      right: 30%;
    }

    div:nth-child(4) {
      float: right;
    }
  }
}

@media screen and (max-width: 1000px) {
  .desp {
    display: none !important;
  }
}

@media screen and (max-width: 800px) {
  .from-panel{
    padding: 50px 10px;
  }
}
</style>
