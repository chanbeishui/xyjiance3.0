/**
 * 通用工具函数（从 xyjiance-ui 适配）
 */

// 日期格式化
export function parseTime(time, pattern) {
  if (!time) return null
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(/-/gm, '/').replace('T', ' ').replace(/\.[\d]{3}/gm, '')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(), m: date.getMonth() + 1, d: date.getDate(),
    h: date.getHours(), i: date.getMinutes(), s: date.getSeconds(),
    a: date.getDay()
  }
  return format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') return ['日', '一', '二', '三', '四', '五', '六'][value]
    if (result.length > 0 && value < 10) value = '0' + value
    return value || 0
  })
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) return ''
  const actions = []
  Object.keys(datas).some((key) => {
    if (datas[key].value == ('' + value)) {
      actions.push(datas[key].label)
      return true
    }
  })
  if (actions.length === 0) actions.push(value)
  return actions.join('')
}

// 转换字符串 undefined/null -> ""
export function parseStrEmpty(str) {
  if (!str || str == 'undefined' || str == 'null') return ''
  return str
}

// 构造树型结构
export function handleTree(data, id, parentId, children) {
  const config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children'
  }
  const childrenListMap = {}
  const tree = []
  for (const d of data) {
    const id = d[config.id]
    childrenListMap[id] = d
    if (!d[config.childrenList]) d[config.childrenList] = []
  }
  for (const d of data) {
    const parentId = d[config.parentId]
    const parentObj = childrenListMap[parentId]
    if (!parentObj) tree.push(d)
    else parentObj[config.childrenList].push(d)
  }
  return tree
}

// URL 参数处理
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            result += encodeURIComponent(propName + '[' + key + ']') + '=' + encodeURIComponent(value[key]) + '&'
          }
        }
      } else {
        result += encodeURIComponent(propName) + '=' + encodeURIComponent(value) + '&'
      }
    }
  }
  return result
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  let search = params
  search.params = typeof search.params === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {}
  dateRange = Array.isArray(dateRange) ? dateRange : []
  if (typeof propName === 'undefined') {
    search.params['beginTime'] = dateRange[0]
    search.params['endTime'] = dateRange[1]
  } else {
    search.params['begin' + propName] = dateRange[0]
    search.params['end' + propName] = dateRange[1]
  }
  return search
}

// 验证是否为blob格式
export function blobValidate(data) {
  return data.type !== 'application/json'
}
