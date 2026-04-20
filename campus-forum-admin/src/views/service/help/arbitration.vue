<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span>互助悬赏仲裁管理</span>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"/>
        <el-table-column prop="title" label="互助标题" min-width="180"/>
        <el-table-column label="发单人" width="110">
          <template #default="{ row }">{{ row.publisherName || `用户${row.userId}` }}</template>
        </el-table-column>
        <el-table-column label="接单人" width="110">
          <template #default="{ row }">{{ row.helperName || `用户${row.helperId}` || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reward" label="冻结金额" width="100">
          <template #default="{ row }">
            <span style="color: #e74c3c; font-weight: bold">{{ row.reward }} 元</span>
          </template>
        </el-table-column>
        <el-table-column prop="freezeTime" label="冻结时间" width="160"/>

        <el-table-column label="仲裁裁决" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleResolve(row.id, 1)">判发单人胜(退款)</el-button>
            <el-button size="small" type="success" @click="handleResolve(row.id, 2)">判接单人胜(打款)</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:20px;text-align:right;">
        <el-pagination background layout="prev, pager, next, total"
          :total="total" v-model:current-page="query.page" @current-change="loadList"/>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getArbitrationList, resolveArbitration } from '@/api/service'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({ page: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getArbitrationList(query)
    list.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const handleResolve = (id, winner) => {
  const winnerText = winner === 1 ? '发单人(全额退款)' : '接单人(全额打款)'
  ElMessageBox.confirm('确定将该冻结订单判给' + winnerText + '吗？此操作不可逆！', '仲裁确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await resolveArbitration({ id, resolution: winner })
      ElMessage.success('仲裁已裁决！')
      loadList()
    } catch (_) {}
  }).catch(() => {})
}

onMounted(() => loadList())
</script>
