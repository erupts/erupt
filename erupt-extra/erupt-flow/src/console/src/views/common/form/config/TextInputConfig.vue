<template>
  <el-form-item label="提示文字">
    <el-input size="small" v-model="value.placeholder" placeholder="请设置提示语"/>
  </el-form-item>
</template>

<script>
export default {
  name: "TextInput",
  components: {},
  props:{
    value:{
      type: Object,
      default: ()=>{
        return {}
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
