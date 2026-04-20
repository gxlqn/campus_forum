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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReportList, handleReport } from '@/api/system'

const loading = ref(false)
const total = ref(0)
const tableData = ref([])

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
</style>
