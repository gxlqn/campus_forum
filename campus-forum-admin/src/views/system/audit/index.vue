<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="内容类型">
          <el-select v-model="query.type" style="width: 140px" @change="handleSearch">
            <el-option label="帖子" value="post" />
            <el-option label="评论" value="comment" />
            <el-option label="商品" value="product" />
            <el-option label="活动" value="activity" />
            <el-option label="互助单" value="help" />
            <el-option label="失物招领" value="lostfound" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.auditStatus" clearable placeholder="全部" style="width: 140px">
            <el-option :value="0" label="待审核" />
            <el-option :value="1" label="已通过" />
            <el-option :value="2" label="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题/内容" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <span>内容审核与管理</span>
      </template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="authorName" label="发布者" width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ typeTextMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'">
              {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已驳回' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" min-width="180" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" link @click="approve(row)">通过</el-button>
            <el-button type="danger" size="small" link @click="reject(row)">驳回</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditItem, getAuditItems } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const typeTextMap = {
  post: '帖子',
  comment: '评论',
  product: '商品',
  activity: '活动',
  help: '互助单',
  lostfound: '失物招领'
}

const query = reactive({
  type: 'post',
  auditStatus: null,
  keyword: '',
  current: 1,
  size: 10
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAuditItems(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.current = 1
  loadData()
}

const handleReset = () => {
  query.auditStatus = null
  query.keyword = ''
  query.current = 1
  loadData()
}

const onPageChange = (page) => {
  query.current = page
  loadData()
}

const approve = async (row) => {
  await auditItem(row.type || query.type, row.id, { auditStatus: 1, auditRemark: '审核通过' })
  ElMessage.success('已审核通过')
  loadData()
}

const reject = async (row) => {
  const result = await ElMessageBox.prompt('请输入驳回原因（选填）', '驳回内容', {
    confirmButtonText: '确定驳回',
    cancelButtonText: '取消',
    inputPlaceholder: '如：广告、违规、信息不完整'
  }).catch(() => null)
  if (!result) return

  await auditItem(row.type || query.type, row.id, {
    auditStatus: 2,
    auditRemark: result.value || '审核驳回'
  })
  ElMessage.success('已驳回')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
