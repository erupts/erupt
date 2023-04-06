//混入组件数据
export default{
  props:{
    mode:{
      type: String,
      default: 'DESIGN'
    },
    formDisable:{
      type: Boolean,
      default: false
    },
    required:{
      type: Boolean,
      default: false
    },
  },
  data(){
    return {}
  },
  watch: {
    _value(newValue, oldValue) {
      this.$emit("change", newValue);
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
}
