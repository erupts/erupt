import request from '@/api/request.js'

// 发起流程
export function startByFormId(formId, data) {
  return request({
    url: '../erupt-api/erupt-flow/process/start/form/'+formId,
    method: 'post',
    data: data
  })
}

// 查询实例
export function getProcessInstance(procInstId) {
  return request({
    url: '../erupt-api/erupt-flow/data/OaProcessInstance/'+procInstId,
    method: 'get'
  });
}

// 查询待我处理的工作
export function listMyTasks(params) {
  return request({
    url: '../erupt-api/erupt-flow/task/mine',
    method: 'get',
    params: params
  });
}

// 完成任务
export function completeTask(taskId, remarks, data) {
  return request({
    url: '../erupt-api/erupt-flow/task/complete/'+taskId,
    method: 'post',
    data: {
      remarks: remarks,
      data: data
    }
  });
}

// 拒绝任务
export function refuseTask(taskId, remarks, data) {
  return request({
    url: '../erupt-api/erupt-flow/task/refuse/'+taskId,
    method: 'post',
    data: {
      remarks: remarks,
      data: data
    }
  });
}

// 预览流程时间线
export function timeLinePreview(defId, content) {
  return request({
    url: '../erupt-api/erupt-flow/process/timeline/preview/'+defId,
    method: 'post',
    data: content
  });
}

// 查看流程实例的时间线
export function timeLine(instId) {
  return request({
    url: '../erupt-api/erupt-flow/process/timeline/'+instId,
    method: 'post'
  });
}

// 查询任务详情
export function getTaskDetail(taskId) {
  return request({
    url: '../erupt-api/erupt-flow/task/detail/'+taskId,
    method: 'get'
  });
}

// 查询实例详情
export function getInstDetail(instId) {
  return request({
    url: '../erupt-api/erupt-flow/inst/detail/'+instId,
    method: 'get'
  });
}

// 查询与我相关的工单
export function getMineAbout(params) {
  return request({
    url: '../erupt-api/erupt-flow/inst/mine/about',
    method: 'get',
    params: params
  });
}
