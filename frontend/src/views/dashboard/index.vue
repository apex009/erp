<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <template #header>
            <div class="card-header">
              <span>今日销售额</span>
              <el-tag type="success">日</el-tag>
            </div>
          </template>
          <div class="card-value">¥{{ todaySales }}</div>
          <div class="card-desc">日环比 <span :class="salesGrowth >= 0 ? 'up' : 'down'">{{ salesGrowth >= 0 ? '↑' : '↓' }} {{ Math.abs(salesGrowth) }}%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <template #header>
            <div class="card-header">
              <span>本月订单</span>
              <el-tag type="primary">月</el-tag>
            </div>
          </template>
          <div class="card-value">{{ monthOrders }}</div>
          <div class="card-desc">月同比 <span class="up">↑ 5%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <template #header>
            <div class="card-header">
              <span>库存预警</span>
              <el-tag type="danger">急</el-tag>
            </div>
          </template>
          <div class="card-value">{{ lowStockCount }}</div>
          <div class="card-desc">需补货商品数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="kpi-card">
          <template #header>
            <div class="card-header">
              <span>待处理商机</span>
              <el-tag type="warning">办</el-tag>
            </div>
          </template>
          <div class="card-value">{{ pendingLeads }}</div>
          <div class="card-desc">转化漏斗关键节点</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>销售趋势 (近30天)</span>
            </div>
          </template>
          <div id="chart-trend" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>销售漏斗</span>
            </div>
          </template>
          <div id="chart-funnel" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getSalesTrend, getSalesFunnel, getLowStock } from '@/api/report/index'

/* Use ref for reactive data */
const todaySales = ref(0)
const salesGrowth = ref(0)
const monthOrders = ref(1203) // Mock for now or add API
const lowStockCount = ref(0)
const pendingLeads = ref(45)  // Mock for now or add API

let trendChart = null
let funnelChart = null

const initCharts = async () => {
  // Init Charts Instances
  const trendDom = document.getElementById('chart-trend')
  const funnelDom = document.getElementById('chart-funnel')
  
  if (trendDom) {
      trendChart = echarts.init(trendDom)
  }
  if (funnelDom) {
      funnelChart = echarts.init(funnelDom)
  }

  try {
    // 1. Fetch & Render Sales Trend
    // Calculate dates for last 30 days
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - 30)
    
    const startStr = startDate.toISOString().split('T')[0]
    const endStr = endDate.toISOString().split('T')[0]
    
    // Fetch data
    let trendData = []
    try {
        const res = await getSalesTrend(startStr, endStr)
        if (res && Array.isArray(res)) {
            console.log('Trend Data from API:', res);
            trendData = res
        }
    } catch (e) {
        console.warn("Failed to load trend data, using mocks", e)
    }

    // Process data for chart
    let dates = []
    let amounts = []
    
    if (trendData.length > 0) {
        dates = trendData.map(d => d.statDate)
        amounts = trendData.map(d => d.amount)
        
        // Update KPI if we have data (Simple logic: last day is today)
        const lastDay = trendData[trendData.length - 1]
        if (lastDay) {
            todaySales.value = lastDay.amount
            // Simple growth calc vs yesterday
            const prevDay = trendData[trendData.length - 2]
            if (prevDay && prevDay.amount > 0) {
                salesGrowth.value = parseFloat(((lastDay.amount - prevDay.amount) / prevDay.amount * 100).toFixed(1))
            }
        }
    } else {
        // Fallback Mock Data
        for (let i = 0; i < 7; i++) {
            const d = new Date()
            d.setDate(d.getDate() - (6 - i))
            dates.push(d.toISOString().split('T')[0])
            amounts.push(Math.floor(Math.random() * 2000) + 1000)
        }
        todaySales.value = amounts[amounts.length-1]
    }

    if (trendChart) {
        trendChart.setOption({
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', boundaryGap: false, data: dates },
          yAxis: { type: 'value' },
          series: [{
            name: '销售额',
            data: amounts,
            type: 'line',
            smooth: true,
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                  { offset: 1, color: 'rgba(64, 158, 255, 0.01)' }
                ])
            },
            itemStyle: { color: '#409EFF' }
          }]
        })
    }

    // 2. Fetch & Render Funnel
    let funnelSeries = []
    try {
        const res = await getSalesFunnel()
        if (res && Array.isArray(res)) {
            // Map backend data (stage, count) to ECharts format (name, value)
            funnelSeries = res.map(item => ({
                name: item.stage,
                value: item.count
            }))
        }
    } catch (e) {
         console.warn("Failed to load funnel data", e)
    }
    
    if (funnelSeries.length === 0) {
        funnelSeries = [
             { value: 100, name: '初步接触' },
             { value: 80, name: '需求分析' },
             { value: 60, name: '方案报价' },
             { value: 40, name: '商务谈判' },
             { value: 20, name: '赢单' }
        ]
    }

    if (funnelChart) {
        funnelChart.setOption({
          tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c}' },
          series: [
            {
              name: '销售漏斗',
              type: 'funnel',
              left: '10%',
              top: 10,
              bottom: 10,
              width: '80%',
              min: 0,
              max: 100,
              minSize: '0%',
              maxSize: '100%',
              sort: 'descending',
              gap: 2,
              label: { show: true, position: 'inside' },
              labelLine: { length: 10, lineStyle: { width: 1, type: 'solid' } },
              itemStyle: { borderColor: '#fff', borderWidth: 1 },
              emphasis: { label: { fontSize: 20 } },
              data: funnelSeries
            }
          ]
        })
    }
    
    // 3. Low Stock Count
    try {
        const stocks = await getLowStock()
        if (stocks && Array.isArray(stocks)) {
            lowStockCount.value = stocks.length
        }
    } catch (e) {
        console.warn("Failed to load low stock data", e)
    }

  } catch (e) {
    console.error("Chart init failed", e)
  }
}

const handleResize = () => {
  trendChart && trendChart.resize()
  funnelChart && funnelChart.resize()
}

onMounted(() => {
  // Use a small timeout to ensure DOM is ready
  setTimeout(() => {
      initCharts()
      window.addEventListener('resize', handleResize)
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  funnelChart && funnelChart.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  
  .kpi-card {
      .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
      }
      .card-value {
          font-size: 28px;
          font-weight: bold;
          margin: 10px 0;
          color: #303133;
      }
      .card-desc {
          font-size: 14px;
          color: #909399;
      }
      .up { color: #67C23A; font-weight: bold; }
      .down { color: #F56C6C; font-weight: bold; }
  }
}
</style>
