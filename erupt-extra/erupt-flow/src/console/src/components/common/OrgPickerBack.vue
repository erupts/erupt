<template>
  <w-dialog :border="false" closeFree width="600px" @ok="selectOk" :title="_title" v-model="visible">
    <div class="picker">
      <div class="candidate" v-loading="loading">
        <div v-if="type !== 'role'">
          <el-input v-model="search" @input="searchUser" style="width: 95%;" size="small"
                    clearable placeholder="搜索" prefix-icon="el-icon-search"/>
          <div v-show="!showUsers">
            <ellipsis hoverTip style="height: 18px; color: #8c8c8c; padding: 5px 0 0" :row="1" :content="deptStackStr">
              <i slot="pre" class="el-icon-office-building"></i>
            </ellipsis>
            <div style="margin-top: 5px">
              <el-checkbox v-model="checkAll" @change="handleCheckAllChange" v-if="multiple">全选</el-checkbox>
              <span v-show="deptStack.length > 0" class="top-dept" @click="beforeNode">上一级</span>
            </div>
          </div>
        </div>
        <div class="role-header" v-else>
          <div>系统角色</div>
        </div>
        <div class="org-items" :style="type === 'role' ? 'height: 350px':''">
          <el-empty :image-size="100" description="似乎没有数据" v-show="orgs.length === 0"/>
          <div v-for="(org, index) in orgs" :key="index" :class="orgItemClass(org)">
            <el-checkbox v-model="org.selected" v-show="showCheckbox(org)" @change="selectChange(org)"></el-checkbox>
            <div v-if="org.type === 'dept'">
              <i class="el-icon-folder-opened"></i>
              <span class="name">{{ org.name }}</span>
              <span @click.stop="org.selected?'':nextNode(org)" :class="`next-dept${org.selected ? '-disable':''}`">
                <i class="iconfont icon-map-site"></i>
              </span>
            </div>
            <div v-else-if="org.type === 'user'" style="display: flex; align-items: center">
              <el-avatar :size="35" :src="org.avatar" v-if="$isNotEmpty(org.avatar)"/>
              <span v-else class="avatar">{{getShortName(org.name)}}</span>
              <span class="name">{{ org.name }}</span>
            </div>
            <div style="display: inline-block" v-else>
              <i class="iconfont icon-bumen"></i>
              <span class="name">{{ org.name }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="selected">
        <div class="count">
          <span>已选 {{ select.length }} 项</span>
          <span @click="clearSelected">清空</span>
        </div>
        <div class="org-items" style="height: 350px;">
          <el-empty :image-size="100" description="请点击左侧列表选择数据" v-show="select.length === 0"/>
          <div v-for="(org, index) in select" :key="index" :class="orgItemClass(org)" >
            <div v-if="org.type === 'dept'">
              <i class="el-icon-folder-opened"></i>
              <span style="position: static" class="name">{{ org.name }}</span>
            </div>
            <div v-else-if="org.type === 'user'" style="display: flex; align-items: center">
              <el-avatar :size="35" :src="org.avatar" v-if="$isNotEmpty(org.avatar)"/>
              <span v-else class="avatar">{{getShortName(org.name)}}</span>
              <span class="name">{{ org.name }}</span>
            </div>
            <div v-else>
              <i class="iconfont icon-bumen"></i>
              <span class="name">{{ org.name }}</span>
            </div>
            <i class="el-icon-close" @click="noSelected(index)"></i>
          </div>
        </div>
      </div>
    </div>
  </w-dialog>
</template>

<script>
import {getOrgTree, getOrgTreeUser, getRole} from '@/api/org'

export default {
  name: "OrgPicker",
  components: {},
  props: {
    title: {
      default: '请选择',
      type: String
    },
    type: {//user-选人  dept-选部门 role-选角色
      type: String,
      required: true
    },
    multiple: { //是否多选
      default: false,
      type: Boolean
    },
    selected: {
      default: () => {
        return []
      },
      type: Array
    },
  },
  data() {
    return {
      visible: false,
      loading: false,
      checkAll: false,
      nowDeptId: null,
      isIndeterminate: false,
      searchUsers: [],
      nodes: [],
      select: [],
      search: '',
      deptStack: []
    }
  },
  computed: {
    _title() {
      if(this.type==='user') {
        return "请选择用户" + (this.multiple?"[多选]":"[单选]")
      }else if(this.type==='dept') {
        return "请选择部门" + (this.multiple?"[多选]":"[单选]")
      }else if(this.type==='role') {
        return "请选择角色" + (this.multiple?"[多选]":"[单选]")
      }else {
        return "-"
      }
    },
    deptStackStr() {
      return String(this.deptStack.map(v => v.name)).replaceAll(',', ' > ')
    },
    orgs() {
      return !this.search || this.search.trim() === '' ? this.nodes : this.searchUsers
    },
    showUsers(){
      return this.search || this.search.trim() !== ''
    },
  },
  methods: {
    show() {
      this.visible = true
      this.init()
      this.getDataList()
    },
    showCheckbox(node) {
      //只有类型和组件类型符合才可以多选
      return this.type === node.type;
    },
    orgItemClass(org){
      return {
        'org-item': true,
        'org-dept-item': org.type === 'dept',
        'org-user-item': org.type === 'user',
        'org-role-item': org.type === 'role'
      }
    },
    disableDept(node) {
      return this.type === 'user' && 'dept' === node.type
    },
    getDataList() {
      this.loading = true
      if(this.type==='user') {
        getOrgTreeUser({deptId: this.nowDeptId, keywords: this.search}).then(rsp => {
          this.loading = false
          this.nodes = rsp.data
          this.selectToLeft()
        })
        return "请选择用户"
      }else if(this.type==='dept') {
        getOrgTree({deptId: this.nowDeptId, keywords: this.search}).then(rsp => {
          this.loading = false
          this.nodes = rsp.data
          this.selectToLeft()
        })
      }else if(this.type==='role') {
        getRole({deptId: this.nowDeptId, keywords: this.search}).then(rsp => {
          this.loading = false
          this.nodes = rsp.data
          this.selectToLeft()
        })
      }
    },
    getShortName(name) {
      if (name) {
        return name.length > 2 ? name.substring(1, 3) : name;
      }
      return '**'
    },
    searchUser() {
      let userName = this.search.trim()
      this.searchUsers = []
      this.loading = true
      getUserByName({userName: userName}).then(rsp => {
        this.loading = false
        this.searchUsers = rsp.data
        this.selectToLeft()
      }).catch(err => {
        this.loading = false
        this.$message.error("接口异常")
      })
    },
    selectToLeft() {
      let nodes = this.search.trim() === '' ? this.nodes : this.searchUsers;
      nodes.forEach(node => {
        for (let i = 0; i < this.select.length; i++) {
          if (this.select[i].id === node.id) {
            node.selected = true;
            break;
          } else {
            node.selected = false;
          }
        }
      })
    },
    selectChange(node) {
      if (node.selected) {
        this.checkAll = false;
        for (let i = 0; i < this.select.length; i++) {
          if (this.select[i].id === node.id) {
            this.select.splice(i, 1);
            break;
          }
        }
        node.selected = false;
      } else if (!this.disableDept(node)) {
        node.selected = true
        let nodes = this.search.trim() === '' ? this.nodes : this.searchUsers;
        if (!this.multiple) {
          nodes.forEach(nd => {
            if (node.id !== nd.id) {
              nd.selected = false
            }
          })
        }
        if (node.type === 'dept') {
          if (!this.multiple) {
            this.select = [node]
          } else {
            this.select.unshift(node);
          }
        } else {
          if (!this.multiple) {
            this.select = [node]
          } else {
            this.select.push(node);
          }
        }
      }
    },
    noSelected(index) {
      let nodes = this.nodes;
      for (let f = 0; f < 2; f++) {
        for (let i = 0; i < nodes.length; i++) {
          if (nodes[i].id === this.select[index].id) {
            nodes[i].selected = false;
            this.checkAll = false;
            break;
          }
        }
        nodes = this.searchUsers;
      }
      this.select.splice(index, 1)
    },
    handleCheckAllChange() {
      this.nodes.forEach(node => {
        if (this.checkAll) {
          if (!node.selected && !this.disableDept(node)) {
            node.selected = true
            this.select.push(node)
          }
        } else {
          node.selected = false;
          for (let i = 0; i < this.select.length; i++) {
            if (this.select[i].id === node.id) {
              this.select.splice(i, 1);
              break;
            }
          }
        }
      })
    },
    nextNode(node) {
      this.nowDeptId = node.id
      this.deptStack.push(node)
      this.getDataList()
    },
    beforeNode() {
      if (this.deptStack.length === 0) {
        return;
      }
      if (this.deptStack.length < 2) {
        this.nowDeptId = null
      } else {
        this.nowDeptId = this.deptStack[this.deptStack.length - 2].id
      }
      this.deptStack.splice(this.deptStack.length - 1, 1);
      this.getDataList()
    },
    recover() {
      this.select = []
      this.nodes.forEach(nd => nd.selected = false)
    },
    selectOk() {
      this.$emit('ok', Object.assign([], this.select.map(v => {
        v.avatar = undefined
        return v
      })))
      this.visible = false
      this.recover()
    },
    clearSelected(){
      this.$confirm('您确定要清空已选中的项?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.recover()
      })
    },
    close() {
      this.$emit('close')
      this.recover()
    },
    init() {
      this.checkAll = false;
      this.nowDeptId = null;
      this.deptStack = [];
      this.nodes = []
      this.select = Object.assign([], this.selected)
      this.selectToLeft()
    }
  }
}
</script>

