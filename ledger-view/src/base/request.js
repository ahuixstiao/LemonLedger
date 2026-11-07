import axios from 'axios'

// 实例只在模块加载时创建一次：原来写进 request() 里，每次请求都新建一个 axios 实例
// 并重复注册两个拦截器，请求越多越慢，拦截器也会不停堆积。
const http = axios.create({
  // 服务器基础路径
  baseURL: import.meta.env.VITE_BASE_URL, // 获取对应环境的路径
  // 超时时间：导出 Excel / 年度统计可能跑十几秒，5 秒太短
  timeout: 15000
})

// 请求拦截器
http.interceptors.request.use(
  config => {
    // 清理空查询参数，避免后端 Integer/Long 参数接收空字符串时报 400
    if (config.params && typeof config.params === 'object') {
      const cleanedParams = {}
      Object.keys(config.params).forEach(key => {
        const value = config.params[key]
        if (value !== '' && value !== null && value !== undefined) {
          cleanedParams[key] = value
        }
      })
      config.params = cleanedParams
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：2xx 直接返回，其余交给调用方 catch
http.interceptors.response.use(
  response => response,
  error => Promise.reject(error)
)

export function request(config) {
  return http(config)
}
