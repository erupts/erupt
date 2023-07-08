<template>
  <div>
    <div v-if="mode === 'DESIGN'">
      <el-date-picker size="medium" v-model="_value" disabled :type="type" :start-placeholder="placeholder[0]" :end-placeholder="placeholder[1]"/>
      <div v-if="showLength" class="length">
        <span>时长：</span>
        <span>{{timeLength}}</span>
      </div>
    </div>
    <div v-else>
      <el-date-picker v-model="_value" size="medium" :disabled="!editable" clearable :value-format="format" :type="type" :start-placeholder="placeholder[0]" :end-placeholder="placeholder[1]"/>
      <div v-if="showLength" class="length">
        <span>时长：</span>
        <span>{{timeLength}}</span>
      </div>
    </div>
  </div>
</template>

<script>
import componentMinxins from '../ComponentMinxins'
import moment from 'moment';

export default {
  mixins: [componentMinxins],
  name: "DateTimeRange",
  components: {},
  props:{
    value: {
      type: Array,
      default: () => {
        return []
      }
    },
    format:{
      type: String,
      default: 'yyyy-MM-dd HH:mm'
    },
    placeholder:{
      type: Array,
      default: ()=>{
        return ['开始时间', '结束时间']
      }
    },
    showLength:{
      type: Boolean,
      default: false
    }
  },
  computed:{
    type(){
      switch (this.format){
        case 'yyyy-MM-dd': return 'daterange';
        case 'yyyy-MM-dd HH:mm': return 'datetimerange';
        default: return 'daterange';
      }
    },
    timeLength(){
      //求时长算法
      if (Array.isArray(this.value)){
        let start = moment(this.value[0]).format(this.format.replaceAll('dd', 'DD'))
        let end = moment(this.value[1]).format(this.format.replaceAll('dd', 'DD'))
        if (start === end){
          return '0 （时长为0，请确认）'
        }
        let mstart = moment(start);
        let mend = moment(end)
        let years = mend.diff(start, 'years')
        let months = mend.diff(start, 'months')
        let days = mend.diff(start, 'days')
        let hours = mend.diff(start, 'hours')
        let minutes = mend.diff(start, 'minutes')
        minutes = minutes % 60
        hours = hours % 24
        months = months % 12
        //因为每月天不固定，所以天要特殊动态处理
        if(mstart.date() < mend.date()){
          days = mend.date() - mstart.date()
          if (minutes > 0 || hours > 0){
            days --;
          }
        }
        //处理超过俩月且天超过31
        if (days > 31 && mend.month() - mstart.month() >= 2){
          //将日期推至上月求差
          days = mend.diff(mstart.add(mend.month() - mstart.month() - 1, 'month'), 'days')
        }
        return `${years > 0 ? years + '年 ': ' '}${months > 0 ? months + '个月 ': ' '}
                ${days > 0 ? days + '天 ': ' '}${hours > 0 ? hours + '小时 ': ' '}
                ${minutes > 0 ? minutes + '分钟 ': ' '}`
      } else {
        return '先选择时间哦'
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
.length{
  margin-top: 5px;
}
.length:nth-child(2){
  color: #8c8c8c;
}
/deep/ .el-date-editor--datetimerange.el-input__inner{
  width: 100%;
  max-width: 400px;
}
</style>