<style lang="less" scoped>

@containWidth: 278px;

.candidate, .selected {
  position: absolute;
  display: inline-block;
  width: @containWidth;
  height: 400px;
  border: 1px solid #e8e8e8;
}

.picker {
  height: 402px;
  position: relative;
  text-align: left;
  .candidate {
    left: 0;
    top: 0;

    .role-header{
      padding: 10px !important;
      margin-bottom: 5px;
      border-bottom: 1px solid #e8e8e8;
    }

    .top-dept{
      margin-left: 20px;
      cursor: pointer;
      color:#38adff;
    }
    .next-dept {
      float: right;
      color: @theme-primary;
      cursor: pointer;
    }

    .next-dept-disable {
      float: right;
      color: #8c8c8c;
      cursor: not-allowed;
    }

    & > div:first-child {
      padding: 5px 10px;
    }

  }

  .selected {
    right: 0;
    top: 0;
  }

  .org-items {
    overflow-y: auto;
    height: 310px;

    .el-icon-close {
      position: absolute;
      right: 5px;
      cursor: pointer;
      font-size: larger;
    }
    .org-dept-item {
      padding: 10px 5px;

      & > div {
        display: inline-block;

        & > span:last-child {
          position: absolute;
          right: 5px;
        }
      }
    }

    .org-role-item {
      display: flex;
      align-items: center;
      padding: 10px 5px;
    }

    /deep/ .org-user-item {
      display: flex;
      align-items: center;
      padding: 5px;

      & > div {
        display: inline-block;
      }

      .avatar {
        width: 35px;
        text-align: center;
        line-height: 35px;
        background: @theme-primary;
        color: white;
        border-radius: 50%;
      }
    }

    /deep/ .org-item {
      margin: 0 5px;
      border-radius: 5px;
      position: relative;
      cursor: pointer;
      .el-checkbox {
        margin-right: 10px;
      }

      .name {
        margin-left: 5px;
      }

      &:hover {
        background: #f1f1f1;
      }
    }
  }
}

.selected {
  border-left: none;

  .count {
    width: calc(@containWidth - 20px);
    padding: 10px;
    display: inline-block;
    border-bottom: 1px solid #e8e8e8;
    margin-bottom: 5px;
    & > span:nth-child(2) {
      float: right;
      color: #c75450;
      cursor: pointer;
    }
  }
}

/deep/ .el-dialog__body {
  padding: 10px 20px;
}

.disabled{
  cursor: not-allowed !important;
  color: #8c8c8c !important;
}

::-webkit-scrollbar {
  float: right;
  width: 4px;
  height: 4px;
  background-color: white;
}

::-webkit-scrollbar-thumb {
  border-radius: 16px;
  background-color: #efefef;
}
</style>
