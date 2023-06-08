<template>
  <!-- 树形选择组件 -->
  <div>
    <el-select class="max-fill" v-show="false" v-model="_value" multiple size="medium" clearable :placeholder="placeholder">
      <el-option v-for="(op, index) in selectOptions" v-if="op" :key="index" :value="_opValue(op)" :label="_opLabel(op)"></el-option>
    </el-select>
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
    this.readOptions(this.options);
    this.handleCheckChange();//刷新一次选中节点
  },
  computed: {
  },
  methods: {
    readOptions(ops) {
      ops.forEach(op => {
        this.selectOptions.push(op);
        if(op.children) {
          this.selectOptions.push(this.readOptions(op.children));
        }
      });
    },
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
      selectOptions: []
    };
  }
}
</script>

<style scoped>
</style>
