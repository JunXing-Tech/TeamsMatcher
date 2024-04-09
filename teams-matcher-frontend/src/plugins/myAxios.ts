import axios from 'axios'

// 创建一个 axios 实例
const myAxios = axios.create({
    baseURL: 'http://localhost:8080/api' // 设置请求的基地址
});

myAxios.defaults.withCredentials = true;

// 添加请求拦截器
myAxios.interceptors.request.use(function (config) {
    console.log("前端发送请求", config)
    return config;
}, function (error) {
    return Promise.reject(error);
});

// 添加响应拦截器
myAxios.interceptors.response.use(function (response) {
    console.log("后端返回数据", response)
    return response.data;
}, function (error) {
    return Promise.reject(error);
});

// 导出创建的 axios 实例
export default myAxios;



