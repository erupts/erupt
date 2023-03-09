<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <draggable class="l-drag-from" :list="_columns" group="form"
                 :options="{animation: 300, chosenClass:'choose', sort:true}"
                 @start="drag = true; selectFormItem = null" @end="drag = false">
        <div v-for="(cp, id) in _columns" :key="id" class="l-form-item" @click.stop="selectItem(cp)" :style="getSelectedClass(cp)">
          <div class="l-form-header">
            <p><span v-if="cp.props.required">*</span>{{ cp.title }}</p>
            <div class="l-option">
              <i class="el-icon-close" @click="delItem(id)"></i>
            </div>
            <form-design-render :config="cp"/>
          </div>
        </div>
      </draggable>
      <div style="color: #c0bebe;text-align: center; width: 90%; padding: 5px;">☝ 拖拽控件到表格内部</div>
    </div>
    <div v-else>
      <div v-if="rowLayout">
        <el-table size="medium" :header-cell-style="{background:'#f5f7fa', padding:'3px 0'}" :border="showBorder" :data="_value" style="width: 100%">
          <el-table-column fixed type="index" label="序号" width="50"></el-table-column>
          <el-table-column :min-width="getMinWidth(column)" v-for="(column, index) in _columns" :prop="column.id" :label="column.title">
            <template slot-scope="scope">
              <form-design-render :class="{'valid-error': showError(column, _value[scope.$index][column.id])}" v-model="_value[scope.$index][column.id]" :mode="mode" :config="column"/>
            </template>
          </el-table-column>
          <el-table-column fixed="right" min-width="90" label="操作">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="copyData(scope.$index, scope.row)">复制</el-button>
              <el-button size="mini" type="text" @click="delRow(scope.$index, scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button size="small" icon="el-icon-plus" @click="addRow">{{placeholder}}</el-button>
      </div>
      <div v-else>
        <el-form :rules="rules" :model="row" :ref="`table-form-${i}`" class="table-column" v-for="(row, i) in _value" :key="i">
          <div class="table-column-action">
            <span>第 {{i + 1}} 项</span>
            <i class="el-icon-close" @click="delRow(i, row)"></i>
          </div>
          <el-form-item v-for="(column, index) in _columns" :key="'column_' + index" :prop="column.id" :label="column.title">
            <form-design-render v-model="row[column.id]" :mode="mode" :config="column"/>
          </el-form-item>
        </el-form>
        <el-button size="small" icon="el-icon-plus" @click="addRow">{{placeholder}}</el-button>
      </div>
    </div>
  </div>

</template>

