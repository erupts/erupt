<template>
  <node title="发起人" :is-root="true" :content="content"
        @selected="$emit('selected')" @insertNode="type => $emit('insertNode', type)"
        placeholder="所有人" header-bgc="#576a95" header-icon="el-icon-user-solid"/>
</template>

<script>
import Node from './Node'

export default {
  name: "RootNode",
  components: {Node},
  props:{
    config:{
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  computed:{
    content(){
      if (this.config.props.assignedUser.length > 0){
        let texts = []
        this.config.props.assignedUser.forEach(org => texts.push(org.name))
        return String(texts).replaceAll(',', '、')
      } else {
        return '所有人'
      }
    }
  },
  data() {
    return {
    }
  },
  methods: {}
}
</script>

<style scoped>

</style>
