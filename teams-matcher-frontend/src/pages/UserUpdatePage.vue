<script setup lang="ts">
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user.ts";
import {useRouter} from "vue-router";

const user = ref();

  onMounted(async () => {
    user.value = await getCurrentUser();
  })

const router = useRouter();

const formatGender = (gender : number)  => {
  return gender === 0 ? '男' : '女';
}

const formatDate = (timestamp : string) => {
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

const toEdit = (editKey : string, editName : string, currentValue : string) => {
  router.push({
    path: '/user/edit',
    query: {
      editKey,
      editName,
      currentValue
    }
  })
}
</script>

<template>
    <template v-if="user">
      <van-cell title="头像" is-link to="/user/edit" :value="user.avatarUrl" @click="toEdit('avatarUrl', '头像', user.avatarUrl)">
        <img style="height: 48px" :src="user.avatarUrl"/>
      </van-cell>
      <van-cell title="账号" :value="user.userAccount" />
      <van-cell title="昵称" is-link to="/user/edit" :value="user.username" @click="toEdit('username', '昵称', user.username)"/>
      <van-cell
          title="性别"
          is-link
          to="/user/edit"
          :value="formatGender(user.gender)"
          @click="toEdit('gender', '性别', formatGender(user.gender))"
      />
      <van-cell title="电话" is-link to="/user/edit" :value="user.phone" @click="toEdit('phone', '电话', user.phone)"/>
      <van-cell title="邮箱" is-link to="/user/edit" :value="user.email"  @click="toEdit('email', '邮箱', user.email)"/>
      <van-cell title="注册时间" is-link :value="formatDate(user.createTime)" />
    </template>
</template>

<style scoped>

</style>