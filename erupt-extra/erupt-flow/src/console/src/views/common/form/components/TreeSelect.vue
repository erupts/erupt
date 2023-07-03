<template>
  <!-- 树形选择组件 -->
  <div>
    <div class="buttons">
      <el-button size="small" @click="checkAll">全选</el-button>
      <el-button size="small" @click="resetChecked">清空</el-button>
    </div>
    <el-tree
      :data="options"
      :props="{
        children: 'children',
        label: 'label',
      }"
      default-expand-all
      :expand-on-click-node="false"
      :default-checked-keys="_value"
      show-checkbox
      ref="tree"
      node-key="value"
      @node-click="handleNodeClick"
      @check-change="handleCheckChange"
    >
    </el-tree>
  </div>
</template>

<script>
import componentMinxins from "../ComponentMinxins";

export default {
  mixins: [componentMinxins],
  name: "TreeSelect",
  components: {},
  props:{
    options: {
      type: Array,
      default: () => {
        return []
      }
    },
    value:{
      type: Array,
      default: () => {
        return []
      }
    },
    placeholder:{
      type: String,
      default: '请选择'
    },
  },
  mounted() {
    this.handleCheckChange();//刷新一次选中节点
  },
  computed: {
  },
  methods: {
    handleCheckChange(data, checked, indeterminate) {
      //选中事件
      this._value = this.$refs.tree.getCheckedKeys();
    },
    handleNodeClick(data) {
      //节点单击事件
    },
    checkAll() {
      this.$refs.tree.setCheckedNodes(this.options);
    },
    resetChecked() {
      this.$refs.tree.setCheckedKeys([]);
    }
  },
  data() {
    return {
    };
  }
}
</script>

<style scoped>
</style>
