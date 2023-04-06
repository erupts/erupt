<template>
  <div>
    <div v-if="mode === 'DESIGN'" >
      <el-date-picker size="medium" disabled :type="type" :placeholder="placeholder"></el-date-picker>
    </div>
    <div v-else>
      <el-date-picker v-model="_value" :value-format="format" size="medium" :disabled="formDisable" clearable :type="type" :placeholder="placeholder"></el-date-picker>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'

export default {
  mixins: [componentMinxins],
  name: "DateTime",
  components: {},
  props:{
    value: {
      default: null
    },
    format:{
      type: String,
      default: 'yyyy-MM-dd HH:mm'
    },
    placeholder:{
      type: String,
      default: '请选择日期时间'
    }
  },
  computed:{
    type(){
      switch (this.format){
        case 'yyyy': return 'year';
        case 'yyyy-MM': return 'month';
        case 'yyyy-MM-dd': return 'date';
        case 'yyyy-MM-dd HH:mm': return 'datetime';
        default: return 'datetime';
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
