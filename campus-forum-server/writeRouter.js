const fs = require('fs');
const content = "import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色权限', icon: 'UserFilled' }
      },
      {
        path: 'system/audit',
        name: 'SystemAudit',
        component: () => import('@/views/system/audit/index.vue'),
        meta: { title: '内容审核', icon: 'Document' }
      },
      {
        path: 'system/sensitive-words',
        name: 'SensitiveWords',
        component: () => import('@/views/system/sensitive-words/index.vue'),
        meta: { title: '敏感词库', icon: 'Warning' }
      },
      {
        path: 'system/report',
        name: 'SystemReport',
        component: () => import('@/views/system/report/index.vue'),
        meta: { title: '举报处理', icon: 'Warning' }
      },
      {
        path: 'forum/section',
        name: 'ForumSection',
        component: () => import('@/views/forum/section/index.vue'),
        meta: { title: '板块管理', icon: 'Grid' }
      },
      {
        path: 'forum/post',
        name: 'ForumPost',
        component: () => import('@/views/forum/post/index.vue'),
        meta: { title: '帖子管理', icon: 'Document' }
      },
      {
        path: 'service/product',
        name: 'ServiceProduct',
        component: () => import('@/views/service/product/index.vue'),
        meta: { title: '商品分类', icon: 'ShoppingCart' }
      },
      {
        path: 'service/product/manage',
        name: 'ServiceProductManage',
        component: () => import('@/views/service/product/manage.vue'),
        meta: { title: '商品管理', icon: 'ShoppingCart' }
      },
      {
        path: 'service/lostfound',
        name: 'ServiceLostFound',
        component: () => import('@/views/service/lostfound/index.vue'),
        meta: { title: '失物招领', icon: 'Search' }
      },
      {
        path: 'service/lostfound/claim',
        name: 'ServiceLostFoundClaim',
        component: () => import('@/views/system/lostfound-claim/index.vue'),
        meta: { title: '认领审核', icon: 'Checked' }
      },
      {
        path: 'service/activity',
        name: 'ServiceActivity',
        component: () => import('@/views/service/activity/index.vue'),
        meta: { title: '活动管理', icon: 'Calendar' }
      },
      {
        path: 'service/help',
        name: 'ServiceHelp',
        component: () => import('@/views/service/help/index.vue'),
        meta: { title: '互助管理', icon: 'Service' }
      },
      {
        path: 'service/help/arbitration',
        name: 'ServiceHelpArbitration',
        component: () => import('@/views/service/help/arbitration.vue'),
        meta: { title: '互助悬赏仲裁', icon: 'Warning' }
      },
      {
        path: 'info/news',
        name: 'InfoNews',
        component: () => import('@/views/info/news/index.vue'),
        meta: { title: '校园资讯', icon: 'Notification' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? \\ - 校园服务论坛管理后台\ : '校园服务论坛管理后台'

  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
";
fs.writeFileSync('D:/graduationProject/campus-forum/campus-forum-admin/src/router/index.js', content, 'utf8');