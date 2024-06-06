<script setup lang="ts">
  import {ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast, showSuccessToast} from "vant";
  import {useRouter} from "vue-router";

  const router = useRouter();

  // 声明两个响应式变量用于存储用户账号和密码
  const userAccount = ref('');
  const userPassword = ref('');
  const checkPassword = ref('');

  /** 提交登录表单的函数 */
  const onSubmit = async () => {
    // 使用myAxios发送登录请求
    const res = await myAxios.post('/user/register', {
      userAccount: userAccount.value,
      userPassword: userPassword.value,
      checkPassword: checkPassword.value
    })
     console.log(res, "用户注册");
    if(res.code === 0 && res.data) {
      showSuccessToast('注册成功');
      // 跳转到登录页面
      await router.push('/user/login');
    } else {
      showFailToast('注册失败');
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
          :rules="[{ required: true, message: '请输入密码' }]"
      />
      <van-field
          v-model="checkPassword"
          type="password"
          name="checkPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[{ required: true, message: '请再次输入密码' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        注册
      </van-button>
    </div>
  </van-form>
</template>

<style scoped>

</style>