<script setup lang="ts">

  import {ref} from "vue";
  import myAxios from "../plugins/myAxios.ts";
  import {showFailToast, showSuccessToast} from "vant";
  import {useRouter} from "vue-router";

  const router = useRouter();

  // 初始化表单数据
  const initFormData = {
    "description": "",
    "expireTime": "",
    "maxNum": 3,
    "name": "",
    "password": "",
    "status": 0,
  }

  // 使用扩展对象运算符和ref创建表单数据ref，避免原始数据被修改，方便重置数据
  const addTeamData = ref({...initFormData})

  const onSubmit = async () => {
    // 准备提交的数据，处理expireTime格式
    const postData = {
      ...addTeamData.value,
      status: Number(addTeamData.value.status),
      expireTime: addTeamData.value.expireTime.replace(/\//g, '-')
    }

    // TODO 前端参数校验
    const res = await myAxios.post('/team/add', postData);
    if(res?.code === 0 && res.data) {
      router.push({
        path: '/team',
        // 防止用户点击后退返回创建队伍页面
        replace: true
      });
    } else {
      showFailToast('队伍创建失败');
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

        <van-field name="stepper" label="最大人数">
          <template #input>
            <van-stepper v-model="addTeamData.maxNum" max="20"/>
          </template>
        </van-field>

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