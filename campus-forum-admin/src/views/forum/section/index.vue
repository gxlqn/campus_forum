<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="板块名/编码" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>板块管理</span>
          <el-button type="primary" size="small" @click="openCreateDialog">新增板块</el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="sectionName" label="板块名称" min-width="160" />
        <el-table-column prop="sectionCode" label="编码" min-width="130" />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="postCount" label="帖子数" width="90" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑板块' : '新增板块'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="板块名称" required>
          <el-input v-model="form.sectionName" placeholder="请输入板块名称" />
        </el-form-item>
        <el-form-item label="板块编码">
          <el-input v-model="form.sectionCode" placeholder="不填则自动生成" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="可填 Emoji 或图标字符" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="默认板块">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
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
import { createSection, getSectionList, updateSection, updateSectionStatus } from '@/api/system'

const loading = ref(false)
const keyword = ref('')
const tableData = ref([])

const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({
  sectionName: '',
  sectionCode: '',
  description: '',
  icon: '',
  sort: 0,
  status: 1,
  isDefault: 0
})

const emptyForm = () => {
  form.sectionName = ''
  form.sectionCode = ''
  form.description = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  form.isDefault = 0
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getSectionList({ keyword: keyword.value })
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  keyword.value = ''
  loadData()
}

const openCreateDialog = () => {
  editingId.value = null
  emptyForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  editingId.value = row.id
  form.sectionName = row.sectionName
  form.sectionCode = row.sectionCode
  form.description = row.description
  form.icon = row.icon
  form.sort = row.sort || 0
  form.status = row.status ?? 1
  form.isDefault = row.isDefault ?? 0
  dialogVisible.value = true
}

const submitForm = async () => {
  const payload = {
    sectionName: form.sectionName,
    sectionCode: form.sectionCode,
    description: form.description,
    icon: form.icon,
    sort: form.sort,
    status: form.status,
    isDefault: form.isDefault
  }

  if (editingId.value) {
    await updateSection(editingId.value, payload)
    ElMessage.success('板块更新成功')
  } else {
    await createSection(payload)
    ElMessage.success('板块创建成功')
  }

  dialogVisible.value = false
  loadData()
}

const changeStatus = async (row, status) => {
  await updateSectionStatus(row.id, status)
  ElMessage.success('板块状态已更新')
  loadData()
}

loadData()
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
