<template>
  <div>
    <el-form-item label="提示文字">
      <el-input size="small" v-model="value.placeholder" placeholder="请设置日期提示"/>
    </el-form-item>
    <el-form-item label="日期格式">
      <el-select size="small" v-model="value.format">
        <el-option value="yyyy" label="年"></el-option>
        <el-option value="yyyy-MM" label="年-月"></el-option>
        <el-option value="yyyy-MM-dd" label="年-月-日"></el-option>
        <el-option value="yyyy-MM-dd HH:mm" label="年-月-日 时:分"></el-option>
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
