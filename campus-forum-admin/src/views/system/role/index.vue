<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <span>角色与权限管理</span>
      </template>

      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openPermissionDialog(row)">
              分配权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="520px">
      <div class="permission-tip" v-if="currentRole">
        当前角色：<b>{{ currentRole.roleName }}</b>（{{ currentRole.roleCode }}）
      </div>
      <el-checkbox-group v-model="selectedPermissionIds" v-loading="permissionLoading">
        <el-checkbox v-for="item in permissionList" :key="item.id" :label="item.id">
          {{ item.permissionName }}（{{ item.permissionCode }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  assignRolePermissions,
  getPermissionList,
  getRoleList,
  getRolePermissionIds
} from '@/api/system'

const loading = ref(false)
const permissionLoading = ref(false)
const roleList = ref([])
const permissionList = ref([])

const permissionDialogVisible = ref(false)
const selectedPermissionIds = ref([])
const currentRole = ref(null)

const loadRoles = async () => {
  loading.value = true
  try {
    roleList.value = await getRoleList()
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  permissionList.value = await getPermissionList()
}

const openPermissionDialog = async (role) => {
  currentRole.value = role
  permissionDialogVisible.value = true
  permissionLoading.value = true
  try {
    selectedPermissionIds.value = await getRolePermissionIds(role.id)
  } finally {
    permissionLoading.value = false
  }
}

const submitPermissions = async () => {
  await assignRolePermissions(currentRole.value.id, selectedPermissionIds.value)
  ElMessage.success('权限分配成功')
  permissionDialogVisible.value = false
}

onMounted(async () => {
  await Promise.all([loadRoles(), loadPermissions()])
})
</script>

<style scoped>
.permission-tip {
  margin-bottom: 12px;
  color: #666;
}
</style>
