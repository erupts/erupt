<template>
  <div>
    <el-button icon="el-icon-plus" round size="mini" type="primary" @click="selectOrg">选择抄送人</el-button>
    <div class="option">
      <!--<el-checkbox label="允许发起人添加抄送人" v-model="config.shouldAdd"></el-checkbox>-->
    </div>
    <org-items v-model="select"/>
    <org-picker ref="orgPicker" :selected="select" multiple type="user" @ok="selected"/>
  </div>
</template>

<script>
import OrgPicker from "@/components/common/OrgPicker";
import OrgItems from "../OrgItems";

export default {
  name: "CcNodeConfig.vue",
  components: {OrgPicker, OrgItems},
  props:{
    config:{
      type: Object,
      default: ()=>{
        return {}
      }
    }
  },
  computed:{
    select: {
      get(){
        return this.config.assignedUser || []
      },
      set(val){
        this.config.assignedUser = val
      }
    }
  },
  data() {
    return {}
  },
  methods: {
    selectOrg() {
      this.$refs.orgPicker.show()
    },
    selected(select) {
      this.config.assignedType = "CC";//分配类型变为CC，即抄送人
      this.select = Object.assign([], select)
    },
    removeOrgItem(index){
      this.select.splice(index, 1)
    }
  }
}
</script>

<style lang="less" scoped>
.option{
  color: #606266;
  margin-top: 20px;
  font-size: small;
}

.desc{
  font-size: small;
  color: #8c8c8c;
}
.org-item{
  margin: 5px;
}
</style>
