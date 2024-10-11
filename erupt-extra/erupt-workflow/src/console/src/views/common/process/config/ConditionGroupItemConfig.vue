<template>
  <div>
    <div v-for="(group, index) in selectedNode.props.groups" :key="index + '_g'" class="group">
      <div class="group-header">
        <span class="group-name">条件组 {{ groupNames[index] }}</span>
        <div class="group-cp">
          <span>组内条件关系：</span>
          <el-switch v-model="group.groupType" active-color="#409EFF"
                     active-text="且" active-value="AND" inactive-color="#c1c1c1"
                     inactive-text="或" inactive-value="OR"/>
        </div>
        <div class="group-operation">
          <el-popover placement="bottom" title="选择审批条件" trigger="click" width="300">
            <!-- <div>以下条件将决定具体的审批流程</div>-->
            <el-checkbox-group v-model="group.cids" value-key="id">
              <el-checkbox v-for="(condition, cindex) in conditionList" :key="condition.id" :label="condition.id" @change="conditionChange(cindex, group)">
                {{ condition.title }}
              </el-checkbox>
            </el-checkbox-group>
            <i slot="reference" class="el-icon-plus"></i>
          </el-popover>
          <i class="el-icon-delete" @click="delGroup(index)"></i>
        </div>
      </div>
      <div class="group-content">
        <p v-if="group.conditions.length === 0">点击右上角 + 为本条件组添加条件 ☝</p>
        <div v-else>
          <el-form ref="condition-form" label-width="100px">
            <!--构建表达式-->
            <el-form-item  v-for="(condition, cindex) in group.conditions" :key="condition.id + '_' + cindex" >
              <ellipsis slot="label" :content="condition.title" hover-tip/>
               <span v-if="condition.valueType === ValueType.string">
                <el-select v-model="condition.compare" placeholder="判断符" size="small" style="width: 120px;" @change="condition.value = []">
                  <el-option label="等于" value="="></el-option>
                  <el-option v-if="getOptions(condition.id).length>0" label="包含在" value="IN"></el-option>
                </el-select>
                 <span v-if="isSelect(condition.id)" style="margin-left: 10px">
                   <el-select v-if="condition.compare === 'IN'" v-model="condition.value" clearable multiple placeholder="选择值" size="small" style="width: 280px;">
                     <el-option v-for="(option, oi) in getOptions(condition.id)" :key="oi" :label="option" :value="option"></el-option>
                   </el-select>
                   <el-select v-else v-model="condition.value[0]" clearable placeholder="选择值" size="small" style="width: 280px;">
                     <el-option v-for="(option, oi) in getOptions(condition.id)" :key="oi" :label="option" :value="option"></el-option>
                   </el-select>
                 </span>
                 <span v-else style="margin-left: 10px">
                   <el-input v-if="condition.compare === '='" v-model="condition.value[0]" placeholder="输入比较值" size="small" style="width: 280px;"/>
                   <el-select v-else v-model="condition.value" allow-create clearable filterable multiple placeholder="输入可能包含的值" size="small" style="width: 280px;"></el-select>
                 </span>
              </span>
              <span v-else-if="condition.valueType === ValueType.number">
                <el-select v-model="condition.compare" placeholder="判断符" size="small" style="width: 120px;">
                  <el-option v-for="exp in explains" :key="exp.value" :label="exp.label" :value="exp.value"></el-option>
                </el-select>
                <span style="margin-left: 10px">
                  <el-input v-if="conditionValType(condition.compare) === 0" v-model="condition.value[0]" placeholder="输入比较值" size="small" style="width: 280px;" type="number"/>
                  <el-select v-else-if="conditionValType(condition.compare) === 1" v-model="condition.value" allow-create filterable multiple placeholder="输入可能包含的值" size="small" style="width: 280px;"></el-select>
                  <span v-else>
                    <el-input v-model="condition.value[0]" placeholder="输入比较值" size="small" style="width: 130px;" type="number"/>
                    <span> ~
                      <el-input v-model="condition.value[1]" placeholder="输入比较值" size="small" style="width: 130px;" type="number"/>
                    </span>
                  </span>
                </span>
              </span>

              <span v-else-if="condition.valueType === ValueType.user">
                <!--<span class="item-desc" style="margin-right: 20px">属于某部门 / 为某些人其中之一</span>-->
                <el-select v-model="condition.compare" placeholder="判断符" size="small" style="width: 120px; margin-right: 10px;">
                  <el-option label="为某些人其中之一" value="user"/>
                  <el-option label="为某部门或其下属部门之一" value="dept"></el-option>
                  <el-option label="为某角色其中之一" value="role"></el-option>
                </el-select>
                <el-button icon="el-icon-plus" round size="mini" type="primary" @click="selectUser(condition.value, condition.compare)">选择范围</el-button>
                <org-items v-model="condition.value"/>
              </span>

              <span v-else-if="condition.valueType === ValueType.dept">
                <el-select v-model="condition.compare" placeholder="判断符" size="small" style="width: 120px; margin-right: 10px;">
                  <el-option label="为某部门或其下属部门之一" value="dept"></el-option>
                </el-select>
                <!--<span class="item-desc" style="margin-right: 20px">为某部门 / 某部门下的部门</span>-->
                <el-button icon="el-icon-plus" round size="mini" type="primary" @click="selectUser(condition.value, 'dept')">选择部门</el-button>
                <org-items v-model="condition.value"/>
              </span>

              <span v-else-if="condition.valueType === ValueType.date">
                <el-select v-model="condition.compare" placeholder="判断符" size="small" style="width: 120px;">
                  <el-option v-for="exp in explains" :key="exp.value" :label="exp.label" :value="exp.value"></el-option>
                </el-select>
                <span style="margin-left: 10px">
                  <el-date-picker v-if="conditionValType(condition.compare) === 0" v-model="condition.value[0]" placeholder="输入比较值" size="small" style="width: 280px;" type="date" value-format="yyyy-MM-dd"/>
                  <span v-else>
                    <el-date-picker v-model="condition.value[0]" placeholder="输入比较值" size="small" style="width: 130px;" type="date" value-format="yyyy-MM-dd"/>
                    <span> ~
                      <el-date-picker v-model="condition.value[1]" placeholder="输入比较值" size="small" style="width: 130px;" type="date" value-format="yyyy-MM-dd"/>
                    </span>
                  </span>
                </span>
              </span>

              <i class="el-icon-delete" @click="rmSubCondition(group, cindex)"></i>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    <org-picker ref="orgPicker" :selected="users" :type="orgType" multiple @ok="selected"></org-picker>
  </div>
