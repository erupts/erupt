select name '姓名',choice '选择',`date` '时间',remark '备注' from demo
where 1=1
${
  if(:name){
    and name = :name
  }
}