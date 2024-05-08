<script setup lang="ts">
  import {ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast, showSuccessToast} from "vant";
  import {useRoute} from "vue-router";

  const route = useRoute();

  // 声明两个响应式变量用于存储用户账号和密码
  const userAccount = ref('');
  const userPassword = ref('');

  /** 提交登录表单的函数 */
  const onSubmit = async () => {
    // 使用myAxios发送登录请求
    const res = await myAxios.post('/user/login', {
      userAccount: userAccount.value,
      userPassword: userPassword.value,
    })
     console.log(res, "用户登录");
    if(res.code === 0 && res.data) {
      showSuccessToast('登录成功');
      // 跳转到登录前的页面
      const redirectUrl = route.query?.redirect as string ?? '/';
      window.location.href = redirectUrl;
    } else {
      showFailToast('登录失败');
    }
  };
</script>

<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ required: true, message: '请填写账号' }]"
      />
      <van-field
          v-model="userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<style scoped>

</style>