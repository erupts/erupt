//混入组件数据
export default{
  props:{
    mode:{
      type: String,
      default: 'DESIGN'
    },
    editable:{
      type: Boolean,
      default: true
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
  methods: {
    _opValue(op) {
      if(typeof(op)==='object') {
        return op.value;
      }else {
        return op;
      }
    },
    _opLabel(op) {
      if(typeof(op)==='object') {
        return op.label;
      }else {
        return op;
      }
    }
  }
}
