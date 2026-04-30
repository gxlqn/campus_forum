<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="处理状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
            <el-option :value="0" label="待处理" />
            <el-option :value="1" label="已处理" />
            <el-option :value="2" label="已忽略" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="query.targetType" clearable placeholder="全部" style="width: 130px">
            <el-option :value="1" label="帖子" />
            <el-option :value="2" label="评论" />
            <el-option :value="3" label="用户" />
            <el-option :value="4" label="商品" />
            <el-option :value="5" label="活动" />
            <el-option :value="6" label="失物招领" />
            <el-option :value="7" label="互助" />
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
        <span>举报处理</span>
      </template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column label="目标类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ targetText(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="90" />
        <el-table-column label="目标内容" min-width="260">
          <template #default="{ row }">
            <div class="target-box">
              <div class="target-title">{{ row.targetTitle || '暂无标题' }}</div>
              <div class="target-meta">作者：{{ row.targetAuthorName || '未知' }}</div>
              <div class="target-meta" v-if="row.targetCreateTime">发布时间：{{ row.targetCreateTime }}</div>
              <div class="target-snapshot" v-if="row.targetContent">{{ row.targetContent }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="举报原因" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'">
              {{ row.status === 1 ? '已处理' : row.status === 2 ? '已忽略' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="处理人" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button v-if="row.targetType === 1" size="small" type="primary" link @click="reviewPost(row)">审查帖子</el-button>
            <el-button size="small" type="success" link @click="doHandle(row, 1)">标记已处理</el-button>
            <el-button size="small" type="info" link @click="doHandle(row, 2)">忽略</el-button>
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

    <el-dialog v-model="reviewDialogVisible" title="审查帖子" width="720px">
      <div v-if="currentReport" class="review-panel">
        <div class="review-item">
          <div class="review-label">举报原因</div>
          <div class="review-value">{{ currentReport.reason || '无' }}</div>
        </div>
        <div class="review-item">
          <div class="review-label">帖子标题</div>
          <div class="review-value">{{ currentReport.targetTitle || '暂无标题' }}</div>
        </div>
        <div class="review-item">
          <div class="review-label">帖子正文</div>
          <div class="review-content">{{ currentReport.targetContent || '暂无内容' }}</div>
        </div>
        <div class="review-item">
          <div class="review-label">作者</div>
          <div class="review-value">{{ currentReport.targetAuthorName || '未知' }}</div>
        </div>
      </div>

      <el-form label-width="110px" class="review-form">
        <el-form-item label="帖子处理结果">
          <el-radio-group v-model="reviewForm.postAuditStatus">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回并下线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="reviewForm.postAuditRemark" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
        <el-form-item label="举报结论">
          <el-input v-model="reviewForm.handleResult" type="textarea" :rows="3" placeholder="请输入举报处理说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReviewPost">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, handleReport, resolvePostReport } from '@/api/system'

const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const currentReport = ref(null)

const reviewForm = reactive({
  postAuditStatus: 2,
  postAuditRemark: '',
  handleResult: ''
})

const query = reactive({
  current: 1,
  size: 10,
  status: 0,
  targetType: undefined
})

const targetText = (type) => {
  const map = { 1: '帖子', 2: '评论', 3: '用户', 4: '商品', 5: '活动', 6: '失物招领', 7: '互助' }
  return map[type] || `类型${type}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReportList(query)
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
  query.status = 0
  query.targetType = undefined
  query.current = 1
  loadData()
}

const onPageChange = (page) => {
  query.current = page
  loadData()
}

const doHandle = async (row, status) => {
  const result = await ElMessageBox.prompt('请输入处理说明（选填）', '处理举报', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: status === 1 ? '如：已删除违规内容' : '如：证据不足，暂不处理'
  }).catch(() => null)
  if (!result) return

  await handleReport(row.id, {
    status,
    handleResult: result.value || (status === 1 ? '已处理' : '已忽略')
  })
  ElMessage.success('举报处理成功')
  loadData()
}

const reviewPost = async (row) => {
  currentReport.value = row
  reviewForm.postAuditStatus = 2
  reviewForm.postAuditRemark = row.targetAuditRemark || ''
  reviewForm.handleResult = '帖子已审查，举报已完成处理'
  reviewDialogVisible.value = true
}

const submitReviewPost = async () => {
  if (!currentReport.value) return
  reviewSubmitting.value = true
  try {
    await resolvePostReport(currentReport.value.id, {
      postAuditStatus: reviewForm.postAuditStatus,
      postAuditRemark: reviewForm.postAuditRemark,
      handleResult: reviewForm.handleResult || (reviewForm.postAuditStatus === 1 ? '帖子已通过审核，举报已完成处理' : '帖子已驳回并下线，举报已完成处理')
    })
    ElMessage.success('帖子审查完成')
    reviewDialogVisible.value = false
    loadData()
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.target-box {
  line-height: 1.45;
}

.target-title {
  font-weight: 600;
  color: #1f2937;
}

.target-meta {
  color: #6b7280;
  font-size: 12px;
}

.target-snapshot {
  margin-top: 4px;
  color: #374151;
  font-size: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.review-panel {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 16px;
  background: #fafafa;
}

.review-item {
  margin-bottom: 12px;
}

.review-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.review-value,
.review-content {
  font-size: 14px;
  color: #111827;
}

.review-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.review-form {
  margin-top: 6px;
}
</style>
