/**
 * 用户状态管理模块
 * 用于管理和操作当前用户的登录状态
 */

import {UserType} from "../models/user"; // 导入用户类型定义

let currentUser : UserType; // 存储当前登录用户的数据

/**
 * 设置当前用户状态
 * @param user {UserType} - 需要设置的用户对象
 */
const setCurrentUserState = (user : UserType) => {
    currentUser = user;
}

/**
 * 获取当前用户状态
 * @returns {UserType} - 返回当前登录的用户对象
 */
const getCurrentUserState = () : UserType => {
    return currentUser;
}

export {
    setCurrentUserState, // 导出设置当前用户状态的函数
    getCurrentUserState // 导出获取当前用户状态的函数
}
