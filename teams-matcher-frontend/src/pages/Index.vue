<script setup>
  import {onMounted, ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast, showSuccessToast} from "vant";
  import UserCardList from "../components/UserCardList.vue";

  const userList = ref([]);

  // 在组件挂载时触发的异步函数
  onMounted(async () => {
    // 使用myAxios发起一个get请求，请求用户标签搜索结果
    const userListDate = await myAxios.get('/user/recommend', {
      params: {
        pageSize: 8,
        pageNum: 1
      }
    })
        .then(function (response) {
          console.log('user/recommend succeed', response);
          showSuccessToast('请求成功')
          return response?.data?.records;
        })
        .catch(function (error) {
          console.log('user/recommend error', error);
          showFailToast('请求失败')
        })
    // 请求成功后，解析并更新用户列表
    if(userListDate) {
      // 把后端传来的JSON数据转化成数组
      userListDate.forEach(user => {
        if(user.tags) {
          user.tags = JSON.parse(user.tags);
        }
      })
      // 更新用户列表的值
      userList.value = userListDate;
      console.log('成功更新用户列表的值');
    }
  })
</script>

<template>
  <user-card-list :user-list="userList" />
  <!-- 当用户列表为空时，显示无搜索结果的提示 -->
  <van-empty v-if="!userList || userList.length < 1" image="search" description="数据为空" />
</template>

<style scoped>

</style>