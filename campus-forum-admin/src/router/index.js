import { createRouter, createWebHistory } from 'vue-router'

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
        meta: { title: '用户管理', icon: 'User', permission: 'system:user' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色权限', icon: 'UserFilled', permission: 'system:role' }
      },
      {
        path: 'system/audit',
        name: 'SystemAudit',
        component: () => import('@/views/system/audit/index.vue'),
        meta: { title: '内容审核', icon: 'Document', permission: 'system:audit' }
      },
      {
        path: 'system/sensitive-words',
        name: 'SensitiveWords',
        component: () => import('@/views/system/sensitive-words/index.vue'),
        meta: { title: '敏感词库', icon: 'Warning', permission: 'system:sensitive' }
      },
      {
        path: 'system/report',
        name: 'SystemReport',
        component: () => import('@/views/system/report/index.vue'),
        meta: { title: '举报处理', icon: 'Warning', permission: 'system:report' }
      },
      {
        path: 'system/moderator',
        name: 'SystemModerator',
        component: () => import('@/views/system/moderator/index.vue'),
        meta: { title: '版主管理', icon: 'UserFilled', permission: 'system:role' }
      },
      {
        path: 'forum/section',
        name: 'ForumSection',
        component: () => import('@/views/forum/section/index.vue'),
        meta: { title: '板块管理', icon: 'Grid', permission: 'forum:section' }
      },
      {
        path: 'forum/post',
        name: 'ForumPost',
        component: () => import('@/views/forum/post/index.vue'),
        meta: { title: '帖子管理', icon: 'Document', permission: 'forum:post' }
      },
      {
        path: 'service/product',
        name: 'ServiceProduct',
        component: () => import('@/views/service/product/index.vue'),
        meta: { title: '商品分类', icon: 'ShoppingCart', permission: 'service:category' }
      },
      {
        path: 'service/product/manage',
        name: 'ServiceProductManage',
        component: () => import('@/views/service/product/manage.vue'),
        meta: { title: '商品管理', icon: 'ShoppingCart', permission: 'market:manage' }
      },
      {
        path: 'service/lostfound',
        name: 'ServiceLostFound',
        component: () => import('@/views/service/lostfound/index.vue'),
        meta: { title: '失物招领', icon: 'Search', permission: 'lostfound:manage' }
      },
      {
        path: 'service/lostfound/claim',
        name: 'ServiceLostFoundClaim',
        component: () => import('@/views/service/lostfound/claim.vue'),
        meta: { title: '认领审核', icon: 'Checked', permission: 'lostfound:manage' }
      },
      {
        path: 'service/activity',
        name: 'ServiceActivity',
        component: () => import('@/views/service/activity/index.vue'),
        meta: { title: '活动管理', icon: 'Calendar', permission: 'activity:manage' }
      },
      {
        path: 'service/help',
        name: 'ServiceHelp',
        component: () => import('@/views/service/help/index.vue'),
        meta: { title: '互助管理', icon: 'Service', permission: 'help:manage' }
      },
      {
        path: 'service/help/arbitration',
        name: 'ServiceHelpArbitration',
        component: () => import('@/views/service/help/arbitration.vue'),
        meta: { title: '互助悬赏仲裁', icon: 'Warning', permission: 'help:manage' }
      },
      {
        path: 'info/news',
        name: 'InfoNews',
        component: () => import('@/views/info/news/index.vue'),
        meta: { title: '校园资讯', icon: 'Notification', permission: 'info:news' }
      },
      {
        path: 'info/nav',
        name: 'InfoNav',
        component: () => import('@/views/info/nav/index.vue'),
        meta: { title: '服务导航', icon: 'Guide', permission: 'info:nav' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 校园服务论坛管理后台` : '校园服务论坛管理后台'

  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 权限校验：如果路由定义了 permission，检查用户是否拥有该权限
  if (to.meta.permission) {
    const userInfo = localStorage.getItem('userInfo')
    let userPermissions = []
    let roles = []
    try {
      const parsed = JSON.parse(userInfo || '{}')
      userPermissions = parsed.permissions || []
      roles = parsed.roles || []

      // 超级管理员：全部放行
      if (roles.includes('SUPER_ADMIN')) {
        next()
        return
      }

      // 普通管理员：有除 system:role 外的全部系统权限，但角色管理页需拦截
      if (roles.includes('ADMIN')) {
        if (to.meta.permission === 'system:role') {
          // 管理员无权访问角色权限管理 / 版主管理（仅超管可见）
          next('/dashboard')
          return
        }
        next()
        return
      }

      // 版主类角色（MODERATOR / MODERATOR_*）：严格按权限码校验
      const isModerator = roles.some(r => r === 'MODERATOR' || r.startsWith('MODERATOR_'))
      if (isModerator) {
        if (!userPermissions.includes(to.meta.permission)) {
          next('/dashboard')
          return
        }
      }
    } catch (e) {
      // 解析失败，继续走权限检查
    }

    // 版主：严格按权限码校验
    if (!userPermissions.includes(to.meta.permission)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
