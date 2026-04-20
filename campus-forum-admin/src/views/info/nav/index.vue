<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 160px" @change="handleSearch">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px" @change="handleSearch">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="名称/地址搜索" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务导航管理</span>
          <el-button type="primary" @click="openCreate">新增导航</el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="primary" size="small" link @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增导航' : '编辑导航'" width="760px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description" maxlength="255" show-word-limit />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="链接地址">
              <el-input v-model="form.url" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model="form.longitude" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model="form.latitude" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="停用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminNav,
  deleteAdminNav,
  getAdminNavCategories,
  getAdminNavList,
  updateAdminNav,
  updateAdminNavStatus
} from '@/api/info'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const editVisible = ref(false)
const editMode = ref('create')
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({
  current: 1,
  size: 10,
  category: '',
  keyword: '',
  status: null
})

const initialForm = () => ({
  category: '',
  name: '',
  description: '',
  icon: '',
  url: '',
  phone: '',
  address: '',
  longitude: '',
  latitude: '',
  sort: 0,
  status: 1
})

const form = reactive(initialForm())

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] === null || params[k] === undefined) {
        delete params[k]
      }
    })
    const res = await getAdminNavList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await getAdminNavCategories()
  categories.value = Array.isArray(res) ? res : []
}

const handleSearch = () => {
  query.current = 1
  loadList()
}

const handleReset = () => {
  query.current = 1
  query.category = ''
  query.keyword = ''
  query.status = null
  loadList()
}

const onPageChange = (page) => {
  query.current = page
  loadList()
}

const openCreate = () => {
  editMode.value = 'create'
  editingId.value = null
  Object.assign(form, initialForm())
  editVisible.value = true
}

const openEdit = (row) => {
  editMode.value = 'edit'
  editingId.value = row.id
  Object.assign(form, {
    category: row.category || '',
    name: row.name || '',
    description: row.description || '',
    icon: row.icon || '',
    url: row.url || '',
    phone: row.phone || '',
    address: row.address || '',
    longitude: row.longitude ?? '',
    latitude: row.latitude ?? '',
    sort: row.sort ?? 0,
    status: row.status ?? 1
  })
  editVisible.value = true
}

const resetForm = () => {
  Object.assign(form, initialForm())
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
  } catch (_) {
    return
  }

  saving.value = true
  try {
    const payload = {
      ...form,
      longitude: form.longitude === '' ? null : form.longitude,
      latitude: form.latitude === '' ? null : form.latitude
    }
    if (editMode.value === 'create') {
      await createAdminNav(payload)
      ElMessage.success('新增成功')
    } else {
      await updateAdminNav(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    editVisible.value = false
    await Promise.all([loadList(), loadCategories()])
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  await updateAdminNavStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  loadList()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除导航“${row.name}”？`, '删除确认', { type: 'warning' })
  await deleteAdminNav(row.id)
  ElMessage.success('删除成功')
  await Promise.all([loadList(), loadCategories()])
}

onMounted(async () => {
  await Promise.all([loadList(), loadCategories()])
})
</script>

<style scoped>
.search-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
