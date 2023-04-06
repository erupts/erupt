Array.prototype.remove = function (value) {
  let index = this.indexOf(value)
  if (index > -1) {
    this.splice(index, 1)
  }
  return index
}

//移除对象数组，匹配唯一key
Array.prototype.removeByKey = function (key, val) {
  let index = this.findIndex(value => value[key] === val)
  if (index > -1) {
    this.splice(index, 1)
  }
  return index
}

//对象数组转map
Array.prototype.toMap = function (key) {
  let map = new Map()
  this.forEach(v => map.set(v[key], v))
  return map
}


