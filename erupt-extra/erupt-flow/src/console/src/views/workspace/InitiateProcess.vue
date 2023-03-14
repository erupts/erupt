<template>
  <div v-loading="loading">
    <div v-if="!loading">
      <!--渲染表单-->
      <form-render class="process-form" ref="form" :forms="forms" v-model="formData" @change="formChange"/>
    </div>

    <div style="padding-bottom: 10px;" @click="showTimeLine = !showTimeLine">
      <el-button type="text" size="medium" style="color: #909399;">
        审批流程 <i :class="{'el-icon-arrow-down': !showTimeLine, 'el-icon-arrow-up': showTimeLine}"></i>
      </el-button>
    </div>
    <div v-show="showTimeLine">
      <timeLine ref="timeLine" current="root"></timeLine>
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
      showTimeLine: false,
      form: {
        formId: '',
        formName: "",
        logo: {},
        formItems: [],
        process: {},
        remark: ""
      }
    }
  },
  mounted() {
    this.loadFormInfo(this.code);
    //this.formChange()//刷新一次时间线
  },
  computed: {
    forms() {
      return this.$store.state.design.formItems;
    }
  },
  watch: {
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
      });
    },
    validate(call) {
      this.$refs.form.validate(call);
    },
    //表单内容变更回调
    formChange(field, val) {
      this.$nextTick(() => {
        //时间线为空，或者当前字段会触发时间线变更
        if(this.$refs.timeLine.getActivities().length<=0 || this.isConditionField(field, this.form.process)) {
          //刷新时间线
          this.$refs.timeLine.fresh(this.code, this.formData);
        }
      })
    },
    isConditionField(field, nodeConfig) {
      //如果当前节点是一个条件分支，并且条件里面有此字段
      if(nodeConfig.type=="CONDITION" && nodeConfig.props.groups && nodeConfig.props.groups.length>0) {
        for (let i in nodeConfig.props.groups) {//条件组
          for (let j in nodeConfig.props.groups[i].conditions) {//条件
            if(nodeConfig.props.groups[i].conditions[j].id === field) {
              return true;
            }
          }
        }
      }
      //否则判断分支
      if(nodeConfig.branchs && nodeConfig.branchs.length>0) {
        for (let i in nodeConfig.branchs) {
          if(this.isConditionField(field, nodeConfig.branchs[i])) {
            return true;
          }
        }
      }
      //否则判断子节点
      if(nodeConfig.children) {
        if(this.isConditionField(field, nodeConfig.children)) {
          return true;
        }
      }
      //都不满足，返回false
      return false;
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
