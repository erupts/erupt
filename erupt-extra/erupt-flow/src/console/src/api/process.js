import request from '@/api/request.js'

// 模拟流程步骤

// 发起流程
export function startByFormId(formId, data) {
  return request({
    url: '/process/start/form/'+formId,
    method: 'post',
    data: data
  })
}

// 查询实例
export function getProcessInstance(procInstId) {
  return request({
    url: '/data/OaProcessInstance/'+procInstId,
    method: 'get'
  });
}

// 查询待我处理的工作
export function listMyTasks(params) {
  return request({
    url: '/task/mine',
    method: 'get',
    params: params
  });
}

// 查询待我处理的工作
export function completeTask(taskId, remarks) {
  return request({
    url: '/task/complete/'+taskId,
    method: 'post',
    params: {
      remarks: remarks
    }
  });
}

// 预览流程时间线
export function timeLinePreview(defId, content) {
  return request({
    url: '/process/timeline/preview/'+defId,
    method: 'post',
    data: content
  });
}

// 查看流程实例的时间线
export function timeLine(instId) {
  return request({
    url: '/process/timeline/'+instId,
    method: 'post'
  });
}
