<script setup lang="ts">
import { ref } from 'vue';
import {useRouter} from "vue-router";

const router = useRouter();

/** 搜索框 */
const searchText = ref('');

/** 标签列表 */
const originTagList = [
  {
    text: '性别',
    children: [
      { text: '男', id: '男' },
      { text: '女', id: '女' },
    ],
  },
  {
    text: '年级',
    children: [
      { text: '大一', id: '大一' },
      { text: '大二', id: '大二' },
      { text: '大三', id: '大三' },
      { text: '大四', id: '大四' },
    ],
  },
  {
    text: '专业',
    children: [
      { text: '计算机科学与技术', id: '计算机科学与技术' },
      { text: '软件工程', id: '软件工程' },
      { text: '物联网工程', id: '物联网工程' },
      { text: '通信工程', id: '通信工程' },
    ],
  },
];

/** 搜索时，根据搜索内容过滤标签列表 */
let tagList = ref(originTagList);
// 该函数根据searchText的值过滤originTagList中的子标签，并将过滤后的结果更新到tagList中。
const onSearch = () => {
  // 1.通过map方法遍历originTagList中的每个父标签
  tagList.value = originTagList.map(parentTag => {
    // 2.创建一个临时的子标签数组tempChildrenTag和一个临时的父标签对象tempParentTag
    const tempChildrenTag = [...parentTag.children];
    const tempParentTag = {...parentTag};
    // 3.将父标签的子标签复制到tempChildrenTag中
    // 并使用filter方法过滤出包含searchText.value的子标签
    // 最后将过滤后的子标签赋值给tempParentTag的children属性
    tempParentTag.children = tempChildrenTag.filter(
        item => item.text.includes(searchText.value));
    // 4.将处理后的父标签返回
    return tempParentTag;
  });
}

/** 取消搜索 */
const onCancel = () => {
  searchText.value = '';
  tagList.value = originTagList;
}

/** 已选的标签的id和索引 */
const activeIds = ref([]);
const activeIndex = ref(0);

/** 关闭已选的标签 */
const doClose = (tag : any) => {
  /** filter()方法接收一个回调函数作为参数，该回调函数会对数组中的每个元素进行处理。
   * 在代码中，是判断当前元素是否等于被关闭的标签，
   * 如果不等于就将其保留在新数组中，最终返回一个新的数组，该数组中不包含被关闭的标签 */
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag
  });
};

/** 路由跳转至搜索结果页 */
const doSearchResult = () => {
  router.push({
    path: 'user/list',
    query: {
      tags: activeIds.value
    }
  })
}
</script>

<template>
  <!-- 搜索框 -->
  <form action="/">
    <van-search
        v-model="searchText"
        show-action
        placeholder="请输入搜索关键词"
        @search="onSearch"
        @cancel="onCancel"
    />
  </form>
  <!-- 标签的选择与展示 -->
  <van-divider content-position="left">已选择标签</van-divider>
  <div v-if="activeIds.length === 0">请选择标签</div>
  <!-- 标签的行列布局 -->
  <van-row gutter="16" style="padding: 0 16px">
    <van-col v-for="tag in activeIds" >
      <van-tag closeable size="medium" type="primary" @close="doClose(tag)">
        {{ tag }}
      </van-tag>
    </van-col>
  </van-row>

  <!-- 标签的分类选择 -->
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
  />
  <!-- 搜索按钮 -->
  <div style="padding: 12px">
    <van-button block type="primary" @click="doSearchResult">搜索标签</van-button>
  </div>

</template>

<style scoped>

</style>