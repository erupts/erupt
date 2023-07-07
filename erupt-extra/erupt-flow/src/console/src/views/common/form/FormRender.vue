<template>
  <!--渲染表单-->
  <el-form ref="form" class="process-form" label-position="top" :rules="rules" :model="_value">
    <el-form-item v-for="(item, index) in forms"
        style="margin-bottom: 8px;"
        v-show="item.props.perm!='H'"
        :prop="item.id" :label="item.name !== 'SpanLayout'?item.title+'['+item.props.perm+']':''" :key="item.name + index">

      <!-- 普通内容 -->
      <form-design-render :ref="`sub-item_${item.id}`" v-model="_value[item.id]" :mode="mode" :config="item" @change="change"
        v-if="item.name !== 'SpanLayout' && item.name !== 'Description'"
      />

      <!-- 布局容器 -->
      <form-design-render ref="`span-layou_${item.id}`" v-else v-model="_value" :mode="mode" :config="item"/>

    </el-form-item>
  </el-form>
</template>

<script>
import FormDesignRender from '@/views/admin/layout/form/FormDesignRender'

export default {
  name: "FormRender",
  components: {FormDesignRender},
  props:{
    forms: {
      type: Array,
      default: () => {
        return []
      }
    },
    value: {
      type: Object,
      default: () => {
        return {}
      }
    },
    mode: {
      type: String,
      default: "PC"
    }
  },
  data() {
    return {
      rules: {},
    }
  },
  mounted() {
    this.loadFormConfig(this.forms)
  },
  computed: {
    _value:{
      get(){
        return this.value
      },
      set(val){
        this.$emit('input', val)
      }
    }
  },
  watch: {
  },
  methods: {
    validate(call) {
      let success = true
      this.$refs.form.validate(valid => {
        success = valid
        if(valid){
          //校验成功再校验内部
          for (let i = 0; i < this.forms.length; i++) {
            if (this.forms[i].name === 'TableList'){
              let formRef = this.$refs[`sub-item_${this.forms[i].id}`]
              if (formRef && Array.isArray(formRef) && formRef.length > 0){
                formRef[0].validate(subValid => {
                  success = subValid
                })
                if (!success){
                  break
                }
              }
            }
          }
        }
        call(success)
      });
    },
    loadFormConfig(forms){
      // console.log("==获取到权限==")
      // this.forms.forEach(item => {
      //   console.log(item);
      //   console.log(item.title + ": " + item.props.perm);
      // })
      forms.forEach(item => {
        if (item.name === 'SpanLayout'){
          this.loadFormConfig(item.props.items)
        }else {
          this.$set(this._value, item.id, this.value[item.id])
          if(item.props.required){
            this.$set(this.rules, item.id, [{
              type: item.valueType === 'Array' ? 'array':undefined,
              required: true,
              message: `请填写${item.title}`, trigger: 'blur'
            }])
          }
          //let itemPerm = this.getPermsMap()[item.id];
          //item.props.perm = itemPerm || 'R';//默认只可读取
        }
      })
    },
    change(fieldName, val) {
      this.$emit("change", fieldName, val)
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