</template>

<script>
import OrgPicker from "@/components/common/OrgPicker";
import OrgItems from '../OrgItems'
import {ValueType} from '@/views/common/form/ComponentsConfigExport'

export default {
  name: "ConditionGroupItemConfig",
  components: {OrgPicker, OrgItems},
  data() {
    return {
      ValueType,
      users: [],
      orgType: 'user',
      showOrgSelect: false,
      //groupConditions: [],
      groupNames: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'],
      supportTypes:[ValueType.number, ValueType.string, ValueType.date, ValueType.dept, ValueType.user],//这些字段可以用作条件
      explains:[
        {label: '等于', value:'='},
        {label: '大于', value:'>'},
        {label: '大于等于', value:'>='},
        {label: '小于', value:'<'},
        {label: '小于等于', value:'<='},
        {label: '包含在', value:'IN'},
        {label: 'x < 值 < x', value:'B'},
        {label: 'x ≤ 值 < x', value:'AB'},
        {label: 'x < 值 ≤ x', value:'BA'},
        {label: 'x ≤ 值 ≤ x', value:'ABA'},
      ]
    }
  },
  computed: {
    selectedNode() {
      return this.$store.state.selectedNode
    },
    select() {
      return this.selectedNode.props.assignedUser || []
    },
    formItems(){
      return this.$store.state.design.formItems
    },
    formMap(){
      const map = new Map();
      this.formItems.forEach(item => this.itemToMap(map, item))
      return map
    },
    conditionList() {
      //构造可用条件选项
      const conditionItems = []
      this.formItems.forEach(item => this.filterCondition(item, conditionItems))
      if (conditionItems.length === 0 || conditionItems[0].id !== 'root'){
        conditionItems.unshift({id: 'root', title: '发起人', valueType: 'User'})
      }
      return conditionItems
    }
  },
  methods: {
    itemToMap(map, item){
      map.set(item.id, item)
      if (item.name === 'SpanLayout'){
        item.props.items.forEach(sub => this.itemToMap(map, sub))
      }
    },
    isSelect(formId){
      let form = this.formMap.get(formId)
      if (form && (form.name === 'SelectInput' || form.name === 'MultipleSelect')){
        return true
      }
      return false
    },
    getOptions(formId){
      return this.formMap.get(formId).props.options || []
    },
    conditionValType(type){
      switch (type){
        case '=':
        case '>':
        case '>=':
        case '<':
        case '<=': return 0;
        case 'IN': return 1;
        default: return 2;
      }
    },
    selectUser(value, orgType) {
      if(this.orgType === orgType) {
        this.users = value;
      }else {
        value.length = 0;//清空选项
        this.users = value;
      }
      this.orgType = orgType
      this.$nextTick(() => {//必须赋值完毕之后打开
        this.$refs.orgPicker.show()
      })
    },
    filterCondition(item, list){
      if (item.name === 'SpanLayout'){
        item.props.items.forEach(sub => this.filterCondition(sub, list))
      }else if (this.supportTypes.indexOf(item.valueType) > -1 && item.props.required){
        list.push({title: item.title, id: item.id, valueType: item.valueType})
      }
    },
    selected(select) {
      this.users.length = 0
      select.forEach(u => this.users.push(u));
    },
    delGroup(index) {
      this.selectedNode.props.groups.splice(index, 1)
    },
    rmSubCondition(group, index){
      group.cids.splice(index, 1)
      group.conditions.splice(index, 1)
    },
    conditionChange(index, group) {
      //判断新增的
      group.cids.forEach(cid => {
        if (0 > group.conditions.findIndex(cd => cd.id === cid)){
          //新增条件
          let condition = {...this.conditionList[index]}
          condition.compare = '';
          condition.value = []
          group.conditions.push(condition)
        }
      })
      for (let i = 0; i < group.conditions.length; i++) {
        //去除没有选中的
        if (group.cids.indexOf(group.conditions[i].id) < 0){
          group.conditions.splice(i, 1)
        }
      }
    }
  }
}
</script>

<style lang="less" scoped>
.group {
  margin-bottom: 20px;
  color: #5e5e5e;
  overflow: hidden;
  border-radius: 6px;
  border: 1px solid #e3e3e3;

  .group-header {
    padding: 5px 10px;
    background: #e3e3e3;
    position: relative;

    div {
      display: inline-block;
    }

    .group-name {
      font-size: small;
    }

    .group-cp {
      font-size: small;
      position: absolute;
      left: 100px;
      display: flex;
      top: 5px;
      justify-content: center;
      align-items: center;
    }

    .group-operation {
      position: absolute;
      right: 10px;

      i {
        padding: 0 10px;

        &:hover {
          cursor: pointer;
        }
      }
    }
  }

  .group-content{
    padding: 10px 5px;
    p{
      text-align: center;
      font-size: small;
    }
    .el-icon-delete{
      position: absolute;
      cursor: pointer;
      top: 12px;
      right: 0;
    }
  }

  .condition-title{
    display: block;
    width: 100px;
  }
}
</style>
