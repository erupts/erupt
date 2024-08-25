<template>
  <div>
    <el-form-item label="提示文字">
      <el-input v-model="value.placeholder" placeholder="请设置日期提示" size="small"/>
    </el-form-item>
    <el-form-item label="日期格式">
      <el-select v-model="value.format" size="small">
        <el-option label="年" value="yyyy"></el-option>
        <el-option label="年-月" value="yyyy-MM"></el-option>
        <el-option label="年-月-日" value="yyyy-MM-dd"></el-option>
        <el-option label="年-月-日 时:分" value="yyyy-MM-dd HH:mm"></el-option>
      </el-select>
    </el-form-item>
  </div>
</template>

<script>
export default {
  name: "DateTime",
  components: {},
  props: {
    value: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {}
  },
  methods: {}
}
</script>

<style scoped>

</style>
