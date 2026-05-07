<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="昵称/用户名/学号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="正常" />
            <el-option :value="0" label="禁用" />
            <el-option :value="2" label="待审核" />
          </el-select>
        </el-form-item>
        <el-form-item label="实名">
          <el-select v-model="query.isVerified" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="已认证" />
            <el-option :value="0" label="未认证" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <span>用户与权限管理</span>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="studentId" label="学号/工号" min-width="140" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getUserTypeText(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="170">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles || []"
              :key="`${row.id}-${role.roleId}`"
              type="info"
              class="role-tag"
            >
              {{ role.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实名认证" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isVerified === 1 ? 'success' : 'warning'">
              {{ row.isVerified === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 2 ? '待审核' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openRoleDialog(row)">分配角色</el-button>
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              link
              @click="changeUserStatus(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              link
              @click="changeUserStatus(row, 1)"
            >
              启用
            </el-button>
            <el-button
              v-if="row.isVerified !== 1"
              type="success"
              size="small"
              link
              @click="changeUserVerify(row, 1)"
            >
              通过实名
            </el-button>
            <el-button
              v-else
              type="warning"
              size="small"
              link
              @click="changeUserVerify(row, 0)"
            >
              取消实名
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          :current-page="query.current"
          :page-size="query.size"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="420px">
      <el-checkbox-group v-model="selectedRoleIds" v-loading="roleLoading">
        <el-checkbox v-for="role in roleOptions" :key="role.id" :label="role.id">
          {{ role.roleName }}（{{ role.roleCode }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { assignUserRoles, getRoleList, getUserList, updateUserStatus, updateUserVerify } from '@/api/system'

const loading = ref(false)
const roleLoading = ref(false)
const total = ref(0)
const tableData = ref([])
const roleOptions = ref([])

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: undefined,
  isVerified: undefined
})

const roleDialogVisible = ref(false)
const selectedRoleIds = ref([])
const currentUserId = ref(null)

const getUserTypeText = (type) => {
  if (type === 1) return '学生'
  if (type === 2) return '教职工'
  return '其他'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadRoleOptions = async () => {
  roleLoading.value = true
  try {
    roleOptions.value = await getRoleList()
  } finally {
    roleLoading.value = false
  }
}

const handleSearch = () => {
  query.current = 1
  loadData()
}

const handleReset = () => {
  query.keyword = ''
  query.status = undefined
  query.isVerified = undefined
  query.current = 1
  loadData()
}

const onPageChange = (page) => {
  query.current = page
  loadData()
}

const changeUserStatus = async (row, status) => {
  await updateUserStatus(row.id, status)
  ElMessage.success('用户状态已更新')
  loadData()
}

const changeUserVerify = async (row, isVerified) => {
  await updateUserVerify(row.id, isVerified)
  ElMessage.success('实名认证状态已更新')
  loadData()
}

const openRoleDialog = (row) => {
  currentUserId.value = row.id
  selectedRoleIds.value = (row.roles || []).map((item) => item.roleId)
  roleDialogVisible.value = true
}

const submitUserRoles = async () => {
  await assignUserRoles(currentUserId.value, selectedRoleIds.value)
  ElMessage.success('角色分配成功')
  roleDialogVisible.value = false
  loadData()
}

onMounted(async () => {
  await Promise.all([loadRoleOptions(), loadData()])
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.role-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
