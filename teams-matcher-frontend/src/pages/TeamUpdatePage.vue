<script setup lang="ts">

import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
// 获取当前路由信息
const route = useRoute();
// 从路由查询参数中获取队伍ID
const id = route.query.id;

/**
* 初始化表单数据的ref对象。
* 使用扩展对象运算符和ref创建表单数据ref，确保表单数据的原始状态不被直接修改，
* 同时方便在需要时重置数据。
*/
const addTeamData = ref({});

onMounted(async () => {
  if(id <= 0) {
    showFailToast('获取队伍信息失败');
    return
  }
  const res = await myAxios.get("/team/get", {
    params: {
      id
    }
  });
  if(res?.code === 0) {
    addTeamData.value = res.data;
  } else {
    showFailToast('获取队伍信息失败');
  }
})

/**
* 提交表单数据。
* 格式化提交数据，处理expireTime日期格式，然后发送到服务器。
* 根据服务器响应显示成功或失败提示，并可能跳转到列表页面。
*/
const onSubmit = async () => {
  // 准备提交的数据，处理expireTime格式
  const postData = {
    ...addTeamData.value,
    status: Number(addTeamData.value.status),
    expireTime: addTeamData.value.expireTime.replace(/\//g, '-')
  }

  // TODO 前端参数校验
  const res = await myAxios.post('/team/update', postData);
  if(res?.code === 0 && res.data) {
    showSuccessToast('队伍更新成功');
    router.push({
      path: '/team',
      // 防止用户点击后退返回创建队伍页面
      replace: true
    });
  } else {
    showFailToast('队伍更新失败');
  }
}

addTeamData.value.expireTime = ref('');
const showPicker = ref(false);
const onConfirm = ({ selectedValues }) => {
  addTeamData.value.expireTime = selectedValues.join('/');
  showPicker.value = false;
};

</script>

<template>
  <div id="teamAddPage">
    <van-form @submit="onSubmit">
      <van-cell-group inset>

        <van-field
            v-model="addTeamData.name"
            name="name"
            label="队伍名称"
            placeholder="请输入队伍名称"
            :rules="[{ required: true, message: '请输入队伍名称' }]"
        />

        <van-field
            v-model="addTeamData.description"
            rows="2"
            autosize
            label="队伍描述"
            type="textarea"
            maxlength="512"
            placeholder="请输入队伍描述"
            show-word-limit
        />

        <van-field
            v-model="addTeamData.expireTime"
            is-link
            readonly
            name="datePicker"
            label="过期时间"
            placeholder="点击选择过期时间"
            @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom">
          <van-date-picker
              @confirm="onConfirm"
              @cancel="showPicker = false"
              :min-date="new Date()"
          />
        </van-popup>

        <van-field name="radio" label="队伍状态">
          <template #input>
            <van-radio-group v-model="addTeamData.status" direction="horizontal">
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">私有</van-radio>
              <van-radio name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>

        <van-field
            v-if="Number(addTeamData.status) === 2"
            v-model="addTeamData.password"
            type="password"
            name="password"
            label="队伍密码"
            placeholder="队伍密码"
            :rules="[{ required: true, message: '请填写队伍密码' }]"
        />

      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>

</style>