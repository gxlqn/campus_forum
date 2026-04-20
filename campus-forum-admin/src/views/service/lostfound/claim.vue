<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="认领状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px" @change="handleSearch">
            <el-option :value="0" label="待审核" />
            <el-option :value="1" label="已通过" />
            <el-option :value="2" label="已拒绝" />
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
        <div class="card-header">
          <span>失物认领申请管理</span>
        </div>
      </template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="lostFoundTitle" label="招领物品" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="description" label="认领理由/证据" min-width="200" show-overflow-tooltip />
        <el-table-column label="证据图片" width="120">
          <template #default="{ row }">
            <div v-if="row.images" class="image-preview">
              <el-image 
                v-for="(img, index) in parseImages(row.images)" 
                :key="index"
                :src="img" 
                :preview-src-list="parseImages(row.images)"
                fit="cover"
                style="width: 30px; height: 30px; margin-right: 5px; border-radius: 4px"
              />
            </div>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status].type">{{ statusMap[row.status].label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="primary" size="small" link @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" size="small" link @click="handleAudit(row, 2)">拒绝</el-button>
            </template>
            <el-button v-else type="info" size="small" link @click="viewDetail(row)">详情</el-button>
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

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditVisible"
      :title="auditType === 1 ? '通过认领申请' : '拒绝认领申请'"
      width="450px"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :placeholder="auditType === 1 ? '请输入审核通过的相关提示...' : '请说明拒绝原因...'"
            rows="3"
          />
        </el-form-item>
        <p v-if="auditType === 1" class="audit-tip">注意：通过认领后，该招领信息的状态将自动变更为“已认领”。</p>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button :type="auditType === 1 ? 'primary' : 'danger'" @click="submitAudit" :loading="submitting">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailVisible" title="认领申请详情" width="500px">
      <el-descriptions border :column="1">
        <el-descriptions-item label="物品名称">{{ currentDetail?.lostFoundTitle }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentDetail?.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="认领理由">{{ currentDetail?.description }}</el-descriptions-item>
        <el-descriptions-item label="证据图片">
          <div v-if="currentDetail?.images" class="image-preview" style="display: flex; gap: 8px; flex-wrap: wrap;">
            <el-image 
              v-for="(img, index) in parseImages(currentDetail.images)" 
              :key="index"
              :src="img" 
              :preview-src-list="parseImages(currentDetail.images)"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 4px"
            />
          </div>
          <span v-else>无证据图片</span>
        </el-descriptions-item>
        <el-descriptions-item label="处理结果" v-if="currentDetail?.status !== 0">
          <el-tag :type="statusMap[currentDetail?.status]?.type">{{ statusMap[currentDetail?.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" v-if="currentDetail?.status !== 0">
          {{ currentDetail?.auditRemark || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLostFoundClaimList, auditLostFoundClaim } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const submitting = ref(false)

const query = reactive({
  status: 0,
  current: 1,
  size: 10
})

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' }
}

const auditVisible = ref(false)
const auditType = ref(1)
const auditForm = reactive({
  id: null,
  remark: ''
})

const detailVisible = ref(false)
const currentDetail = ref(null)

const viewDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLostFoundClaimList({
      current: query.current,
      size: query.size,
      status: query.status
    })
    tableData.value = res.records
    total.value = res.total
  } catch (err) {
    console.error('加载数据失败', err)
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
  handleSearch()
}

const onPageChange = (val) => {
  query.current = val
  loadData()
}

const parseImages = (jsonStr) => {
  if (!jsonStr) return []
  try {
    const rawImages = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    if (!Array.isArray(rawImages)) return []
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    return rawImages.map(url => {
      if (url.startsWith('http')) return url
      let path = url
      if (!path.startsWith('/')) path = '/' + path
      if (!path.includes('/uploads/')) path = '/uploads' + path
      return baseUrl + path
    })
  } catch (e) {
    return []
  }
}

const getFullUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  let path = url
  if (!path.startsWith('/')) path = '/' + path
  if (!path.includes('/uploads/')) path = '/uploads' + path
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  return baseUrl + path
}

const handleAudit = (row, type) => {
  auditType.value = type
  auditForm.id = row.id
  auditForm.remark = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.id) return
  submitting.value = true
  try {
    await auditLostFoundClaim(auditForm.id, {
      auditStatus: auditType.value,
      auditRemark: auditForm.remark
    })
    ElMessage.success('操作成功')
    auditVisible.value = false
    loadData()
  } catch (err) {
    console.error('审核失败', err)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.audit-tip {
  margin-top: 10px;
  color: #ff4757;
  font-size: 12px;
}
.image-preview {
  display: flex;
  align-items: center;
}
</style>