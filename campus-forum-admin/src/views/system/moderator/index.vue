<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>版主模块管理</span>
          <el-button type="primary" @click="openAssignDialog">
            <el-icon><Plus /></el-icon> 分配版主
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="搜索用户">
          <el-input v-model="searchKeyword" placeholder="昵称/用户名" clearable style="width: 200px;" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="业务模块">
          <el-select v-model="searchModuleCode" placeholder="全部模块" clearable style="width: 180px;">
            <el-option v-for="m in moduleList" :key="m.code" :label="m.name" :value="m.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="用户信息" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatar">{{ (row.nickname || '用').charAt(0) }}</el-avatar>
              <div class="user-info-text">
                <span class="nickname">{{ row.nickname || '-' }}</span>
                <span class="username">@{{ row.username || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="moduleName" label="负责模块" width="160">
          <template #default="{ row }">
            <el-tag>{{ row.moduleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="moduleCode" label="模块编码" width="170" />
        <el-table-column label="分配时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定移除此版主的模块权限？" @confirm="handleRemove(row.id)">
              <template #reference>
                <el-button type="danger" size="small" link>移除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 分配版主弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配版主模块" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="form.userId"
            filterable
            remote
            reserve-keyword
            placeholder="输入昵称/用户名搜索"
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%;"
          >
            <el-option v-for="u in userOptions" :key="u.id" :label="`${u.nickname} (@${u.username})`" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责模块" prop="moduleCode">
          <el-select v-model="form.moduleCode" placeholder="请选择业务模块" style="width: 100%;">
            <el-option v-for="m in moduleList" :key="m.code" :label="m.name" :value="m.code" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import http from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const searchModuleCode = ref('')
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 模块列表（6个业务模块）
const moduleList = [
  { code: 'market:manage', name: '二手市场管理' },
  { code: 'lostfound:manage', name: '失物招领管理' },
  { code: 'activity:manage', name: '活动管理' },
  { code: 'help:manage', name: '互助管理' },
  { code: 'info:news', name: '校园资讯管理' },
  { code: 'info:nav', name: '服务导航管理' }
]

// ===== 分配弹窗相关 =====
const assignDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ userId: null, moduleCode: '' })
const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  moduleCode: [{ required: true, message: '请选择负责模块', trigger: 'change' }]
}

// 用户搜索相关
const userSearchLoading = ref(false)
const userOptions = ref([])

// 加载版主列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchModuleCode.value) params.moduleCode = searchModuleCode.value

    const res = await http.get('/admin/system/moderators', { params })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (err) {
    console.error('加载版主列表失败:', err)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchKeyword.value = ''
  searchModuleCode.value = ''
  pagination.current = 1
  loadData()
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query || query.length < 1) return
  userSearchLoading.value = true
  try {
    const res = await http.get('/admin/system/users', { params: { current: 1, size: 20, keyword: query } })
    userOptions.value = res.records || []
  } catch (err) {
    console.error('搜索用户失败:', err)
  } finally {
    userSearchLoading.value = false
  }
}

// 打开分配弹窗
const openAssignDialog = () => {
  resetForm()
  assignDialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.userId = null
  form.moduleCode = ''
  userOptions.value = []
}

// 提交分配
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    // 找到选中的模块名称
    const selectedModule = moduleList.find(m => m.code === form.moduleCode)
    await http.post('/admin/system/moderators', {
      userId: form.userId,
      moduleCode: form.moduleCode,
      moduleName: selectedModule?.name || form.moduleCode
    })
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    loadData()
  } catch (err) {
    console.error('分配失败:', err)
  } finally {
    submitLoading.value = false
  }
}

// 移除版主
const handleRemove = async (id) => {
  try {
    await http.delete(`/admin/system/moderators/${id}`)
    ElMessage.success('已移除')
    loadData()
  } catch (err) {
    console.error('移除失败:', err)
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;

  .user-info-text {
    display: flex;
    flex-direction: column;

    .nickname {
      font-weight: 500;
      color: #333;
    }

    .username {
      font-size: 12px;
      color: #999;
    }
  }
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
