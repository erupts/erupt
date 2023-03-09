<template>
  <node :title="config.name" :show-error="showError" :content="content" :error-info="errorInfo"
        @selected="$emit('selected')" @delNode="$emit('delNode')" @insertNode="type => $emit('insertNode', type)"
        placeholder="请设置延时时间" header-bgc="#f25643" header-icon="el-icon-time"/>
</template>

<script>
import Node from './Node'

export default {
  name: "DelayNode",
  props:{
    config:{
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  components: {Node},
  data() {
    return {
      showError: false,
      errorInfo: '',
    }
  },
  computed:{
    content(){
      if (this.config.props.type === 'FIXED'){
        return `等待 ${this.config.props.time} ${this.getName(this.config.props.unit)}`
      }else if(this.config.props.type === 'AUTO'){
        return `至当天 ${this.config.props.dateTime}`
      }else {
        return null
      }
    }
  },
  methods: {
    //校验数据配置的合法性
    validate(err){
      this.showError = false
      try {
        if (this.config.props.type === "AUTO") {
          if ((this.config.props.dateTime || "") === ""){
            this.showError = true
            this.errorInfo = "请选择时间点"
          }
        } else {
          if (this.config.props.time <= 0) {
            this.showError = true
            this.errorInfo = "请设置延时时长"
          }
        }
      } catch (e) {
        this.showError = true
        this.errorInfo = "配置出现问题"
      }
      if (this.showError){
        err.push(`${this.config.name} 未设置延时规则`)
      }
      return !this.showError
    },
    getName(unit){
      switch (unit){
        case 'D': return '天';
        case 'H': return '小时';
        case 'M': return '分钟';
        default: return '未知';
      }
    }
  }
}
</script>

<style scoped>

</style>
