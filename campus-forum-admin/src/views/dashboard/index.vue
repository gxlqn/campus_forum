<template>
  <div class="dashboard">
    <div class="welcome-header">
      <h1 class="welcome-title">管理中心控制台</h1>
      <p class="welcome-subtitle">欢迎回到校园讲坛后台，今日概览如下：</p>
    </div>

    <el-row :gutter="24" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" class="mb-20">
        <el-card shadow="hover" class="stat-card user-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">用户总数</div>
              <div class="stat-value">{{ stats.userTotal }}</div>
              <div class="stat-sub">已实名：{{ stats.verifiedUserTotal }}</div>
            </div>
            <div class="stat-icon-wrapper">
              <el-icon><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" class="mb-20">
        <el-card shadow="hover" class="stat-card post-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">帖子总量</div>
              <div class="stat-value">{{ stats.postTotal }}</div>
              <div class="stat-sub">评论：{{ stats.commentTotal }}</div>
            </div>
            <div class="stat-icon-wrapper">
              <el-icon><Collection /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" class="mb-20">
        <el-card shadow="hover" class="stat-card service-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">服务总量</div>
              <div class="stat-value">{{ stats.serviceTotal }}</div>
              <div class="stat-sub">商品/活动/互助/失物</div>
            </div>
            <div class="stat-icon-wrapper">
              <el-icon><Goods /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" class="mb-20">
        <el-card shadow="hover" class="stat-card warning-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">待处理事项</div>
              <div class="stat-value">{{ stats.pendingAuditTotal + stats.pendingReportTotal }}</div>
              <div class="stat-sub">待审核 {{ stats.pendingAuditTotal }} / 举报 {{ stats.pendingReportTotal }}</div>
            </div>
            <div class="stat-icon-wrapper">
              <el-icon><Bell /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24">
      <el-col :span="16" :xs="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="card-header-title">近 7 天增长趋势</span>
              <el-button type="primary" link icon="Refresh" @click="reloadData">刷新数据</el-button>
            </div>
          </template>
          <div ref="trendChartRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="8" :xs="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-header-title">板块帖子分布</span>
          </template>
          <div ref="pieChartRef" class="chart" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { User, Collection, Goods, Bell, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getOverviewStats, getSectionDistribution, getTrendStats } from '@/api/system'

const stats = reactive({
  userTotal: 0,
  verifiedUserTotal: 0,
  postTotal: 0,
  commentTotal: 0,
  serviceTotal: 0,
  pendingAuditTotal: 0,
  pendingReportTotal: 0
})

const trendChartRef = ref()
const pieChartRef = ref()
let trendChart = null
let pieChart = null

const renderTrendChart = (trendData = []) => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const xAxis = trendData.map((item) => item.date)
  const userSeries = trendData.map((item) => item.userCount)
  const postSeries = trendData.map((item) => item.postCount)
  const serviceSeries = trendData.map((item) => item.serviceCount)

  trendChart.setOption({
    color: ['#4F46E5', '#10B981', '#F59E0B'],
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#374151' },
      padding: [10, 15]
    },
    legend: { 
      data: ['新增用户', '新增帖子', '新增服务'],
      bottom: 0,
      itemGap: 24,
      icon: 'roundRect'
    },
    grid: { left: 40, right: 30, top: 40, bottom: 60, containLabel: true },
    xAxis: { 
      type: 'category', 
      data: xAxis,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#9ca3af' }
    },
    yAxis: { 
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#f3f4f6' } },
      axisLabel: { color: '#9ca3af' }
    },
    series: [
      { 
        name: '新增用户', 
        type: 'line', 
        smooth: true, 
        showSymbol: false,
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79, 70, 229, 0.2)' },
            { offset: 1, color: 'rgba(79, 70, 229, 0)' }
          ])
        },
        data: userSeries 
      },
      { 
        name: '新增帖子', 
        type: 'line', 
        smooth: true, 
        showSymbol: false,
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0)' }
          ])
        },
        data: postSeries 
      },
      { 
        name: '新增服务', 
        type: 'line', 
        smooth: true, 
        showSymbol: false,
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 158, 11, 0.2)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0)' }
          ])
        },
        data: serviceSeries 
      }
    ]
  })
}

const renderPieChart = (pieData = []) => {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  pieChart.setOption({
    color: ['#6366F1', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899'],
    tooltip: { 
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      padding: [10, 15]
    },
    legend: { 
      orient: 'horizontal',
      bottom: '0',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { fontSize: 12, color: '#666' }
    },
    series: [
      {
        name: '板块帖子分布',
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            formatter: '{b}\n{d}%'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData
      }
    ]
  })
}

const reloadData = async () => {
  const [overview, trend, sections] = await Promise.all([
    getOverviewStats(),
    getTrendStats(7),
    getSectionDistribution()
  ])

  Object.assign(stats, overview)
  renderTrendChart(trend || [])
  renderPieChart(sections || [])
}

onMounted(async () => {
  await nextTick()
  await reloadData()
  window.addEventListener('resize', handleResize)
})

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 120px);

  .welcome-header {
    margin-bottom: 32px;
    
    .welcome-title {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      margin-bottom: 8px;
    }
    
    .welcome-subtitle {
      font-size: 15px;
      color: #64748b;
    }
  }

  .stat-cards {
    margin-bottom: 12px;
  }

  .mb-20 {
    margin-bottom: 24px;
  }

  .stat-card {
    border: none;
    border-radius: 16px;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    color: #ffffff;
    overflow: hidden;
    position: relative;

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    .stat-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .stat-info {
      z-index: 2;
    }

    .stat-title {
      font-size: 14px;
      font-weight: 500;
      opacity: 0.85;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 32px;
      font-weight: 800;
      margin-bottom: 12px;
      letter-spacing: -1px;
    }

    .stat-sub {
      font-size: 12px;
      opacity: 0.7;
      font-weight: 400;
    }

    .stat-icon-wrapper {
      background: rgba(255, 255, 255, 0.15);
      border-radius: 12px;
      padding: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      transition: all 0.3s;
      z-index: 1;
    }

    &.user-card {
      background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    }
    &.post-card {
      background: linear-gradient(135deg, #0ea5e9 0%, #2563eb 100%);
    }
    &.service-card {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    }
    &.warning-card {
      background: linear-gradient(135deg, #f59e0b 0%, #ea580c 100%);
    }
  }

  .chart-card {
    border: none;
    border-radius: 20px;
    margin-bottom: 24px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);

    :deep(.el-card__header) {
      border-bottom: 1px solid #f1f5f9;
      padding: 18px 24px;
    }

    .card-header-title {
      font-size: 16px;
      font-weight: 600;
      color: #334155;
    }

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .chart {
      height: 400px;
      width: 100%;
    }
  }
}
</style>

