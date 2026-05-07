<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '260px'" class="layout-aside">
      <div class="logo-wrapper">
        <div class="logo">
          <img src="/logo.png" alt="logo" class="logo-img" />
          <span v-show="!isCollapse" class="logo-text">校园服务论坛</span>
        </div>
      </div>

      <div class="menu-wrapper">
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          background-color="transparent"
          text-color="#94a3b8"
          active-text-color="#ffffff"
          :collapse-transition="true"
          class="custom-menu"
          router
        >
          <!-- 仪表盘：所有已登录用户可见 -->
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <!-- 系统管理（根据权限动态过滤） -->
          <el-sub-menu v-if="hasAnyMenuPermission(systemMenus)" index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item v-for="item in systemMenus" :key="item.index"
                          :index="item.index" v-show="hasPermission(item.permission)">
              {{ item.label }}
            </el-menu-item>
            <!-- 版主管理仅 SUPER_ADMIN 可见 -->
            <el-menu-item v-if="isSuperAdmin" index="/system/moderator">版主管理</el-menu-item>
          </el-sub-menu>

          <!-- 论坛管理 -->
          <el-sub-menu v-if="hasAnyMenuPermission(forumMenus)" index="forum">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>论坛管理</span>
            </template>
            <el-menu-item v-for="item in forumMenus" :key="item.index"
                          :index="item.index" v-show="hasPermission(item.permission)">
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>

          <!-- 服务管理 -->
          <el-sub-menu v-if="hasAnyMenuPermission(serviceMenus)" index="service">
            <template #title>
              <el-icon><Service /></el-icon>
              <span>服务管理</span>
            </template>
            <el-menu-item v-for="item in serviceMenus" :key="item.index"
                          :index="item.index" v-show="hasPermission(item.permission)">
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>

          <!-- 信息管理 -->
          <el-sub-menu v-if="hasAnyMenuPermission(infoMenus)" index="info">
            <template #title>
              <el-icon><Notification /></el-icon>
              <span>信息管理</span>
            </template>
            <el-menu-item v-for="item in infoMenus" :key="item.index"
                          :index="item.index" v-show="hasPermission(item.permission)">
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <div class="collapse-btn-wrapper" @click="toggleCollapse" :class="{ 'is-active': !isCollapse }">
            <el-icon class="collapse-btn">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <el-breadcrumb separator="/" class="custom-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item><span class="breadcrumb-current">{{ $route.meta.title || '工作台' }}</span></el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <div class="header-actions">
            <el-tooltip content="通知中心" placement="bottom">
              <div class="action-item">
                <el-icon><Bell /></el-icon>
                <div class="badge"></div>
              </div>
            </el-tooltip>
          </div>

          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" src="" class="user-avatar">
                <template #default>管</template>
              </el-avatar>
              <div class="user-desc">
                <span class="user-name">{{ userInfo?.nickname || '管理员' }}</span>
                <el-tag v-if="userRoles.length > 0" size="small" type="primary" style="margin-left: 6px;">
                  {{ primaryRoleLabel }}
                </el-tag>
                <el-icon class="user-dropdown-icon"><CaretBottom /></el-icon>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided class="text-danger">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Odometer, Setting, ChatDotRound, Service, Notification, Fold, Expand, Bell, CaretBottom, User, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const isCollapse = ref(false)

const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
})

// 用户角色列表
const userRoles = computed(() => userInfo.value?.roles || [])

// 主角角色显示名
const primaryRoleLabel = computed(() => {
  const roleMap = {
    'SUPER_ADMIN': '超管',
    'ADMIN': '管理员',
    'MODERATOR': '版主',
    'MODERATOR_FORUM': '论坛版主',
    'MODERATOR_MARKET': '市场版主',
    'MODERATOR_LOSTFOUND': '失物招领版主',
    'MODERATOR_ACTIVITY': '活动版主',
    'MODERATOR_HELP': '互助版主',
    'MODERATOR_INFO': '资讯导航版主'
  }
  for (const r of userRoles.value) {
    if (roleMap[r]) return roleMap[r]
  }
  return '用户'
})

// 是否是超级管理员（可访问版主管理等系统配置功能）
const isSuperAdmin = computed(() => {
  return userRoles.value.includes('SUPER_ADMIN')
})

// 用户权限码列表
const userPermissions = computed(() => userInfo.value?.permissions || [])

// 权限检查方法
const hasPermission = (permission) => {
  // 无 permission 字段的菜单项始终可见（如仪表盘）
  if (!permission) return true
  // 超级管理员拥有所有权限
  if (userRoles.value.includes('SUPER_ADMIN')) return true
  // 普通管理员：有系统权限但无角色权限管理
  if (userRoles.value.includes('ADMIN')) {
    // 管理员不能访问角色/权限管理
    if (permission === 'system:role') return false
    return true
  }
  // 版主类角色（含 MODERATOR 和 MODERATOR_*）：严格按权限码校验
  const isModerator = userRoles.value.some(r => r === 'MODERATOR' || r.startsWith('MODERATOR_'))
  if (isModerator) {
    return userPermissions.value.includes(permission)
  }
  return false
}

const hasAnyMenuPermission = (menus) => {
  return menus.some(m => hasPermission(m.permission))
}

