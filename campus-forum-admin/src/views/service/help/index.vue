<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="订单状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
            <el-option :value="1" label="待接单" />
            <el-option :value="2" label="进行中" />
            <el-option :value="3" label="已完成" />
            <el-option :value="4" label="已取消" />
            <el-option :value="6" label="锁定中" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.auditStatus" clearable placeholder="全部" style="width: 140px">
            <el-option :value="0" label="待审核" />
            <el-option :value="1" label="已通过" />
            <el-option :value="2" label="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="标题/描述" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <span>互助帮帮管理（含历史记录）</span>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="title" label="需求标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="reward" label="悬赏" width="110" />
        <el-table-column prop="expectedTime" label="期望时间" min-width="170" />
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="双方确认" width="130">
          <template #default="{ row }">
            <el-tag :type="row.publisherConfirmed === 1 && row.helperConfirmed === 1 ? 'success' : 'warning'">
              {{ row.publisherConfirmed === 1 && row.helperConfirmed === 1 ? '已双确认' : `${row.publisherConfirmed === 1 ? '发已' : '发未'}/${row.helperConfirmed === 1 ? '接已' : '接未'}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'">
              {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 0" size="small" type="success" link @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" size="small" type="danger" link @click="handleAudit(row.id, 2)">拒绝</el-button>
            <span v-else>-</span>
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
import { ElMessage } from 'element-plus'
import { auditHelp, getHelpList } from '@/api/service'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  current: 1,
  size: 10,
  status: null,
  auditStatus: null,
  keyword: ''
})

const statusLabel = (status) => {
  if (status === 1) return '待接单'
  if (status === 2) return '进行中'
  if (status === 3) return '已完成'
  if (status === 4) return '已取消'
  if (status === 6) return '锁定中'
  return '未知'
}

const statusTagType = (status) => {
  if (status === 1) return 'info'
  if (status === 2) return 'warning'
  if (status === 3) return 'success'
  if (status === 4) return 'danger'
  if (status === 6) return 'info'
  return 'info'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getHelpList(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.current = 1
  loadList()
}

const handleReset = () => {
  query.current = 1
  query.status = null
  query.auditStatus = null
  query.keyword = ''
  loadList()
}

const onPageChange = (page) => {
  query.current = page
  loadList()
}

const handleAudit = async (id, status) => {
  await auditHelp(id, status)
  ElMessage.success('操作成功')
  loadList()
}

onMounted(() => {
  loadList()
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