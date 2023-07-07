<template>
  <node :title="config.name" :show-error="showError" :content="content" :error-info="errorInfo"
        @selected="$emit('selected')" @delNode="$emit('delNode')" @insertNode="type => $emit('insertNode', type)"
        placeholder="请设置触发器" header-bgc="#47bc82" header-icon="el-icon-set-up"/>
</template>

<script>
import Node from './Node'

export default {
  name: "TriggerNode",
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
      this.config
    }
  },
  methods: {
    //校验数据配置的合法性
    validate(err){
      this.showError = false
      if (this.config.props.type === 'WEBHOOK'){
        if(this.$isNotEmpty(this.config.props.http.url)){
          this.showError = false
        }else {
          this.showError = true
          this.errorInfo = '请设置WEBHOOK的URL地址'
        }
      }else if(this.config.props.type === 'EMAIL'){
        if(!this.$isNotEmpty(this.config.props.email.subject)
            || this.config.props.email.to.length === 0
            || !this.$isNotEmpty(this.config.props.email.content)){
          this.showError = true
          this.errorInfo = '请设置邮件发送配置'
        }else {
          this.showError = false
        }
      }
      if (this.showError){
        err.push(`${this.config.name} 触发动作未设置完善`)
      }
      return !this.showError
    }
  }
}
</script>

<style scoped>

</style>
