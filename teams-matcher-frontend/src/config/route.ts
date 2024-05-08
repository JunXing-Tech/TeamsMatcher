// 定义路由，每个路由都需要映射到一个组件
import Index from "../pages/Index.vue";
import Team from "../pages/TeamPage.vue";
import User from "../pages/UserPage.vue";
import Search from "../pages/SearchPage.vue";
import UserEditPage from "../pages/UserEditPage.vue";
import SearchResultPage from "../pages/SearchResultPage.vue";
import UserLoginPage from "../pages/UserLoginPage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import UserTeamJoinPage from "../pages/UserTeamJoinPage.vue";
import UserTeamCreatePage from "../pages/UserTeamCreatePage.vue";

const routes = [
    { path: '/', component: Index},
    { path: '/user', title: '个人信息', component: User},
    { path: '/user/edit', title: '编辑信息', component: UserEditPage},
    { path: '/user/list', title: '用户列表', component: SearchResultPage},
    { path: '/user/login', title: '登录', component: UserLoginPage},
    { path: '/user/update', title: '更新信息', component: UserUpdatePage},
    { path: '/user/team/join', title: '加入队伍', component: UserTeamJoinPage},
    { path: '/user/team/create', title: '创建队伍', component: UserTeamCreatePage},
    { path: '/team', title: '发现队伍', component: Team},
    { path: '/search', title: '发现伙伴', component: Search},
    { path: '/team/add', title: '创建队伍', component: TeamAddPage},
    { path: '/team/update', title: '更新队伍', component: TeamUpdatePage}
]
export default routes;