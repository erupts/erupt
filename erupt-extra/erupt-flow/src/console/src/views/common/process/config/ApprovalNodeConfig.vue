<template>
  <div>
    <el-form label-position="top" label-width="90px">
      <el-form-item label="⚙ 选择审批人" prop="text" class="user-type">
        <el-radio-group v-model="nodeProps.assignedType">
          <el-radio v-for="t in approvalTypes" :label="t.type" :key="t.type">{{ t.name }}</el-radio>
        </el-radio-group>
        <div v-if="nodeProps.assignedType === 'ASSIGN_USER'">
          <el-form-item label="指定人员" prop="text" class="approve-end">
            <el-button size="mini" icon="el-icon-plus" type="primary" @click="openForAssigneeUser" round>选择人员</el-button>
            <org-items v-model="nodeProps.assignedUser"/>
          </el-form-item>
        </div>
        <div v-else-if="nodeProps.assignedType === 'ROLE'">
          <el-form-item label="指定角色" prop="text" class="approve-end">
            <el-button size="mini" icon="el-icon-plus" type="primary" @click="openForAssigneeRole" round>选择角色</el-button>
            <org-items v-model="nodeProps.role"/>
          </el-form-item>
        </div>
        <!--<div v-else-if="nodeProps.assignedType === 'SELF_SELECT'">
          <el-radio-group size="mini" v-model="nodeProps.selfSelect.multiple">
            <el-radio-button :label="false">自选一个人</el-radio-button>
            <el-radio-button :label="true">自选多个人</el-radio-button>
          </el-radio-group>
        </div>-->
        <div v-else-if="nodeProps.assignedType === 'LEADER_TOP'">
          <el-form-item label="审批终点" prop="text" class="approve-end">
            <el-radio-group v-model="nodeProps.leaderTop.endCondition">
              <el-radio label="TOP">直到最上层主管</el-radio>
              <el-radio label="LEAVE">不超过发起人的</el-radio>
            </el-radio-group>
            <div class="approve-end-leave" v-if="nodeProps.leaderTop.endCondition === 'LEAVE'">
              <span>第 </span>
              <el-input-number :min="1" :max="20" :step="1" size="mini" v-model="nodeProps.leaderTop.level"/>
              <span> 级主管</span>
            </div>
          </el-form-item>
        </div>
        <div v-else-if="nodeProps.assignedType === 'LEADER'">
          <el-form-item label="指定主管" prop="text">
            <span>发起人的第 </span>
            <el-input-number :min="1" :max="20" :step="1" size="mini"
                             v-model="nodeProps.leader.level"></el-input-number>
            <span> 级主管</span>
            <div style="color: #409EFF; font-size: small;">👉 直接主管为 第 1 级主管</div>
          </el-form-item>
        </div>
        <div v-else-if="nodeProps.assignedType === 'FORM_USER'">
          <el-form-item label="表单内联系人" prop="text" class="approve-end">
            <el-select style="width: 80%;" size="small" v-model="nodeProps.formUser" placeholder="请选择包含联系人的表单项">
              <el-option v-for="op in forms" :label="op.title" :value="op.id"></el-option>
            </el-select>
          </el-form-item>
        </div>
        <div v-else>
          <span class="item-desc">发起人自己作为审批人进行审批</span>
        </div>
      </el-form-item>

      <el-divider></el-divider>
      <el-form-item label="👤 审批人为空时" prop="text" class="line-mode">
        <el-radio-group v-model="nodeProps.nobody.handler">
          <el-radio label="TO_PASS">自动通过</el-radio>
          <el-radio label="TO_REFUSE">自动驳回</el-radio>
          <el-radio label="TO_ADMIN">转交审批管理员</el-radio>
          <el-radio label="TO_USER">转交到指定人员</el-radio>
        </el-radio-group>

        <div style="margin-top: 10px" v-if="nodeProps.nobody.handler === 'TO_USER'">
          <el-button size="mini" icon="el-icon-plus" type="primary" @click="openForNobodyAssignee" round>选择人员</el-button>
          <org-items v-model="nodeProps.nobody.assignedUser"/>
        </div>

      </el-form-item>

      <div v-if="showMode">
        <el-divider/>
        <el-form-item :label="'👩‍👦‍👦 '+nodeProps.nobody.tips" prop="text" class="approve-mode">
          <el-radio-group v-model="nodeProps.mode">
            <el-radio label="NEXT">依次会签 （按顺序审批，每个人必须同意）</el-radio>
            <el-radio label="AND">同时会签（可同时审批，每个人必须同意）</el-radio>
            <el-radio label="OR">或签（有一人同意即可）</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>

      <el-divider>高级设置</el-divider>
      <!-- ↓↓↓↓这几个功能先不做↓↓↓↓ -->
      <div v-if="false">
      <el-form-item label="✍ 审批同意时是否需要签字" prop="text">
        <el-switch inactive-text="不用" active-text="需要" v-model="nodeProps.sign"></el-switch>
        <el-tooltip class="item" effect="dark" content="如果全局设置了需要签字，则此处不生效" placement="top-start">
          <i class="el-icon-question" style="margin-left: 10px; font-size: medium; color: #b0b0b1"></i>
        </el-tooltip>
      </el-form-item>
      <el-form-item label="⏱ 审批期限（为 0 则不生效）" prop="timeLimit">
        <el-input style="width: 180px;" placeholder="时长" size="small" type="number"
                  v-model="nodeProps.timeLimit.timeout.value">
          <el-select style="width: 75px;" v-model="nodeProps.timeLimit.timeout.unit" slot="append" placeholder="请选择">
            <el-option label="天" value="D"></el-option>
            <el-option label="小时" value="H"></el-option>
          </el-select>
        </el-input>
      </el-form-item>
      <el-form-item label="审批期限超时后执行" prop="level" v-if="nodeProps.timeLimit.timeout.value > 0">
        <el-radio-group v-model="nodeProps.timeLimit.handler.type">
          <el-radio label="PASS">自动通过</el-radio>
          <el-radio label="REFUSE">自动驳回</el-radio>
          <el-radio label="NOTIFY">发送提醒</el-radio>
        </el-radio-group>
        <div v-if="nodeProps.timeLimit.handler.type === 'NOTIFY'">
          <div style="color:#409EEF; font-size: small">默认提醒当前审批人</div>
          <el-switch inactive-text="循环" active-text="一次" v-model="nodeProps.timeLimit.handler.notify.once"></el-switch>
          <span style="margin-left: 20px" v-if="!nodeProps.timeLimit.handler.notify.once">
							每隔
							<el-input-number :min="0" :max="10000" :step="1" size="mini"
                               v-model="nodeProps.timeLimit.handler.notify.hour"/>
							小时提醒一次
						</span>
        </div>
      </el-form-item>
      </div>
      <!-- ↑↑↑↑这几个功能先不做↑↑↑↑ -->
      <el-form-item label="🙅‍ 如果审批被驳回 👇">
        <el-radio-group v-model="nodeProps.refuse.type">
          <el-radio label="TO_END">直接结束流程</el-radio>
          <el-radio label="TO_BEFORE">驳回到上级审批节点</el-radio>
          <el-radio label="TO_NODE">驳回到指定节点</el-radio>
        </el-radio-group>
        <div v-if="nodeProps.refuse.type === 'TO_NODE'">
          <span>指定节点:</span>
          <el-select style="margin-left: 10px; width: 150px;" placeholder="选择跳转步骤" size="small" v-model="nodeProps.refuse.target">
            <el-option v-for="(node, i) in nodeOptions" :key="i" :label="node.name" :value="node.id"></el-option>
          </el-select>
        </div>
      </el-form-item>
    </el-form>
    <!-- 人员选择 -->
    <org-picker multiple :type="orgPickerType" :selected="orgPickerChecked" ref="orgPicker" @ok="orgPickerOk"/>
  </div>
