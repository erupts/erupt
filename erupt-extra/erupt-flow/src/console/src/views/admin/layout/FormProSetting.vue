<template>
  <div class="senior-setup">
    <el-form label-position="top" label-width="80px">
      <el-form-item label="审批同意时是否签字">
        <el-switch inactive-text="无需签字" active-text="需要签字"
                   v-model="setup && setup.sign"></el-switch>
        <div class="sign-tip">如果此处设置为 <b>需要签字</b>，则所有审批人“同意时” <b>必须签字</b></div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "FormProSetting",
  computed: {
    setup() {
      return this.$store.state.design.settings;
    }
  },
  data(){
    return{}
  },
  methods:{
    validate(){
      return []
    }
  }
}
</script>

<style lang="less" scoped>
.senior-setup {
  overflow: auto;
  margin: 0 auto;
  width: 600px;
  height: calc(100vh - 105px);
  background: #ffffff;
  margin-top: 10px;
  padding: 15px 20px;

  .sign-tip {
    color: #949495;
    font-size: small;
    margin-left: 20px;
  }
}
</style>
