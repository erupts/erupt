<template>
  <el-dialog custom-class="custom-dialog" class="border" :width="width" :title="title"
             append-to-body :close-on-click-modal="clickClose"
             :destroy-on-close="closeFree" :visible.sync="_value">
    <slot></slot>
    <div slot="footer" v-if="showFooter">
      <el-button size="mini" @click="_value = false; $emit('cancel')">{{ cancelText }}</el-button>
      <el-button size="mini" type="primary" @click="$emit('ok')">{{ okText }}</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "WDialog",
  install(Vue) {
    Vue.component('WDialog', this)
  },
  components: {},
  props: {
    title: {
      type: String,
      default: ''
    },
    width: {
      type: String,
      default: '50%'
    },
    value: {
      type: Boolean,
      default: false
    },
    clickClose: {
      type: Boolean,
      default: false
    },
    closeFree: {
      type: Boolean,
      default: false
    },
    showFooter: {
      type: Boolean,
      default: true
    },
    cancelText: {
      type: String,
      default: '取 消'
    },
    okText: {
      type: String,
      default: '确 定'
    },
    border:{
      type: Boolean,
      default: true
    }
  },
  computed: {
    _value: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit("input", val);
      }
    }
  },
  data() {
    return {}
  },
  methods: {}
}
</script>

<style lang="less" scoped>
/deep/ .custom-dialog {
  .el-dialog__header {
    padding: 10px 20px;
    .el-dialog__title{
      font-size: 17px;
    }
    .el-dialog__headerbtn{
      top: 15px;
      .i{
        font-size: large;
      }
    }
  }

  .el-dialog__footer {
    padding: 10px 20px;
  }
}

 .border {
   /deep/ .el-dialog__header {
    border-bottom: 1px solid #e8e8e8;
  }
   /deep/ .el-dialog__footer {
    border-top: 1px solid #e8e8e8;
  }
}
</style>
