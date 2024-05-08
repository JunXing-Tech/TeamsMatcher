<script setup lang="ts">
  import {useRouter} from "vue-router";
  import {onMounted, ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast} from "vant";
  import TeamCardList from "../components/TeamCardList.vue";

  const router = useRouter();
  // 定义搜索文本的响应式变量
  const searchText = ref("");
  const active = ref("public");

  // 跳转到创建队伍页面的函数
  const toAddTeam = () => {
    router.push({
      path: '/team/add'
    })
  };

  // 定义队伍列表的响应式变量
  const teamList = ref([]);

  /**
   * 请求我的队伍列表。
   * @param {string} val 搜索文本，用于过滤队伍列表，默认为空字符串
   * @returns {Promise<void>} 不返回任何内容
   */
  const listTeam = async (val = '', status = 0) => {
    const res = await myAxios.get("/team/list/my/create", {
      params: {
        searchText: val,
        pageNum: 1,
        status
      }
    });
    if(res?.code === 0) {
      teamList.value = res.data;
    } else {
      showFailToast('加载队伍失败，请重新加载');
    }
  }

  const onTabChange = (name : string) => {
    if(name === 'public') {
      listTeam(searchText.value, 0);
    } else if(name === 'encrypt') {
      listTeam(searchText.value, 2);
    }
  }

  // 处理搜索事件的函数
  const onSearch = (val : string) => {
    listTeam(val);
  }

  // 在组件挂载时调用，初始化队伍列表
  onMounted( () => {
    listTeam();
  })
</script>

<template>
  <div id="teamPage">
    <van-search v-model="searchText" placeholder="搜索队伍" @search="onSearch" />
    <van-tabs v-model:active="active" @change="onTabChange">
      <van-tab title="公开队伍" name="public"></van-tab>
      <van-tab title="加密队伍" name="encrypt"></van-tab>
    </van-tabs>
    <!-- 创建队伍按钮 -->
    <van-button
        class="add-team-button"
        icon="plus"
        type="primary"
        @click="toAddTeam"
    />
    <team-card-list :teamList="teamList" />
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>

<style scoped>

</style>