<script>
import draggable from "vuedraggable";
import {ValueType} from "../ComponentsConfigExport";
import FormDesignRender from '@/views/admin/layout/form/FormDesignRender'
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "TableList",
  components: {draggable, FormDesignRender},
  props: {
    value:{
      type: Array,
      default: () => {
        return []
      }
    },
    placeholder: {
      type: String,
      default: '添加数据'
    },
    columns: {
      type: Array,
      default: () => {
        return []
      }
    },
    showBorder: {
      type: Boolean,
      default: true
    },
    maxSize: {
      type: Number,
      default: 0
    },
    rowLayout: {
      type: Boolean,
      default: true
    },
  },
  created() {
    if (!Array.isArray(this.value)){
      this._value = []
    }
  },
  computed: {
    rules(){
      const rules = {}
      this.columns.forEach(col => {
        if (col.props.required){
          rules[col.id] = [{
            type: col.valueType === 'Array' ? 'array':undefined,
            required: true,
            message: `请填写${col.title}`, trigger: 'blur'
          }]
        }
      })
      return rules
    },
    _columns: {
      get() {
        return this.columns;
      },
      set(val) {
        this.columns = val;
      }
    },
    selectFormItem: {
      get() {
        return this.$store.state.selectFormItem
      },
      set(val) {
        this.$store.state.selectFormItem = val
      },
    }
  },
  data() {
    return {
      select: null,
      drag: false,
      ValueType
    }
  },
  methods: {
    getMinWidth(col){
      switch (col.name){
        case 'DateTime': return '250px'
        case 'DateTimeRange': return '280px'
        case 'MultipleSelect': return '200px'
        default: return '150px'
      }
    },
    showError(col, val){
      if (col.props.required){
        switch (col.valueType){
          case ValueType.dept:
          case ValueType.user:
          case ValueType.dateRange:
          case ValueType.array: return !(Array.isArray(val) && val.length > 0)
          default: return !this.$isNotEmpty(val)
        }
      }
      return false
    },
    copyData(i, row){
      this._value.push(this.$deepCopy(row))
    },
    delRow(i, row){
      this._value.splice(i, 1)
    },
    addRow(){
      if (this.maxSize > 0 && this._value.length >= this.maxSize){
        this.$message.warning(`最多只能添加${this.maxSize}行`)
      }else {
        let row = {}
        this.columns.forEach(col => this.$set(row, col.id, undefined))
        this._value.push(row)
        this.$set(this, '_value', this._value)
      }
    },
    delItem(id) {
      this._columns.splice(id, 1)
    },
    selectItem(cp) {
      this.selectFormItem = cp
    },
    getSelectedClass(cp) {
      return this.selectFormItem && this.selectFormItem.id === cp.id ? 'border-left: 4px solid #f56c6c' : ''
    },
    validate(call){
      if (this.rowLayout){
        let result = true
        for (let i = 0; i < this.columns.length; i++) {
          if (this.columns[i].props.required){
            for (let j = 0; j < this._value.length; j++) {
              result = !this.showError(this.columns[i], this._value[j][this.columns[i].id])
              if (!result){
                call(false)
                return
              }
            }
          }
        }
        call(result)
      }else {
        let success = 0
        this._value.forEach((v, i) => {
          let formRef = this.$refs[`table-form-${i}`]
          if (formRef && Array.isArray(formRef) && formRef.length > 0){
            formRef[0].validate(valid => {
              if (valid){
                success ++;
              }
            })
          }
        })
        if (success === this._value.length){
          call(true)
        }else {
          call(false)
        }
      }
    }
  }
}
</script>

<style lang="less" scoped>

/deep/ .valid-error {
  .el-input__inner{
    border-color: #F56C6C;
  }
}

.choose {
  border: 1px dashed @theme-primary !important;
}

.table-column {
  padding: 5px;
  margin-bottom: 10px;
  border-left: 3px solid #409eff;
  border-radius: 5px;
  background: #fafafa;
  /deep/ .el-form-item{
    margin-bottom: 0;
    .el-form-item__label{
      height: 25px;
    }
  }
  .table-column-action{
    float: right;
    span{
      color: #afafaf;
      margin-right: 10px;
      font-size: 13px;
    }
    i{
      color: #afafaf;
      padding: 5px;
      font-size: large;
      cursor: pointer;
      &:hover{
        color: #666666;
      }
    }
  }
}

.l-drag-from {
  min-height: 50px;
  background-color: rgb(245, 246, 246);

  .l-form-item, li {
    cursor: grab;
    background: #ffffff;
    padding: 10px;
    border: 1px solid #ebecee;
    margin: 5px 0;
  }
}

.l-form-header {
  font-size: small;
  color: #818181;
  text-align: left;
  position: relative;
  background-color: #fff;

  p {
    position: relative;
    margin: 0 0 10px 0;

    span {
      position: absolute;
      left: -8px;
      top: 3px;
      color: rgb(217, 0, 19);
    }
  }

  .l-option {
    position: absolute;
    top: -10px;
    right: -10px;

    i {
      font-size: large;
      cursor: pointer;
      color: #8c8c8c;
      padding: 5px;

      &:hover {
        color: @theme-primary;
      }
    }
  }
}

</style>
