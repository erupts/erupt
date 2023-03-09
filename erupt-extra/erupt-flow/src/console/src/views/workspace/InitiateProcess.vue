<template>
  <div v-loading="loading">
    <div v-if="!loading">
      <!--渲染表单-->
      <form-render class="process-form" ref="form" :forms="forms" v-model="formData"/>
    </div>
    <div style="padding-bottom: 10px;">
      <el-button type="text" size="medium" style="color: #909399;" @click="showTimeLineSwitch">
        审批流程 <i :class="{'el-icon-arrow-down': !showTimeLine, 'el-icon-arrow-up': showTimeLine}"></i>
      </el-button>
    </div>
    <div v-show="showTimeLine">
      <timeLine ref="timeLine"></timeLine>
    </div>
  </div>
</template>

<script>
import FormRender from '@/views/common/form/FormRender'
import FormDesignRender from '@/views/admin/layout/form/FormDesignRender'
import {getFormDetail} from '@/api/design'
import TimeLine from '@/views/workspace/TimeLine'

export default {
  name: "InitiateProcess",
  components: {FormDesignRender, FormRender, TimeLine},
  props: {
    code: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      formData: {},
      form: {
        formId: '',
        formName: "",
        logo: {},
        formItems: [],
        process: {},
        remark: ""
      },
      showTimeLine: false
    }
  },
  mounted() {
    this.loadFormInfo(this.code)
  },
  computed: {
    forms() {
      return this.$store.state.design.formItems;
    }
  },
  watch: {
    formData(oldVal, newVal) {
      if(this.showTimeLine) {
        this.$refs.timeLine.fresh(this.code, this.formData);
      }
    }
  },
  methods: {
    loadFormInfo(formId) {
      this.loading = true
      getFormDetail(formId).then(rsp => {
        this.loading = false
        let form = rsp.data;
        form.logo = JSON.parse(form.logo)
        form.settings = JSON.parse(form.settings)
        form.formItems = JSON.parse(form.formItems)
        form.process = JSON.parse(form.process)
        this.form = form
        //构建表单及校验规则
        this.$store.state.design = form
      }).catch(err => {
        this.loading = false
        this.$message.error(err)
      })
    },
    validate(call) {
      this.$refs.form.validate(call);
    },
    getFormData() {
      return this.formData;
    },
    showTimeLineSwitch() {
      this.showTimeLine = !this.showTimeLine;
      if(this.showTimeLine) {
        this.$refs.timeLine.fresh(this.code, this.formData);
      }
    }
  }
}
</script>

<style lang="less" scoped>
.process-form {
  /deep/ .el-form-item__label {
    padding: 0 0;
  }
}
</style>
