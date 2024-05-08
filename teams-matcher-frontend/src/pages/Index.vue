<script setup lang="ts">
import {ref, watchEffect} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast, showSuccessToast} from "vant";
  import UserCardList from "../components/UserCardList.vue";

  // 是否为心动模式的引用
  const isMatchMode = ref<boolean>(false);
  // 用户列表的引用
  const userList = ref([]);
  // 加载状态的引用
  const loading = ref(true)

  /**
   * 加载用户数据。
   * 根据当前的心动模式，调用不同的接口获取用户数据。
   * 在心动模式下，根据标签匹配用户；在普通模式下，直接分页查询推荐用户。
   */
  const loadData = async () => {
    let userListData;
    loading.value = true;

    // 如果当前是心动模式，则根据标签匹配用户
    if (isMatchMode.value) {
      // 请求的用户数量
      const num = 10;
      userListData = await myAxios.get('/user/match', {
        params: {
          num
        }
      })
          .then(function (response) {
            console.log('/user/match succeed', response);
            return response?.data;
          })
          .catch(function (error) {
            console.log('/user/match error', error);
            showSuccessToast('请求失败');
          })
    } else {
      // 如果不是心动模式，则直接分页查询推荐用户
      userListData = await myAxios.get('/user/recommend', {
        params: {
          pageSize: 8, // 每页请求的数量
          pageNum: 1, // 当前页码
        }
      })
          .then(function (response) {
            console.log('/user/recommend succeed', response);
            return response?.data?.records;
          })
          .catch(function (error) {
            console.error('/user/recommend error', error);
            showFailToast('请求失败');
          })
    }
    // 如果获取到用户数据，处理用户标签，并更新用户列表
    if(userListData) {
      userListData.forEach((user) => {
        if(user.tags) {
          user.tags = JSON.parse(user.tags);
        }
      })
      userList.value = userListData;
    }
    loading.value = false; // 更新加载状态为完成
  }

// 监听任何影响用户列表的数据变化，自动加载数据
  watchEffect(() => {
    loadData();
  })

</script>

<template>
  <van-cell center title="心动模式">
    <template #right-icon>
      <van-switch v-model="isMatchMode" size="24" />
    </template>
  </van-cell>
  <user-card-list :user-list="userList" :loading="loading"/>
  <!-- 当用户列表为空时，显示无搜索结果的提示 -->
  <van-empty v-if="!userList || userList.length < 1" image="search" description="数据为空" />
</template>

<style scoped>

</style>