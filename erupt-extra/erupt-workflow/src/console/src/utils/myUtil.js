export function format(time, format) {
  var t = new Date(time);
  var tf = function (i) { return (i < 10 ? '0' : '') + i };
  return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function (a) {
    switch (a) {
      case 'yyyy':
        return tf(t.getFullYear());
        break;
      case 'MM':
        return tf(t.getMonth() + 1);
        break;
      case 'mm':
        return tf(t.getMinutes());
        break;
      case 'dd':
        return tf(t.getDate());
        break;
      case 'HH':
        return tf(t.getHours());
        break;
      case 'ss':
        return tf(t.getSeconds());
        break;
    }
  })
}

/**
 * 计算出相差天数
 * @param secondSub
 */
export function formatTotalDateSub (secondSub) {
  var days = Math.floor(secondSub / (24 * 3600));     // 计算出小时数
  var leave1 = secondSub % (24*3600) ;                // 计算天数后剩余的毫秒数
  var hours = Math.floor(leave1 / 3600);              // 计算相差分钟数
  var leave2 = leave1 % (3600);                       // 计算小时数后剩余的毫秒数
  var minutes = Math.floor(leave2 / 60);              // 计算相差秒数
  var leave3 = leave2 % 60;                           // 计算分钟数后剩余的毫秒数
  var seconds = Math.round(leave3);
  return days + "天" + hours + "时" + minutes + "分" + seconds + '秒';
}

