<script setup lang="ts">
  import {UserType} from "../models/user";

  /**
   * 用户卡片列表组件的属性接口
   *
   * @property {UserType[]} userList - 用户列表，类型为UserType数组
   */
  interface UserCardListProps {
    userList: UserType[];
    loading: boolean;
  }
  // 使用withDefaults函数为组件的props定义默认值
  const props = withDefaults(defineProps<UserCardListProps>(), {
    loading: true,
    // @ts-ignore
    userList: [] as UserType[]
  });
</script>

<template>
  <!-- 使用van-skeleton组件来展示加载时的骨架屏 -->
  <van-skeleton
      title
      avatar
      :row="3"
      :loading="props.loading"
      v-for="user in props.userList">
  <!-- 使用van-card组件来展示用户信息列表 -->
  <van-card
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
  </van-skeleton>
</template>

<style scoped>

</style>