// ===== 各分组菜单定义（含权限标识） =====
const systemMenus = [
  { index: '/system/user', label: '用户管理', permission: 'system:user' },
  { index: '/system/role', label: '角色权限', permission: 'system:role' },
  { index: '/system/audit', label: '内容审核', permission: 'system:audit' },
  { index: '/system/sensitive-words', label: '敏感词库', permission: 'system:sensitive' },
  { index: '/system/report', label: '举报处理', permission: 'system:report' },
]

const forumMenus = [
  { index: '/forum/section', label: '板块管理', permission: 'forum:section' },
  { index: '/forum/post', label: '帖子管理', permission: 'forum:post' },
]

const serviceMenus = [
  { index: '/service/product', label: '商品分类', permission: 'service:category' },
  { index: '/service/product/manage', label: '商品管理', permission: 'market:manage' },
  { index: '/service/lostfound', label: '失物招领', permission: 'lostfound:manage' },
  { index: '/service/lostfound/claim', label: '认领审核', permission: 'lostfound:manage' },
  { index: '/service/activity', label: '活动管理', permission: 'activity:manage' },
  { index: '/service/help', label: '互助管理', permission: 'help:manage' },
  { index: '/service/help/arbitration', label: '互助仲裁', permission: 'help:manage' },
]

const infoMenus = [
  { index: '/info/news', label: '校园资讯', permission: 'info:news' },
  { index: '/info/nav', label: '服务导航', permission: 'info:nav' },
]

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  background-color: #f8fafc;
}

.layout-aside {
  background-color: #0f172a; /* 深蓝偏黑现代质感 */
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.08);
  z-index: 10;

  .logo-wrapper {
    height: 72px;
    padding: 0 20px;
    display: flex;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    background: rgba(0, 0, 0, 0.1);
  }

  .logo {
    display: flex;
    align-items: center;
    color: #fff;
    font-size: 18px;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 1px;

    .logo-img {
      width: 28px;
      height: 28px;
      margin-right: 12px;
      border-radius: 6px;
      background: #fff;
      padding: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
    
    .logo-text {
      background: linear-gradient(to right, #ffffff, #94a3b8);
      -webkit-background-clip: text;
      color: transparent;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  .custom-menu {
    border-right: none;
    
    :deep(.el-menu-item), :deep(.el-sub-menu__title) {
      margin: 4px 12px;
      border-radius: 8px;
      height: 48px;
      line-height: 48px;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
        color: #ffffff !important;
      }
    }

    :deep(.el-menu-item.is-active) {
      background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%) !important;
      color: #ffffff !important;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
      font-weight: 600;
    }
    
    :deep(.el-sub-menu) {
      .el-menu {
        background-color: transparent !important;
        padding: 4px 0;
      }
      .el-menu-item {
        height: 44px;
        line-height: 44px;
        margin: 2px 12px 2px 24px;
        min-width: 0;
      }
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
  z-index: 9;

  .header-left {
    display: flex;
    align-items: center;

    .collapse-btn-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: transparent;
      cursor: pointer;
      transition: all 0.2s ease;
      margin-right: 16px;
      
      &:hover {
        background: #f1f5f9;
        color: #3b82f6;
      }

      .collapse-btn {
        font-size: 20px;
        color: #64748b;
        transition: color 0.2s ease;
      }
      
      &:hover .collapse-btn {
        color: #3b82f6;
      }
    }

    .custom-breadcrumb {
      :deep(.el-breadcrumb__inner) {
        font-weight: 500;
        color: #64748b;
        
        &.is-link:hover {
          color: #3b82f6;
        }
      }
      .breadcrumb-current {
        color: #0f172a;
        font-weight: 600;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 24px;

    .header-actions {
      display: flex;
      align-items: center;
      
      .action-item {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        position: relative;
        color: #64748b;
        transition: all 0.2s;
        
        &:hover {
          background: #f1f5f9;
          color: #3b82f6;
        }
        
        .el-icon {
          font-size: 18px;
        }
        
        .badge {
          position: absolute;
          top: 8px;
          right: 8px;
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background-color: #ef4444;
          border: 1px solid #fff;
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 32px;
      transition: background 0.2s;
      
      &:hover {
        background: #f1f5f9;
      }

      .user-avatar {
        background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
        font-size: 16px;
        color: white;
      }

      .user-desc {
        display: flex;
        align-items: center;
        margin-left: 10px;
        
        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: #334155;
          margin-right: 4px;
        }
        
        .user-dropdown-icon {
          color: #94a3b8;
          font-size: 12px;
        }
      }
    }
  }
}

.layout-main {
  background-color: #f8fafc;
  padding: 24px;
  position: relative;
  
  /* 页面过渡动画 */
  .fade-transform-leave-active,
  .fade-transform-enter-active {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  .fade-transform-enter-from {
    opacity: 0;
    transform: translateX(-15px);
  }
  
  .fade-transform-leave-to {
    opacity: 0;
    transform: translateX(15px);
  }
}

.user-dropdown-menu {
  border-radius: 12px !important;
  padding: 8px !important;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1) !important;
  
  :deep(.el-dropdown-menu__item) {
    border-radius: 6px;
    margin: 2px 0;
    padding: 8px 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    &:hover {
      background-color: #f1f5f9;
      color: #3b82f6;
    }
    
    &.text-danger {
      color: #ef4444;
      &:hover {
        background-color: #fef2f2;
        color: #dc2626;
      }
    }
  }
}
</style>
