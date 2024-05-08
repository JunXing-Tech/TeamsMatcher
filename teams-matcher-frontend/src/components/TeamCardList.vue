<script setup lang="ts">
import {TeamType} from "../models/team";
import {teamStatusEnum} from "../constants/team.ts";
import {teamImage} from "../constants/team.ts";
import myAxios from "../plugins/myAxios.ts";
import {showFailToast, showSuccessToast} from "vant";
import 'vant/es/notify/style'
import 'vant/es/toast/style'
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user.ts";
import {useRouter} from "vue-router";

/**
 * 定义组件的属性接口，用于外部传入数据
 *
 * @property {TeamType[]} teamList - 队伍列表，包含多个TeamType类型的对象
 */
interface TeamCardListProps {
  teamList: TeamType[];
}

// 使用withDefaults函数为组件的props定义默认值
const props = withDefaults(defineProps<TeamCardListProps>(), {
  // @ts-ignore
  teamList: [] as TeamType[]
});

// 判断该用户是否为队伍队长，队长才能更新队伍信息
const currentUser = ref();
onMounted(async () => {
  currentUser.value = await getCurrentUser();
})

/**
 * 处理用户加入队伍的逻辑
 * @param teamId 队伍ID，用于请求加入队伍接口
 */
const doJoinTeam = async () => {
  if(!joinTeamId.value) {
    return;
  }
  const res = await myAxios.post('/team/join', {
    teamId : joinTeamId.value,
    password : password.value
  });
  if (res?.code === 0) {
    showSuccessToast('加入成功');
    doJoinCancel();
    // 操作成功，刷新页面
    location.reload();
  } else {
    showFailToast('加入失败' + res.description ? `${res.description}` : '');
  }
}

// 跳转到队伍信息更新页面，传入用户的队伍ID
const router = useRouter();
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/update',
    query: {
      id
    }
  })
}

/**
 * 退出队伍
 * @param id 队伍Id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quit', {
    teamId: id
  });
  if (res?.code === 0) {
    showSuccessToast('操作成功');
    // 操作成功，刷新页面
    location.reload();
  } else {
    showFailToast('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}

/**
 * 删除队伍
 * @param id 队伍Id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/delete', {
    id,
  });
  if (res?.code === 0) {
    showSuccessToast('操作成功');
    // 操作成功，刷新页面
    location.reload();
  } else {
    showFailToast('操作失败' + (res.description ? `，${res.description}` : ''));
  }
}

/**
 * 用户加入加密队伍时的密码输入框，默认为不显示（false）
 */
const showPasswordDialog = ref(false);
// 队伍密码
const password = ref('');
// 加入队伍的ID
const joinTeamId = ref(0);

/**
 * 取消加入队伍时，重置输入的信息
 */
const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
}

/**
 * 预处理加入队伍，如果是加密队伍，则弹出输入密码的对话框
 * @param team
 */
const preJoinTeam = (team : TeamType) => {
  joinTeamId.value = team.id;
  if(team.status === 0) {
    doJoinTeam();
  } else {
    showPasswordDialog.value = true;
  }
}

</script>

<template>
  <!-- 使用van-card组件循环展示每个团队的信息 -->
  <van-card
      v-for="team in props.teamList"
      :thumb="teamImage[Math.floor((Math.random() * 21))]"
      :desc="team.description"
      :title="team.name"
  >
    <!-- 标签模板，用于展示用户的标签 -->
    <template #tags>
      <van-tag plain type="danger" style="margin: 6px 6px">
        {{ teamStatusEnum[team.status] }}
      </van-tag>
    </template>
    <template #bottom>
      <div>
        {{ `队伍人数: ${team.hasJoinNum}/${team.maxNum}` }}
      </div>
      <div v-if="team.expireTime">
        {{ '过期时间' + team.expireTime }}
      </div>
      <div>
        {{ '创建时间' + team.createTime }}
      </div>
    </template>
    <!-- 底部模板，包含一个联系我按钮 -->
    <template #footer>
      <!-- 仅队伍创建用户可见 -->
      <van-button
          v-if="team.userId === currentUser?.id"
          size="small"
          type="primary"
          plain
          @click="doUpdateTeam(team.id)">
        更新队伍
      </van-button>

      <!-- 队伍创建用户不可见，仅已加入队伍的用户可见 -->
      <van-button
          size="small"
          plain
          v-if="team.userId !== currentUser?.id && team.hasJoin"
          @click="doQuitTeam(team.id)">
        退出队伍
      </van-button>

      <!-- 仅队伍创建用户可见 -->
      <van-button
          v-if="team.userId === currentUser?.id"
          size="small"
          type="danger"
          plain
          @click="doDeleteTeam(team.id)">
        解散队伍
      </van-button>

      <!-- 仅非队伍创建用户，且未加入队伍的用户可见 -->
      <van-button
          size="small"
          type="primary"
          v-if="team.userId !== currentUser?.id && !team.hasJoin"
          plain
          @click="preJoinTeam(team)">
        加入队伍
      </van-button>
    </template>
  </van-card>
  <!-- 弹出框 -->
  <van-dialog
      v-model:show="showPasswordDialog"
      title="队伍密码"
      show-cancel-button
      @confirm="doJoinTeam"
      @cancel="doJoinCancel">
    <van-field v-model="password" placeholder="请输入队伍密码"/>
  </van-dialog>
</template>

<style scoped>

</style>