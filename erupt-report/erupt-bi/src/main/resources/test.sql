select name '姓名',choice '选择',`date` '时间',remark '备注' from demo
where 1=1 ${name&&' and '+' name = '+"'"+ name[0] +' '+name[1]+"'" }
${exp(233)}