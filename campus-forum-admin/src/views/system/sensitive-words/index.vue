<template>
  <div class="page-card">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="词库分类">
          <el-select v-model="query.category" clearable placeholder="全部分类" style="width: 160px" @change="loadData">
            <el-option :value="1" label="政治敏感" />
            <el-option :value="2" label="色情低俗" />
            <el-option :value="3" label="暴力恐怖" />
            <el-option :value="4" label="广告营销" />
            <el-option :value="5" label="其他违规" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词搜索">
          <el-input v-model="query.keyword" placeholder="输入敏感词" clearable @keyup.enter="loadData" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showAddDialog">+ 添加敏感词</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header-row">
          <span>敏感词库管理</span>
          <div class="header-stats">
            <el-tag type="info">共 {{ totalCount }} 条</el-tag>
            <el-tag size="small" :type="'danger'" v-if="levelStats[3] > 0">强级 {{ levelStats[3] }}</el-tag>
            <el-tag size="small" :type="'warning'" v-if="levelStats[2] > 0">中级 {{ levelStats[2] }}</el-tag>
            <el-tag size="small" :type="'info'" v-if="levelStats[1] > 0">弱级 {{ levelStats[1] }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="filteredData" stripe v-loading="loading" border size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="word" label="敏感词" width="150">
          <template #default="{ row }">
            <el-text type="danger">{{ row.word }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="categoryTagType(row.category)" size="small">{{ categoryText(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ levelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="replacement" label="替换词" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.replacement || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.isEnabled" :active-value="1" :inactive-value="0"
                       @change="toggleStatus(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="editRow(row)">编辑</el-button>
            <el-popconfirm title="确定删除此敏感词？" @confirm="deleteRow(row)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑敏感词' : '添加敏感词'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" placeholder="请输入敏感词" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option :value="1" label="政治敏感" />
            <el-option :value="2" label="色情低俗" />
            <el-option :value="3" label="暴力恐怖" />
            <el-option :value="4" label="广告营销" />
            <el-option :value="5" label="其他违规" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-radio-group v-model="form.level">
            <el-radio :label="1">弱级(标记可疑)</el-radio>
            <el-radio :label="2">中级(转人工)</el-radio>
            <el-radio :label="3">强级(直接拒绝)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="替换词">
          <el-input v-model="form.replacement" placeholder="可选，命中时自动替换（留空则不替换）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSensitiveWords, addSensitiveWord, updateSensitiveWord, deleteSensitiveWord } from '@/api/service'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const tableData = ref([])
const query = reactive({ category: null, keyword: '' })

const form = reactive({
  id: null,
  word: '',
  category: 1,
  level: 1,
  replacement: '',
  remark: '',
  isEnabled: 1
})

const formRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }]
}

const totalCount = computed(() => tableData.value.length)
const filteredData = computed(() => {
  let list = tableData.value
  if (query.keyword) {
    const kw = query.keyword.toLowerCase()
    list = list.filter(item => item.word.toLowerCase().includes(kw))
  }
  return list
})

const levelStats = computed(() => {
  const stats = { 1: 0, 2: 0, 3: 0 }
  tableData.value.forEach(item => {
    if (item.level && stats[item.level] !== undefined) stats[item.level]++
  })
  return stats
})

function categoryText(c) {
  const map = { 1: '政治敏感', 2: '色情低俗', 3: '暴力恐怖', 4: '广告营销', 5: '其他违规' }
  return map[c] || '未知'
}
function categoryTagType(c) {
  const map = { 1: 'danger', 2: '', 3: 'warning', 4: 'info', 5: '' }
  return map[c] || ''
}
function levelText(l) {
  const map = { 1: '弱级', 2: '中级', 3: '强级' }
  return map[l] || '-'
}
function levelTagType(l) {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[l] || ''
}

async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (query.category) params.category = query.category
    const res = await getSensitiveWords(params)
    tableData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (_) {
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.category = null
  query.keyword = ''
  loadData()
}

function resetForm() {
  Object.assign(form, { id: null, word: '', category: 1, level: 1, replacement: '', remark: '', isEnabled: 1 })
}

function showAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function editRow(row) {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateSensitiveWord(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await addSensitiveWord(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (_) {
    } finally {
      submitting.value = false
    }
  })
}

async function deleteRow(row) {
  try {
    await deleteSensitiveWord(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (_) {}
}

async function toggleStatus(row) {
  try {
    await updateSensitiveWord(row.id, row)
    ElMessage.success('状态已更新')
  } catch (_) {
    row.isEnabled = row.isEnabled === 1 ? 0 : 1
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-card { padding: 16px; }
.search-card { margin-bottom: 16px; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }
.header-stats { display: flex; gap: 6px; }
</style>
