import request from '@/api/request.js'


// 查询组织架构树
export function getOrgTree(param) {
  return request({
    url: '/oa/org/tree',
    method: 'get',
    params: param
  })
}

// 查询人员
export function getOrgTreeUser(param) {
  return request({
    url: '/oa/org/tree/user',
    method: 'get',
    params: param
  })
}

// 查询角色列表
export function getRole(param) {
  return request({
    url: '/oa/role',
    method: 'get',
    params: param
  })
}
