import request from '@/api/request.js'


// 查询表单组
export function getFormGroups(param) {
  return request({
    url: '/admin/form/group',
    method: 'get',
    params: param
  })
}

// 查询表单组
export function getFormGroupsWithProcDef(param) {
  return request({
    url: '/process/groups',
    method: 'get',
    params: param
  })
}

// 表单排序
export function groupItemsSort(param) {
  return request({
    url: '/admin/form/sort',
    method: 'put',
    data: param
  })
}

// 表单分组排序
export function groupSort(param) {
  return request({
    url: '/admin/form/group/sort',
    method: 'put',
    data: param
  })
}

// 创建表单组
export function createGroup(groupName) {
  return request({
    url: '/admin/form/group',
    method: 'post',
    params: {
      groupName: groupName
    }
  })
}

// 创建表单组
export function updateGroup(groupId, param) {
  return request({
    url: '/admin/form/group/'+groupId,
    method: 'put',
    data: param
  })
}

// 删除表单组
export function removeGroup(groupId) {
  return request({
    url: '/admin/form/group/'+groupId,
    method: 'delete'
  })
}

// 获取表单分组
export function getGroup() {
  return request({
    url: '/admin/form/group/list',
    method: 'get'
  })
}

// 更新表单
export function updateForm(formId, param) {
  return request({
    url: '/admin/form/'+formId,
    method: 'put',
    data: param
  })
}

//创建表单
export function createForm(param){
  return request({
    url: '/admin/form',
    method: 'post',
    data: param
  })
}

// 查询表单详情
export function getFormDetail(id) {
  return request({
    url: '/admin/form/detail/' + id,
    method: 'get'
  })
}

// 更新表单详情
export function updateFormDetail(param) {
  return request({
    url: '/admin/form/detail',
    method: 'put',
    data: param
  })
}

// 更新表单详情
export function removeForm(param) {
  return request({
    url: '/admin/form/'+param.formId,
    method: 'delete',
    data: param
  })
}
