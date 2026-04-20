<template>
  <div class="page-card">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="queryParams" class="filter-form">
          <el-form-item label="分类">
            <el-select
              v-model="queryParams.categoryId"
              placeholder="请选择分类"
              clearable
              style="width: 140px"
              @change="handleSearch"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="queryParams.status"
              placeholder="请选择状态"
              clearable
              style="width: 140px"
              @change="handleSearch"
            >
              <el-option label="在售/开放中" :value="1" />
              <el-option label="已下架/已关闭/申请中" :value="0" />
              <el-option label="已售出" :value="2" />
              <el-option label="已预定/已匹配" :value="3" />
              <el-option label="已拒绝" :value="4" />
              <el-option label="未入选" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="信息类型">
            <el-select
              v-model="queryParams.tradeType"
              placeholder="全部"
              clearable
              style="width: 140px"
              @change="handleTradeTypeChange"
            >
              <el-option label="闲置在售" :value="1" />
              <el-option label="校园求购" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select
              v-model="queryParams.auditStatus"
              placeholder="请选择审核状态"
              clearable
              style="width: 140px"
              @change="handleSearch"
            >
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="queryParams.keyword"
              placeholder="标题/描述"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <div v-if="Number(queryParams.tradeType) === 2" class="wanted-quick-row">
          <span class="wanted-quick-label">求购快捷筛选：</span>
          <el-button size="small" :type="wantedQuickStatus === null ? 'primary' : 'default'" @click="applyWantedQuickFilter(null)">全部</el-button>
          <el-button size="small" :type="wantedQuickStatus === 1 ? 'success' : 'default'" @click="applyWantedQuickFilter(1)">开放中</el-button>
          <el-button size="small" :type="wantedQuickStatus === 3 ? 'warning' : 'default'" @click="applyWantedQuickFilter(3)">已匹配</el-button>
          <el-button size="small" :type="wantedQuickStatus === 0 ? 'info' : 'default'" @click="applyWantedQuickFilter(0)">已关闭</el-button>
          <el-button size="small" type="primary" plain @click="applyWantedPendingOpenPreset">待审核+开放中</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe v-loading="loading" height="600">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="商品标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
            <span v-if="row.originalPrice && row.originalPrice !== row.price" class="original-price">
              ¥{{ row.originalPrice }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            {{ row.categoryName || '未分类' }}
          </template>
        </el-table-column>
        <el-table-column label="信息类型" width="110">
          <template #default="{ row }">
            <el-tag :type="Number(row.tradeType) === 2 ? 'warning' : 'info'">
              {{ Number(row.tradeType) === 2 ? '校园求购' : '闲置在售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag
              :type="statusTagType(row)"
            >
              {{ statusLabel(row) }}
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
        <el-table-column prop="wantCount" label="想要数" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
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
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              link
              @click="changeStatus(row, 0)"
            >
              {{ Number(row.tradeType) === 2 ? '关闭' : '下架' }}
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
    <el-dialog v-model="detailDialogVisible" title="商品详情" width="800px" :close-on-click-modal="false">
      <div v-if="currentItem" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="商品标题" :span="2">{{ currentItem.title }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentItem.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ currentItem.seller?.nickname || currentItem.user?.nickname || currentItem.userId }}</el-descriptions-item>
          <el-descriptions-item label="是否可议价">
            {{ currentItem.isNegotiable === 1 ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="信息类型">
            {{ Number(currentItem.tradeType) === 2 ? '校园求购' : '闲置在售' }}
          </el-descriptions-item>
          <el-descriptions-item label="交易地点">{{ currentItem.tradeLocation }}</el-descriptions-item>
          <el-descriptions-item label="成色">
            {{
              currentItem.productCondition === 1 ? '全新' :
              currentItem.productCondition === 2 ? '几乎全新' :
              currentItem.productCondition === 3 ? '轻微使用' :
              currentItem.productCondition === 4 ? '正常使用' : '明显使用'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentItem)">
              {{ statusLabel(currentItem) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="currentItem.auditStatus === 1 ? 'success' : currentItem.auditStatus === 2 ? 'danger' : 'warning'">
              {{ currentItem.auditStatus === 1 ? '已通过' : currentItem.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ currentItem.viewCount }}</el-descriptions-item>
          <el-descriptions-item label="想要数">{{ currentItem.wantCount }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentItem.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="description-section">
          <h4>商品描述</h4>
          <p>{{ currentItem.description }}</p>
        </div>

        <div class="images-section" v-if="currentItem.images">
          <h4>商品图片</h4>
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
import { getProductAdminList, auditProduct, offProduct, deleteProduct } from '@/api/service'
import { getProductCategories } from '@/api/system'

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 10,
  categoryId: null,
  tradeType: null,
  status: null,
  auditStatus: null,
  keyword: ''
})

const wantedQuickStatus = ref(null)

const isOrderLikeRow = (row) => {
  return row && (
    Object.prototype.hasOwnProperty.call(row, 'orderNo') ||
    Object.prototype.hasOwnProperty.call(row, 'buyerId') ||
    Object.prototype.hasOwnProperty.call(row, 'sellerId')
  )
}

const statusLabel = (row) => {
  const status = Number(row?.status)
  const tradeType = Number(row?.tradeType)

  if (isOrderLikeRow(row)) {
    if (status === 0) return '申请中'
    if (status === 1) return '进行中'
    if (status === 2) return '已取消'
    if (status === 3) return '已完成'
    if (status === 4) return '已拒绝'
    if (status === 5) return '未入选'
    return '未知状态'
  }

  if (tradeType === 2) {
    if (status === 1) return '开放中'
    if (status === 3) return '已匹配'
    return '已关闭'
  }

  if (status === 1) return '在售'
  if (status === 2) return '已售出'
  if (status === 3) return '已预定'
  return '已下架'
}

const statusTagType = (row) => {
  const status = Number(row?.status)
  const tradeType = Number(row?.tradeType)

  if (isOrderLikeRow(row)) {
    if (status === 0) return 'info'
    if (status === 1) return 'warning'
    if (status === 2) return 'info'
    if (status === 3) return 'success'
    if (status === 4 || status === 5) return 'danger'
    return ''
  }

  if (tradeType === 2) {
    if (status === 1) return 'success'
    if (status === 3) return 'warning'
    return 'info'
  }

  if (status === 1) return 'success'
  if (status === 2) return 'info'
  if (status === 3) return 'warning'
  return 'danger'
}

// 分类数据
const categories = ref([])

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentItem = ref(null)
const parsedImages = ref([])

// 加载分类数据
const loadCategories = async () => {
  try {
    const res = await getProductCategories()
    categories.value = Array.isArray(res) ? res : []
  } catch (_) {
    categories.value = []
  }
}

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

    const res = await getProductAdminList(params)
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

const handleTradeTypeChange = () => {
  if (Number(queryParams.tradeType) !== 2) {
    wantedQuickStatus.value = null
  }
  handleSearch()
}

const applyWantedQuickFilter = (status) => {
  queryParams.tradeType = 2
  queryParams.status = status
  wantedQuickStatus.value = status
  handleSearch()
}

const applyWantedPendingOpenPreset = () => {
  queryParams.tradeType = 2
  queryParams.status = 1
  queryParams.auditStatus = 0
  wantedQuickStatus.value = 1
  handleSearch()
}

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    current: 1,
    size: 10,
    categoryId: null,
    tradeType: null,
    status: null,
    auditStatus: null,
    keyword: ''
  })
  wantedQuickStatus.value = null
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

    await auditProduct(row.id, auditStatus)
    ElMessage.success(`${auditStatus === 1 ? '通过' : '拒绝'}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

// 改变状态
const changeStatus = async (row, status) => {
  try {
    const isWanted = Number(row.tradeType) === 2
    const action = status === 0 ? (isWanted ? '关闭' : '下架') : (isWanted ? '重新开放' : '上架')
    await ElMessageBox.confirm(`确定要${action}这个商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (status === 0) {
      await offProduct(row.id)
    } else {
      // 如果有上架接口，可以调用
    }

    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除
const deleteItem = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteProduct(row.id)
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
  loadCategories()
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

.wanted-quick-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.wanted-quick-label {
  font-size: 13px;
  color: #606266;
}

.price {
  color: #e74c3c;
  font-weight: bold;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
  font-size: 12px;
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