<template>
  <!--  添加 Vant3 NavBar导航栏组件-->
  <van-nav-bar
      title="标题"
      left-text="返回"
      right-text="按钮"
      left-arrow
      @click-left="onClickLeft"
      @click-right="onClickRight"
  >
   <!-- 使用插槽自定义导航栏右侧内容 引入搜索图标 -->
   <template #right>
     <van-icon name="search" size="18" />
   </template>
  </van-nav-bar>

  <!-- 自定义关联页面 -->
  <div id="content">
    <!-- 将显示与 URL 对应的组件 -->
    <router-view />
  </div>
  <!--  设置底部标签栏 -->
  <van-tabbar route @change="onChange">
    <van-tabbar-item to="/" icon="home-o" name="index">主页</van-tabbar-item>
    <van-tabbar-item to="/team" icon="search" name="team">队伍</van-tabbar-item>
    <van-tabbar-item to="/user" icon="friends-o" name="user">个人</van-tabbar-item>
  </van-tabbar>

</template>

<script setup>
  import {useRouter} from "vue-router"

  const router = useRouter();
  const onClickLeft = () => {
    router.back();
  }
  const onClickRight = () => {
    router.push('/search')
  };
</script>

<style scoped>

#content{
  padding-bottom: 50px;
}
</style>