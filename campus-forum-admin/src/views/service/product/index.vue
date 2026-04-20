<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务分类管理（商品）</span>
          <el-button type="primary" size="small" @click="openCreateDialog">新增分类</el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" min-width="180" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="parentId" label="父分类ID" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              link
              @click="changeStatus(row, 0)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              link
              @click="changeStatus(row, 1)"
            >
              启用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="可填 Emoji" />
        </el-form-item>
        <el-form-item label="父分类ID">
          <el-input-number v-model="form.parentId" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createProductCategory,
  getProductCategories,
  updateProductCategory,
  updateProductCategoryStatus
} from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)

const form = reactive({
  categoryName: '',
  icon: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const resetForm = () => {
  form.categoryName = ''
  form.icon = ''
  form.parentId = 0
  form.sort = 0
  form.status = 1
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getProductCategories()
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  editingId.value = row.id
  form.categoryName = row.categoryName
  form.icon = row.icon
  form.parentId = row.parentId || 0
  form.sort = row.sort || 0
  form.status = row.status ?? 1
  dialogVisible.value = true
}

const submitForm = async () => {
  const payload = {
    categoryName: form.categoryName,
    icon: form.icon,
    parentId: form.parentId,
    sort: form.sort,
    status: form.status
  }

  if (editingId.value) {
    await updateProductCategory(editingId.value, payload)
    ElMessage.success('分类更新成功')
  } else {
    await createProductCategory(payload)
    ElMessage.success('分类创建成功')
  }

  dialogVisible.value = false
  loadData()
}

const changeStatus = async (row, status) => {
  await updateProductCategoryStatus(row.id, status)
  ElMessage.success('分类状态已更新')
  loadData()
}

loadData()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
