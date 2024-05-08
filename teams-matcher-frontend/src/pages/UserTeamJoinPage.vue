<script setup lang="ts">
  import {onMounted, ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast} from "vant";

  const searchText = ref('');
  const teamList = ref([]);
  const active = ref('public');

  const listTeam = async (val = '', status = 0) => {
    const res = await myAxios.get("/team/list/my/join", {
      params: {
        searchText: val,
        pageNum: 1,
        status
      }
    });
    if(res?.code === 0) {
      teamList.value = res.data;
    } else {
      showFailToast('加载队伍失败，请重试');
    }
  }

  const onSearch = (val : string) => {
    listTeam(val);
  }

  const onTabChange = (name : string) => {
    if(name === 'public') {
      listTeam(searchText.value, 0);
    } else if (name === 'encrypt') {
      listTeam(searchText.value, 2);
    }
  }

  onMounted(() => {
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
    <team-card-list :teamList="teamList" />
    <van-empty v-if="teamList?.length < 1" description="数据为空"/>
  </div>
</template>

<style scoped>

</style>