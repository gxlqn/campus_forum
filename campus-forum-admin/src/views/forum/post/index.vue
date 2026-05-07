<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题/内容" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="板块">
          <el-select v-model="query.sectionId" placeholder="全部" clearable style="width: 140px" @change="handleSearch">
            <el-option v-for="s in sections" :key="s.id" :label="s.sectionName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="正常" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header><span>帖子列表</span></template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="板块" width="120">
          <template #default="{ row }">{{ row.sectionName || '-' }}</template>
        </el-table-column>
        <el-table-column label="作者" width="100">
          <template #default="{ row }">{{ row.authorName || row.user?.nickname || `用户${row.userId}` }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论数" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info' " size="small">
              {{ row.status === 1 ? '正常' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewPost(row)">查看</el-button>
            <el-popconfirm title="确定删除该帖子？" @confirm="deletePost(row)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next, jumper"
          :total="total" :current-page="query.current" :page-size="query.size"
          @current-change="onPageChange" />
      </div>
    </el-card>

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="700px">
      <div v-if="currentPost">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentPost.id }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentPost.user?.nickname || `用户${currentPost.userId}` }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentPost.title }}</el-descriptions-item>
          <el-descriptions-item label="板块">{{ currentPost.sectionName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentPost.status === 1 ? 'success' : 'info'" size="small">
              {{ currentPost.status === 1 ? '正常' : '隐藏' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览">{{ currentPost.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="点赞">{{ currentPost.likeCount }}</el-descriptions-item>
          <el-descriptions-item label="评论数">{{ currentPost.commentCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="发布时间" :span="2">{{ currentPost.createTime }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">
            <div class="post-content">{{ currentPost.content }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const sections = ref([])
const detailVisible = ref(false)
const currentPost = ref(null)

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  sectionId: undefined,
  status: undefined
})

// 加载帖子列表
const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach(k => { if (params[k] === undefined || params[k] === '') delete params[k] })
    const res = await http.get('/forum/posts', { params })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 加载板块选项（用于筛选）
const loadSections = async () => {
  try {
    const res = await http.get('/forum/sections')
    sections.value = Array.isArray(res) ? res : []
  } catch (_) {}
}

const handleSearch = () => { query.current = 1; loadData() }
const handleReset = () => { query.keyword = ''; query.sectionId = undefined; query.status = undefined; query.current = 1; loadData() }
const onPageChange = (p) => { query.current = p; loadData() }

const viewPost = (row) => { currentPost.value = row; detailVisible.value = true }

const deletePost = async (row) => {
  try {
    await http.delete(`/forum/posts/${row.id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => { await Promise.all([loadSections(), loadData()]) })
</script>

<style scoped>
.search-card { margin-bottom: 20px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.post-content { max-height: 300px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; line-height: 1.6; }
</style>
