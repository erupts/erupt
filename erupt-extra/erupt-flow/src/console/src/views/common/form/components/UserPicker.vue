<template>
  <div style="max-width: 350px">
    <div v-if="mode === 'DESIGN'">
      <el-button disabled icon="el-icon-user" type="primary" size="mini" round>选择人员</el-button>
      <span class="placeholder"> {{placeholder}}</span>
    </div>
    <div v-else>
      <div v-if="!formDisable || _value.length<=0">
        <el-button :disabled="formDisable" icon="el-icon-user" type="primary" size="mini" round @click="$refs.orgPicker.show()">选择人员</el-button>
        <org-picker type="user" :multiple="multiple" ref="orgPicker" :selected="_value" @ok="selected"/>
        <span class="placeholder"> {{placeholder}}</span>
      </div>
      <div style="margin-top: 5px">
        <el-tag style="margin: 5px;" :closable="!formDisable" v-for="(dept, i) in _value" @close="delDept(i)">{{dept.name}}</el-tag>
      </div>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'
import OrgPicker from "@/components/common/OrgPicker";

export default {
  mixins: [componentMinxins],
  name: "DeptPicker",
  components: {OrgPicker},
  props: {
    value:{
      type: Array,
      default: () => {
        return []
      }
    },
    placeholder: {
      type: String,
      default: '请选择人员'
    },
    multiple:{
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showOrgSelect: false
    }
  },
  methods: {
    selected(values){
      this.showOrgSelect = false
      this._value = values
    },
    delDept(i){
      this._value.splice(i, 1)
    }
  }
}
</script>

<style scoped>
.placeholder{
  margin-left: 10px;
  color: #adabab;
  font-size: smaller;
}
</style>
