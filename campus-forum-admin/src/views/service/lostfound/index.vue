<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>失物招领管理</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="queryParams" class="filter-form">
          <el-form-item label="类型">
            <el-select v-model="queryParams.type" placeholder="请选择类型" clearable style="width: 140px" @change="handleSearch">
              <el-option label="寻物" :value="1" />
              <el-option label="招领" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px" @change="handleSearch">
              <el-option label="进行中" :value="1" />
              <el-option label="已关闭" :value="0" />
              <el-option label="已完成" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="queryParams.auditStatus" placeholder="请选择审核状态" clearable style="width: 140px" @change="handleSearch">
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="queryParams.keyword"
              placeholder="标题/物品名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe v-loading="loading" height="600">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'warning' : 'success'">
              {{ row.type === 1 ? '寻物' : '招领' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="itemName" label="物品名称" width="120" />
        <el-table-column prop="itemCategory" label="物品类别" width="120" />
        <el-table-column prop="lostLocation" label="地点" width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'primary' : row.status === 2 ? 'success' : 'info'"
            >
              {{ row.status === 1 ? '进行中' : row.status === 2 ? '已完成' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'"
            >
              {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewDetail(row)">查看</el-button>
            <el-button
              v-if="row.auditStatus === 0"
              type="success"
              size="small"
              link
              @click="auditItem(row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.auditStatus === 0"
              type="danger"
              size="small"
              link
              @click="auditItem(row, 2)"
            >
              拒绝
            </el-button>
            <el-button type="danger" size="small" link @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="失物招领详情" width="800px" :close-on-click-modal="false">
      <div v-if="currentItem" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="currentItem.type === 1 ? 'warning' : 'success'">
              {{ currentItem.type === 1 ? '寻物' : '招领' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="物品名称">{{ currentItem.itemName }}</el-descriptions-item>
          <el-descriptions-item label="物品类别">{{ currentItem.itemCategory }}</el-descriptions-item>
          <el-descriptions-item label="丢失地点">{{ currentItem.lostLocation }}</el-descriptions-item>
          <el-descriptions-item label="发布者">{{ currentItem.publisher?.nickname || currentItem.userId }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentItem.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentItem.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系微信">{{ currentItem.contactWechat }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentItem.status === 1 ? 'primary' : currentItem.status === 2 ? 'success' : 'info'">
              {{ currentItem.status === 1 ? '进行中' : currentItem.status === 2 ? '已完成' : '已关闭' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="currentItem.auditStatus === 1 ? 'success' : currentItem.auditStatus === 2 ? 'danger' : 'warning'">
              {{ currentItem.auditStatus === 1 ? '已通过' : currentItem.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentItem.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentItem.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="description-section">
          <h4>详细描述</h4>
          <p>{{ currentItem.description }}</p>
        </div>

        <div class="images-section" v-if="currentItem.images">
          <h4>图片</h4>
          <div class="image-list">
            <el-image
              v-for="(image, index) in parsedImages"
              :key="index"
              :src="image"
              :preview-src-list="parsedImages"
              class="item-image"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLostFoundAdminList, auditLostFound, deleteLostFound } from '@/api/service'

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  type: null,
  status: null,
  auditStatus: null,
  keyword: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentItem = ref(null)
const parsedImages = ref([])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 清除空值
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })

    const res = await getLostFoundAdminList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询（重置页码）
const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    current: 1,
    size: 10,
    type: null,
    status: null,
    auditStatus: null,
    keyword: ''
  })
  loadData()
}

// 分页大小改变
const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.current = 1
  loadData()
}

// 页码改变
const handleCurrentChange = (current) => {
  queryParams.current = current
  loadData()
}

// 查看详情
const viewDetail = (row) => {
  currentItem.value = row
  // 解析图片
  try {
    const rawImages = row.images ? JSON.parse(row.images) : []
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
    parsedImages.value = rawImages.map(url => {
      if (url.startsWith('http')) return url
      let path = url
      if (!path.startsWith('/')) path = '/' + path
      if (!path.includes('/uploads/')) path = '/uploads' + path
      return baseUrl + path
    })
  } catch (e) {
    parsedImages.value = []
  }
  detailDialogVisible.value = true
}

// 审核
const auditItem = async (row, auditStatus) => {
  try {
    await ElMessageBox.confirm(
      `确定要${auditStatus === 1 ? '通过' : '拒绝'}这条记录吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await auditLostFound(row.id, auditStatus)
    ElMessage.success(`${auditStatus === 1 ? '通过' : '拒绝'}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

// 删除
const deleteItem = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteLostFound(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-card {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.description-section,
.images-section {
  margin-top: 20px;
}

.description-section h4,
.images-section h4 {
  margin-bottom: 10px;
  color: #333;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.item-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
