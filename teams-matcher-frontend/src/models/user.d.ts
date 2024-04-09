/**
 * 定义用户类型的接口
 *
 * @type {UserType} 用户信息的类型定义，用于结构化表示用户的数据
 */
export type UserType = {
    id: number; // 用户的唯一标识符
    username: string; // 用户的用户名
    userAccount: string; // 用户的账号
    avatarUrl?: string; // 用户的头像URL，可选
    profile?: string; // 用户的个人简介，可选
    gender: number; // 用户的性别，0（男）或1（女）
    phone: string; // 用户的电话号码
    email: string; // 用户的电子邮箱地址
    userStatus: number; // 用户的状态，例如：0（正常）、1（禁用）
    userRole: number; // 用户的角色，例如：0（普通用户）、1（管理员）
    tags: string[]; // 用户的标签，用于分类或搜索
    createTime: Date; // 用户创建的时间
    
};