<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>活动管理</span>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="activityType" label="类型" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="报名人数" width="110">
          <template #default="{ row }">{{ row.currentParticipants || 0 }}/{{ row.maxParticipants || '无限制' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'info'" size="small">
              {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 0" size="small" type="success" @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" size="small" type="danger" @click="handleAudit(row.id, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:20px;text-align:right;">
        <el-pagination background layout="prev, pager, next, total"
          :total="total" v-model:current-page="query.current" @current-change="loadList" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue"
import { getActivityList, auditActivity } from "@/api/service"
import { ElMessage } from "element-plus"

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ current: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getActivityList(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleAudit = async (id, status) => {
  try {
    await auditActivity(id, status)
    ElMessage.success("操作成功")
    loadList()
  } catch (_) {}
}

onMounted(() => loadList())
</script>
