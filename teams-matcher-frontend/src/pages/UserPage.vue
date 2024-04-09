<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user.ts";

const user = ref();

onMounted(async () => {
  user.value = await getCurrentUser();
})

/** 跳转到用户信息编辑页面 */
const router = useRouter();

const toEdit = (editKey: string, editName: string, currentValue: string) => {
  router.push({
    path: '/user/edit',
    // 在重定向时，将editKey和currentValue作为查询参数附加在URL中
    // query对象允许您在不改变路径的情况下传递额外的数据，这些数据会被序列化并添加到URL的查询字符串部分
    query: {
      editKey, // 要编辑的用户的标识键
      editName, // 要编辑的字段名称
      currentValue // 当前待编辑字段的值
    }
  });
  // 跳转后，URL可能类似于 '/user/edit?editKey=someKey
}
</script>
<!-- 单元格列表 -->
<template>
  <template v-if="user">
    <van-cell title="头像" is-link to="/user/edit" >
      <img style="height: 48px" :src="user.avatarUrl"/>
    </van-cell>
    <van-cell title="账号" value="TestUserAccount" />
    <van-cell title="昵称" is-link to="/user/edit" :value="user.username" @click="toEdit('username', '昵称', user.username)"/>
    <van-cell title="性别" is-link to="/user/edit" :value="user.gender" @click="toEdit('gender', '性别', user.gender)"/>
    <van-cell title="电话" is-link to="/user/edit" :value="user.phone" @click="toEdit('phone', '电话', user.phone)"/>
    <van-cell title="邮箱" is-link to="/user/edit" :value="user.email"  @click="toEdit('email', '邮箱', user.email)"/>
    <van-cell title="注册时间" is-link :value="user.createTime" />
  </template>
</template>

<style scoped>

</style>