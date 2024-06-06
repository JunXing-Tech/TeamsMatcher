<script setup lang="ts">
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user.ts";
import myAxios from "../plugins/myAxios.ts";
import {useRouter} from "vue-router";
import {showConfirmDialog} from "vant";

const user = ref();
let show = ref(false);

const router = useRouter();

const userQuit = async () => {
  show.value = true;
  await showConfirmDialog({
    message:
        '确定退出账号？',
  })
      .then(() => {
          const res = myAxios.post('/user/logout');
          console.log(res, "用户退出账号成功");
          // 用户登出后页面刷新
          router.push("/user/login");
      })
      .catch(() => {
        router.push("/user");
      });

}

onMounted(async () => {
  user.value = await getCurrentUser();
})

</script>
<template>
  <template v-if="user" v-model:show>
    <van-cell title="当前用户" :value="user?.username" />
    <van-cell title="修改信息" is-link to="/user/update" />
    <van-cell title="我创建的队伍" is-link to="/user/team/create" />
    <van-cell title="我加入的队伍" is-link to="/user/team/join" />
    <van-button round type="primary" size="large" @click="userQuit" >退出账号</van-button>
  </template>
</template>

<style scoped>

</style>