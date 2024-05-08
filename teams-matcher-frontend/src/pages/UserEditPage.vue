<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {getCurrentUser} from "../services/user.ts";

// 使用vue-router的useRoute获取当前路由信息
const route = useRoute();
const router = useRouter();

// 使用ref创建一个响应式对象，存储编辑模式下的用户信息
// 初始化时，从路由的查询参数中加载editKey, currentValue, editName
const editUser = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName
})

const onSubmit = async () => {

  const currentUser = await getCurrentUser();

  if(!currentUser) {
    showFailToast('用户未登录');
    return;
  }
  const res = await myAxios.post('/user/update', {
    'id': currentUser.id,
    [editUser.value.editKey as string]: editUser.value.currentValue
  })
  if(res.code === 0 && res.data > 0) {
    router.back();
  } else {
    showFailToast('修改错误');
  }
};
</script>

<template>
  <!-- 提交表单的容器，表单提交时触发onSubmit事件 -->
  <van-form @submit="onSubmit">
    <!-- 表单输入域的容器，设置为内陷样式 -->
    <van-cell-group inset>
      <!-- 动态绑定编辑用户的当前值、编辑键、编辑名称和占位符 -->
      <!-- v-model 双向绑定输入框的值到变量 editUser.currentValue， 当用户在输入框中输入时，editUser.currentValue 的值会相应更新 -->
      <van-field
         v-if="editUser.editKey != 'gender'"
         v-model="editUser.currentValue as string"
        :name="editUser.editKey as string"
        :label="editUser.editName as string"
        :placeholder="`请输入${editUser.editName}`"
      />

      <van-field name="radio" label="性别" v-if="editUser.editKey == 'gender'">
        <template #input>
          <van-radio-group v-model="editUser.currentValue" direction="horizontal">
            <van-radio name="0">男</van-radio>
            <van-radio name="1">女</van-radio>
          </van-radio-group>
        </template>
      </van-field>

      <!-- 提交按钮，点击后触发表单的提交事件 -->
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-cell-group>
  </van-form>
</template>

<style scoped>

</style>