import axios from 'axios'

export function request(config) {
    // 创建实例
    const _axiso = axios.create({
        // 服务器基础路径
        baseURL: import.meta.env.VITE_BASE_URL //获取对应环境的路径
        //timeout: 5000
    })

    // 请求拦截器
    _axiso.interceptors.request.use(
        config => {
            return config
        },
        error => {
            return Promise.reject(error)
        }
    )

    _axiso.interceptors.response.use(
        response => {
            // 2xx 范围内的状态码都会触发该函数。
            // 对响应数据做点什么
            return response
        },
        error => {
            // 超出 2xx 范围的状态码都会触发该函数。
            // 对响应错误做点什么
            return Promise.reject(error)
        }
    )
    //console.log('请求' + config.url)
    // 发送网络请求，返回Promise对象
    return _axiso(config)
}