</template>

<script>
import OrgPicker from "@/components/common/OrgPicker";
import OrgItems from "../OrgItems";

export default {
  name: "ApprovalNodeConfig",
  components: {OrgPicker, OrgItems},
  props: {
    config: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      orgPickerType: 'user',
      orgPickerChecked: [],
      orgPickerMod: null,// user 选择审批用户 role 选择审批角色 nobodyUser 选择无人处理时的审批人
      approvalTypes: [
        {name: '指定人员', type: 'ASSIGN_USER'},
        {name: '指定角色', type: 'ROLE'},
        //{name: '发起人自选', type: 'SELF_SELECT'},
        {name: '发起人自己', type: 'SELF'},
        {name: '连续多级主管', type: 'LEADER_TOP'},
        {name: '主管', type: 'LEADER'},
        {name: '表单内联系人', type: 'FORM_USER'}
      ]
    }
  },
  computed: {
    nodeProps() {
      return this.$store.state.selectedNode.props
    },
    forms() {//筛选出表单中的用户选择组件
      return this.$store.state.design.formItems.filter(f => {
        return f.name === 'UserPicker'
      })
    },
    nodeOptions() {//筛选跳转的目标节点
      let values = []
      const excType = ['ROOT', 'EMPTY', "CONDITION", "CONDITIONS", "CONCURRENT", "CONCURRENTS"]
      this.$store.state.nodeMap.forEach((v) => {
        if (excType.indexOf(v.type) === -1) {
          values.push({id: v.id, name: v.name})
        }
      })
      return values
    },
    showMode() {//是否需要展示多人会签
      switch (this.nodeProps.assignedType) {
        case "ASSIGN_USER"://指定多名用户
          this.nodeProps.nobody.tips = "指定多人时";
          return this.nodeProps.assignedUser.length > 0;
        case "SELF_SELECT"://发起人自选多人
          this.nodeProps.nobody.tips = "多人审批时";
          return this.nodeProps.selfSelect.multiple;
        case "LEADER_TOP"://连续多级主管，如果某一级主管有多人时，需要这个配置
          this.nodeProps.nobody.tips = "部门主管为多人时";
          return true;
        case "FORM_USER":
          this.nodeProps.nobody.tips = "表单联系人选择多人时";
          return true;
        case "ROLE":
          this.nodeProps.nobody.tips = "角色下有多人时";
          return true;
        default:
          return false;
      }
    }
  },
  methods: {
    openForAssigneeUser() {
      this.orgPickerMod = 'user';//选择审批人
      this.orgPickerType = "user";
      this.orgPickerChecked = this.config.assignedUser || [];
      console.log(this.orgPickerMod, this.orgPickerType, this.orgPickerChecked)
      this.$nextTick(() => {
        this.$refs.orgPicker.show();
      });
    },
    openForAssigneeRole() {
      this.orgPickerMod = 'role';//选择审批角色
      this.orgPickerType = "role";
      this.orgPickerChecked = this.config.role || [];
      this.$nextTick(() => {
        this.$refs.orgPicker.show();
      });
    },
    openForNobodyAssignee() {
      this.orgPickerMod = 'nobodyUser';//选择无人处理时的审批人
      this.orgPickerType = "user";
      this.orgPickerChecked = this.config.nobody.assignedUser || [];
      this.$nextTick(() => {
        this.$refs.orgPicker.show();
      });
    },
    orgPickerOk(list) {
      if("user"===this.orgPickerMod) {
        this.config.assignedUser.length = 0;
        list.forEach(val => this.config.assignedUser.push(val))
      }
      if("role"===this.orgPickerMod) {
        this.config.role.length = 0;
        list.forEach(val => this.config.role.push(val))
      }
      if("nobodyUser"===this.orgPickerMod) {
        this.config.nobody.assignedUser.length = 0;
        list.forEach(val => this.config.nobody.assignedUser.push(val))
      }
    },
    removeOrgItem(index) {
      this.select.splice(index, 1)
    }
  }
}
</script>

<style lang="less" scoped>
.user-type {
  /deep/ .el-radio {
    width: 110px;
    margin-top: 10px;
    margin-bottom: 20px;
  }
}

/deep/ .line-mode {
  .el-radio {
    width: 150px;
    margin: 5px;
  }
}

/deep/ .el-form-item__label {
  line-height: 25px;
}

/deep/ .approve-mode {
  .el-radio {
    float: left;
    width: 100%;
    display: block;
    margin-top: 15px;
  }
}

/deep/ .approve-end {
  position: relative;

  .el-radio-group {
    width: 160px;
  }

  .el-radio {
    margin-bottom: 5px;
    width: 100%;
  }

  .approve-end-leave {
    position: absolute;
    bottom: -5px;
    left: 150px;
  }
}

/deep/ .el-divider--horizontal {
  margin: 10px 0;
}
</style>
