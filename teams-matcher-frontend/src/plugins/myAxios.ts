import axios from 'axios'

// 创建一个 axios 实例
const myAxios = axios.create({
    baseURL: 'http://localhost:8080/api' // 设置请求的基地址
});
// 跨域请求时也包含cookie等用户信息
myAxios.defaults.withCredentials = true;

/**
 * 添加请求拦截器
 * 用于在发送请求前对请求配置进行统一处理，或对错误进行统一处理。
 *
 * @param {Function} use 函数，用于注册一个请求拦截器。该函数接收两个参数：
 *                       - config：请求配置对象。
 *                       - error：请求错误对象。
 * @returns 返回一个Promise，当请求拦截器中发生错误时，Promise将被reject。
 */
myAxios.interceptors.request.use(function (config) {
    console.log("前端发送请求", config)
    return config;
}, function (error) {
    return Promise.reject(error);
});

/**
 * 添加响应拦截器以处理后端返回的数据和错误。
 *
 * @param {Function} onFulfilled - 当请求成功响应时调用的函数。接收响应对象作为参数。
 *                                  可以在此函数内对响应数据进行处理，并返回最终想要的结果
 * @param {Function} onRejected - 当请求发生错误时调用的函数。接收错误对象作为参数。
 *                                 可以在此函数内对错误进行处理，并返回一个被拒绝的Promise
 */
myAxios.interceptors.response.use(function (response) {
    console.log("后端返回数据", response)
    // 检查响应数据码，若为未登录状态，则重定向到登录页
    if(response?.data?.code === 40100) {
        const redirectUrl = window.location.href;
        window.location.href = `/user/login?redirect=${redirectUrl}`;
    }
    return response.data;
}, function (error) {
    // 对错误进行处理，返回一个被拒绝的Promise
    return Promise.reject(error);
});

// 导出创建的 axios 实例
export default myAxios;



