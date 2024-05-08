<script setup lang="ts">

  import { useRouter } from 'vue-router';
  import TeamCardList from "../components/TeamCardList.vue";
  import {onMounted, ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast} from "vant";

  const active = ref('public');
  const router = useRouter();
  const searchText = ref('');

  /**
   * 切换队伍查询状态
   * @param name 队伍状态
   */
  const onTabChange = (name: string) => {
    // 查询公开队伍
    if(name === 'public') {
      listTeam(searchText.value, 0);
    }
    // 查询加密队伍
    else if(name === 'encrypt'){
      listTeam(searchText.value, 2);
    }
  }

  /**
   * 跳转到加入队伍页面
   */
  const toAddTeam = () => {
    router.push({
      path: '/team/add'
    })
  }

  // 使用 ref 创建一个响应式数组来存储队伍列表
  const teamList = ref([]);

  /**
   * 请求队伍列表数据
   * @param {string} val 搜索文本，用于过滤队伍列表，默认为空字符串
   * @param {number} status 队伍状态，默认为 0 - 公开 / 2 - 加密
   * @returns {Promise<void>} 无返回值
   */
  const listTeam = async (val = '', status = 0) => {
    const res = await myAxios.get("/team/list", {
      params: {
        searchText: val,
        pageNum: 1,
        status
      },
    });
    if(res?.code === 0) {
      // 更新队伍列表
      teamList.value = res.data;
    } else {
      showFailToast('加载队伍失败，请刷新重试');
    }
  }

  /**
   * 在组件挂载后，通过 axios 获取队伍列表数据，
   * 并更新 teamList 响应式数组
   */
  onMounted(() => {
    listTeam()
  });

  /**
   * 处理搜索事件，调用listTeam刷新队伍列表
   * @param {string} val 用户输入的搜索文本
   */
  const onSearch = (val : string) => {
    listTeam(val);
  }
</script>

<template>
  <div id="teamPage">
    <van-search v-model="searchText" placeholder="搜索队伍" @search="onSearch" />
    <!-- 标签页组件   -->
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
    <team-card-list :teamList = "teamList"/>
    <!--    无队伍处理-->
    <van-empty v-if="teamList?.length < 1" description="暂无队伍" />
  </div>
</template>

<style scoped>

</style>