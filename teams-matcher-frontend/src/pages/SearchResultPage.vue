<script setup>
import {useRoute} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios"
import qs from "qs";

// 使用useRoute获取当前路由信息
const route = useRoute();
// 从路由的query中解析出tags
const {tags} = route.query;
// 使用ref创建一个用户列表的响应式变量
const userList = ref([]);

// 在组件挂载时触发的异步函数
onMounted(async () => {
  // 使用myAxios发起一个get请求，请求用户标签搜索结果
  const userListDate = await myAxios.get('/user/search/tags', {
    params: {
      tagNameList: tags
    },
    paramsSerializer: params => {
      // 使用qs库来序列化查询参数，避免参数中的数组被错误解析
      return qs.stringify(params, { indices: false })
    }
  })
      .then(function (response) {
        console.log('user/search/tags succeed', response);
        // showSuccessToast('请求成功')
        return response?.data;
      })
      .catch(function (error) {
        console.log('user/search/tags error', error);
        // showFailToast('请求失败')
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
  }
})
</script>

<template>
  <!-- 使用van-card组件来展示用户信息列表 -->
  <van-card
      v-for="user in userList"
      :desc="user.profile"
      :title="user.username"
      :thumb="user.avatarUrl"
  >
    <!-- 标签模板，用于展示用户的标签 -->
    <template #tags>
      <van-tag plain type="danger" v-for="tag in user.tags" :key="tag" style="margin: 6px 6px">
        {{ tag }}
      </van-tag>
    </template>
    <!-- 底部模板，包含一个联系我按钮 -->
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
  <!-- 当用户列表为空时，显示无搜索结果的提示 -->
  <van-empty v-if="!userList || userList.length < 1" image="search" description="无搜索结果" />
</template>

<style scoped>

</style>