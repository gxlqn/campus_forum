<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 140px" @change="handleSearch">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px" @change="handleSearch">
            <el-option :value="1" label="已上架" />
            <el-option :value="0" label="已下架" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题搜索" clearable @keyup.enter="handleSearch" />
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
          <span>校园资讯管理</span>
          <el-button type="primary" @click="openCreate">新增资讯</el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="source" label="来源" width="140" show-overflow-tooltip />
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isTop === 1 ? 'warning' : 'info'">{{ row.isTop === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已上架' : '已下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row)">查看</el-button>
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="primary" size="small" link @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next, jumper"
          :total="total" :current-page="query.current" :page-size="query.size"
          @current-change="onPageChange" />
      </div>
    </el-card>

    <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增资讯' : '编辑资讯'" width="820px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如：教务/后勤/图书馆" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input v-model="form.source" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="原文链接">
              <el-input v-model="form.sourceUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图链接">
              <el-input v-model="form.coverImage" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="发布时间">
              <el-date-picker v-model="form.publishTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="可不填，默认当前时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="置顶">
              <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="form.status">
                <el-option :value="1" label="上架" />
                <el-option :value="0" label="下架" />
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="资讯详情" width="720px">
      <div v-if="currentItem">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="来源" :span="2">{{ currentItem.source || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间" :span="2">{{ currentItem.publishTime || currentItem.createTime }}</el-descriptions-item>
          <el-descriptions-item label="正文" :span="2">
            <div class="news-content" v-html="formatContent(currentItem.content)" />
          </el-descriptions-item>
          <el-descriptions-item label="封面图" :span="2" v-if="currentItem.coverImage">
            <el-image :src="getImageUrl(currentItem.coverImage)" fit="contain" style="max-width:400px; max-height:200px;" :preview-src-list="[getImageUrl(currentItem.coverImage)]" />
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminNews,
  deleteAdminNews,
  getAdminNewsCategories,
  getAdminNewsDetail,
  getAdminNewsList,
  updateAdminNews,
  updateAdminNewsStatus
} from '@/api/info'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])
const detailVisible = ref(false)
const currentItem = ref(null)
const editVisible = ref(false)
const editMode = ref('create')
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({ current: 1, size: 10, category: '', keyword: '', status: null })

const initialForm = () => ({
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  source: '',
  sourceUrl: '',
  category: '',
  isTop: 0,
  status: 1,
  publishTime: ''
})

const form = reactive(initialForm())

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach(k => { if (!params[k]) delete params[k] })
    const res = await getAdminNewsList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getAdminNewsCategories()
    categories.value = Array.isArray(res) ? res : []
  } catch (_) {}
}

const handleSearch = () => { query.current = 1; loadData() }
const handleReset = () => { query.category = ''; query.keyword = ''; query.status = null; query.current = 1; loadData() }
const onPageChange = (p) => { query.current = p; loadData() }

const viewDetail = (row) => { currentItem.value = row; detailVisible.value = true }

const openCreate = () => {
  editMode.value = 'create'
  editingId.value = null
  Object.assign(form, initialForm())
  editVisible.value = true
}

const openEdit = async (row) => {
  editMode.value = 'edit'
  editingId.value = row.id
  const detail = await getAdminNewsDetail(row.id)
  Object.assign(form, {
    title: detail.title || '',
    summary: detail.summary || '',
    content: detail.content || '',
    coverImage: detail.coverImage || '',
    source: detail.source || '',
    sourceUrl: detail.sourceUrl || '',
    category: detail.category || '',
    isTop: detail.isTop ?? 0,
    status: detail.status ?? 1,
    publishTime: detail.publishTime || ''
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
    const payload = { ...form }
    if (!payload.publishTime) {
      delete payload.publishTime
    }
    if (editMode.value === 'create') {
      await createAdminNews(payload)
      ElMessage.success('新增成功')
    } else {
      await updateAdminNews(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    editVisible.value = false
    loadData()
    loadCategories()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  await updateAdminNewsStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除资讯“${row.title}”？`, '删除确认', { type: 'warning' })
  await deleteAdminNews(row.id)
  ElMessage.success('删除成功')
  loadData()
  loadCategories()
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  return base + (url.startsWith('/') ? '' : '/') + url
}

const formatContent = (content) => content || '-'

onMounted(async () => { await Promise.all([loadCategories(), loadData()]) })
</script>

<style scoped>
.search-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.news-content { max-height: 500px; overflow-y: auto; line-height: 1.8; word-break: break-all; }
</style>
