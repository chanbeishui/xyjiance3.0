import request from '@/utils/request'

// 查询公司列表
export function listCompany(query) {
  return request({
    url: '/system/company/list',
    method: 'get',
    params: query
  })
}

// 查询公司列表（排除节点）
export function listCompanyExcludeChild(companyId) {
  return request({
    url: '/system/company/list/exclude/' + companyId,
    method: 'get'
  })
}

// 查询公司详细
export function getCompany(companyId) {
  return request({
    url: '/system/company/' + companyId,
    method: 'get'
  })
}

// 新增公司
export function addCompany(data) {
  return request({
    url: '/system/company',
    method: 'post',
    data: data
  })
}

// 修改公司
export function updateCompany(data) {
  return request({
    url: '/system/company',
    method: 'put',
    data: data
  })
}

// 删除公司
export function delCompany(companyId) {
  return request({
    url: '/system/company/' + companyId,
    method: 'delete'
  })
}
