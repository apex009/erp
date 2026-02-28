<template>
  <div class="dashboard-container">
    <!-- 标题栏 -->
    <div class="dashboard-header">
      <span class="header-title">{{ todayStr }} 经营看板</span>
      <span class="header-time">最近更新时间 {{ updateTime }}</span>
    </div>

    <!-- 第一行：今日成交 + 销售目标 + 客户情况 -->
    <el-row :gutter="16">
      <el-col :span="10">
        <div class="panel" @click="goTo('/sales/orders', { date: todayDate })">
          <div class="panel-header">
            <el-tooltip content="统计口径：订单状态=已出库(status=3)，时间=出库完成时间(update_time)" placement="top">
              <span class="panel-title">今日成交</span>
            </el-tooltip>
            <a class="panel-link">查看详情</a>
          </div>
          <div class="panel-body">
            <div class="big-amount">{{ formatMoney(data.todaySalesAmount) }} 元</div>
            <el-row :gutter="12" class="sub-stats">
              <el-col :span="12">
                <div class="stat-label">成交订单</div>
                <div class="stat-value">{{ formatMoney(data.todaySalesAmount) }} 元 / {{ data.todaySalesOrderCount || 0 }} 单</div>
              </el-col>
              <el-col :span="12">
                <div class="stat-label">销售出库</div>
                <div class="stat-value">{{ formatMoney(data.todaySalesOutAmount) }} 元 / {{ data.todaySalesOutCount || 0 }} 单</div>
              </el-col>
              <el-col :span="12" style="margin-top: 8px;">
                <div class="stat-label">销售退货</div>
                <div class="stat-value">{{ formatMoney(data.todaySalesReturnAmount) }} 元 / {{ data.todaySalesReturnCount || 0 }} 单</div>
              </el-col>
              <el-col :span="12" style="margin-top: 8px;">
                <div class="stat-label">毛利</div>
                <div class="stat-value">{{ formatMoney(data.todaySalesProfit) }} 元</div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="panel" @click="goTo('/sales/orders')">
          <div class="panel-header">
            <span class="panel-title">销售目标</span>
          </div>
          <div class="panel-body target-body">
            <div class="target-chart-area">
              <DashboardPieChart :option="targetChartOption" height="140px" :loading="loading" :isEmpty="!dataLoaded" />
            </div>
            <div class="target-info">
              <div class="target-row"><span class="target-label">达成金额</span><span class="target-val">{{ formatMoney(data.todaySalesAmount) }}</span></div>
              <div class="target-row"><span class="target-label">目标金额</span><span class="target-val">{{ formatMoney(data.salesTarget) }}</span></div>
              <div class="target-row">
                <span class="target-label">同比</span>
                <span :class="['target-val', data.yoyGrowth >= 0 ? 'color-green' : 'color-red']">{{ formatGrowth(data.yoyGrowth) }} %</span>
              </div>
              <div class="target-row">
                <span class="target-label">环比</span>
                <span :class="['target-val', data.momGrowth >= 0 ? 'color-blue' : 'color-red']">{{ formatGrowth(data.momGrowth) }} %</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">客户情况</span>
          </div>
          <div class="panel-body customer-body">
            <div class="customer-block clickable" @click="goTo('/base/customers', { date: todayDate })">
              <div class="customer-num color-blue">{{ data.newCustomerCount || 0 }}</div>
              <div class="customer-label">新增客户</div>
            </div>
            <div class="customer-divider"></div>
            <div class="customer-block clickable" @click="goTo('/inventory/stocks', { lowStock: 'true' })">
              <div class="customer-num color-orange">{{ lowStockCount }}</div>
              <div class="customer-label">库存预警</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行：采购情况 + 库存情况 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="14">
        <div class="panel" @click="goTo('/purchase/orders', { date: todayDate })">
          <div class="panel-header">
            <span class="panel-title">采购情况</span>
            <a class="panel-link">查看详情</a>
          </div>
          <div class="panel-body purchase-body">
            <div class="purchase-chart-area">
              <DashboardPieChart :option="purchaseChartOption" height="200px" :loading="loading" :isEmpty="!dataLoaded" />
            </div>
            <div class="purchase-stats">
              <div class="purchase-stat-item">
                <span class="purchase-stat-label">采购订单</span>
                <span class="purchase-stat-value">{{ formatMoney(data.todayPurchaseAmount) }} 元 / {{ data.todayPurchaseOrderCount || 0 }} 单</span>
              </div>
              <div class="purchase-stat-item">
                <span class="purchase-stat-label">采购入库</span>
                <span class="purchase-stat-value">{{ formatMoney(data.todayPurchaseInAmount) }} 元 / {{ data.todayPurchaseInCount || 0 }} 单</span>
              </div>
              <div class="purchase-stat-item">
                <span class="purchase-stat-label">采购退货</span>
                <span class="purchase-stat-value">{{ formatMoney(data.todayPurchaseReturnAmount) }} 元 / {{ data.todayPurchaseReturnCount || 0 }} 单</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="panel" @click="goTo('/inventory/stocks')">
          <div class="panel-header">
            <span class="panel-title">库存情况</span>
          </div>
          <div class="panel-body inventory-body">
            <div class="inventory-chart-area">
              <DashboardPieChart :option="inventoryChartOption" height="180px" :loading="loading" :isEmpty="!dataLoaded" />
            </div>
            <div class="inventory-stats">
              <div class="inventory-stat-row">
                <span class="inv-label">库存金额</span>
                <span class="inv-value color-blue">{{ formatMoney(data.inventoryTotalAmount) }} 元</span>
              </div>
              <div class="inventory-stat-row">
                <span class="inv-label">商品总数</span>
                <span class="inv-value color-green">{{ data.productTotalCount || 0 }} 款</span>
              </div>
              <div class="inventory-stat-row">
                <span class="inv-label">库存预警</span>
                <span class="inv-value color-red">{{ lowStockCount }} 款</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第三行：销售漏斗 + 成交排行榜 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="14">
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">销售漏斗（最近30天）</span>
            <a class="panel-link" @click.stop="goTo('/sales/leads')">查看详情</a>
          </div>
          <div class="panel-body funnel-container">
            <div class="funnel-chart-area">
              <DashboardFunnelChart :option="funnelChartOption" height="300px" :loading="loading" :isEmpty="funnelEmpty" />
            </div>
            <div class="funnel-detail">
              <div class="funnel-detail-header">
                <span>阶段</span><span>线索数</span><span>预期金额</span><span>转化率</span>
              </div>
              <div v-for="item in funnelData" :key="item.stageCode" class="funnel-detail-row">
                <span class="fd-stage">{{ item.stageName }}</span>
                <span class="fd-count">{{ item.count }}</span>
                <span class="fd-amount">{{ formatMoney(item.amount) }}</span>
                <span class="fd-rate" :class="{ 'rate-good': item.conversionRate > 50 }">
                  {{ item.conversionRate != null ? item.conversionRate.toFixed(1) + '%' : '-' }}
                </span>
              </div>
              <div v-if="funnelEmpty" class="funnel-detail-empty">暂无数据</div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="panel rank-panel">
          <div class="panel-header">
            <span class="panel-title">🏆 今日成交排行</span>
            <span class="rank-tip">每5分钟更新</span>
          </div>
          <div class="panel-body rank-body">
            <div v-if="rankList.length === 0 && dataLoaded" class="rank-empty">暂无成交数据</div>
            <div v-for="(item, idx) in rankList" :key="item.userId" class="rank-row" :class="{ 'rank-top3': idx < 3 }">
              <div class="rank-index" :class="'rank-' + (idx + 1)">
                <template v-if="idx < 3">
                  {{ ['🥇','🥈','🥉'][idx] }}
                </template>
                <template v-else>{{ idx + 1 }}</template>
              </div>
              <div class="rank-name">{{ item.nickname }}</div>
              <div class="rank-amount">{{ formatMoney(item.amount) }} 元</div>
              <div class="rank-count">{{ item.orderCount }} 单</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第四行：今日收款 + 今日付款 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <div class="panel" @click="goTo('/finance/receivables')">
          <div class="panel-header">
            <span class="panel-title">今日收款（总计：{{ formatMoney(financeReceivable.totalAmount) }} 元）</span>
          </div>
          <div class="panel-body">
            <DashboardBarChart :option="receivableChartOption" height="200px" :loading="loading" :isEmpty="!dataLoaded" />
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="panel" @click="goTo('/finance/payables')">
          <div class="panel-header">
            <span class="panel-title">今日付款（总计：{{ formatMoney(financePayable.totalAmount) }} 元）</span>
          </div>
          <div class="panel-body">
            <DashboardBarChart :option="payableChartOption" height="200px" :loading="loading" :isEmpty="!dataLoaded" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DashboardPieChart from '@/components/charts/DashboardPieChart.vue'
import DashboardBarChart from '@/components/charts/DashboardBarChart.vue'
import DashboardFunnelChart from '@/components/charts/DashboardFunnelChart.vue'
import { getDashboardSummary, getLowStock, getFinanceReceivable, getFinancePayable, getSalesFunnel, getSalesRank } from '@/api/report/index'

const router = useRouter()
const loading = ref(true)
const dataLoaded = ref(false)
const lowStockCount = ref(0)
const funnelData = ref([])
const funnelEmpty = computed(() => funnelData.value.every(d => d.count === 0) && dataLoaded.value)
const rankList = ref([])

const data = reactive({
  todaySalesAmount: 0, todaySalesOrderCount: 0,
  todaySalesOutAmount: 0, todaySalesOutCount: 0,
  todaySalesReturnAmount: 0, todaySalesReturnCount: 0,
  todaySalesProfit: 0,
  salesTarget: 0, achieveRate: 0, yoyGrowth: 0, momGrowth: 0,
  todayPurchaseAmount: 0, todayPurchaseOrderCount: 0,
  todayPurchaseInAmount: 0, todayPurchaseInCount: 0,
  todayPurchaseReturnAmount: 0, todayPurchaseReturnCount: 0,
  newCustomerCount: 0,
  inventoryTotalAmount: 0, productTotalCount: 0,
  todayReceivableAmount: 0, todayPayableAmount: 0
})

const financeReceivable = reactive({ totalAmount: 0, paidAmount: 0, unpaidAmount: 0 })
const financePayable = reactive({ totalAmount: 0, paidAmount: 0, unpaidAmount: 0 })

const now = new Date()
const todayStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
const todayDate = now.toISOString().split('T')[0]
const updateTime = ref(`${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`)

const formatMoney = (val) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatGrowth = (val) => {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

const goTo = (path, query = {}) => {
  router.push({ path, query })
}

// ====== Chart Options ======

const targetChartOption = computed(() => {
  const target = data.salesTarget || 1
  const achieved = data.todaySalesAmount || 0
  const rate = data.achieveRate != null ? Number(data.achieveRate).toFixed(1) : '0.0'
  return {
    series: [{
      type: 'pie', radius: ['60%', '80%'], center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: { show: true, position: 'center', formatter: `${rate}%\n成交达成率`, fontSize: 14, fontWeight: 'bold', color: '#409EFF', lineHeight: 22 },
      data: [
        { value: achieved, name: '已达成', itemStyle: { color: '#409EFF' } },
        { value: Math.max(0, target - achieved), name: '未达成', itemStyle: { color: '#EBEEF5' } }
      ]
    }]
  }
})

const purchaseChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 元' },
  series: [{
    type: 'pie', radius: ['55%', '75%'], center: ['50%', '45%'],
    label: {
      show: true, position: 'center',
      formatter: `{a|${formatMoney(data.todayPurchaseAmount)}}\n{b|今日采购总额}`,
      rich: { a: { fontSize: 16, fontWeight: 'bold', color: '#303133', lineHeight: 26 }, b: { fontSize: 12, color: '#909399', lineHeight: 20 } }
    },
    data: [
      { value: data.todayPurchaseAmount || 0, name: '采购订单', itemStyle: { color: '#409EFF' } },
      { value: data.todayPurchaseInAmount || 0, name: '已入库', itemStyle: { color: '#67C23A' } },
      { value: data.todayPurchaseReturnAmount || 0, name: '退货', itemStyle: { color: '#E6A23C' } }
    ]
  }]
}))

const inventoryChartOption = computed(() => ({
  series: [{
    type: 'pie', radius: ['55%', '75%'], center: ['50%', '50%'],
    label: {
      show: true, position: 'center',
      formatter: `{a|${formatMoney(data.inventoryTotalAmount)}}\n{b|当前库存总额}`,
      rich: { a: { fontSize: 14, fontWeight: 'bold', color: '#303133', lineHeight: 24 }, b: { fontSize: 12, color: '#909399', lineHeight: 20 } }
    },
    data: [{ value: data.inventoryTotalAmount || 1, name: '库存', itemStyle: { color: '#EBEEF5' } }]
  }]
}))

const FUNNEL_COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']

const funnelChartOption = computed(() => {
  if (funnelData.value.length === 0) return {}
  const maxCount = Math.max(...funnelData.value.map(d => d.count || 0), 1)
  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const item = funnelData.value[params.dataIndex]
        const rate = item?.conversionRate != null ? item.conversionRate.toFixed(1) + '%' : '-'
        const amt = item?.amount != null ? formatMoney(item.amount) : '0.00'
        return `<b>${params.name}</b><br/>线索数: ${params.value}<br/>预期金额: ${amt} 元<br/>转化率: ${rate}`
      }
    },
    series: [{
      name: '销售漏斗', type: 'funnel',
      left: '10%', top: 20, bottom: 20, width: '80%',
      min: 0, max: maxCount || 1,
      minSize: '8%', maxSize: '100%',
      sort: 'none', gap: 8,
      label: {
        show: true, position: 'inside',
        formatter: '{b}  {c}',
        color: '#fff', fontSize: 14, fontWeight: 600,
        textBorderColor: 'rgba(0,0,0,0.2)', textBorderWidth: 2
      },
      labelLine: { show: false },
      itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 4 },
      emphasis: {
        label: { fontSize: 16 },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.15)' }
      },
      data: funnelData.value.map((item, i) => ({
        value: item.count,
        name: item.stageName,
        itemStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: FUNNEL_COLORS[i] },
              { offset: 1, color: FUNNEL_COLORS[i] + 'CC' }
            ]
          }
        }
      }))
    }]
  }
})

const receivableChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '8%', right: '4%', bottom: '12%', top: '8%' },
  xAxis: { type: 'category', data: ['已收款', '未收款'], axisLabel: { color: '#606266' } },
  yAxis: { type: 'value', axisLabel: { color: '#909399' } },
  series: [{
    type: 'bar', barWidth: 40,
    data: [
      { value: financeReceivable.paidAmount, itemStyle: { color: '#67C23A' } },
      { value: financeReceivable.unpaidAmount, itemStyle: { color: '#E6A23C' } }
    ]
  }]
}))

const payableChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '8%', right: '4%', bottom: '12%', top: '8%' },
  xAxis: { type: 'category', data: ['已付款', '未付款'], axisLabel: { color: '#606266' } },
  yAxis: { type: 'value', axisLabel: { color: '#909399' } },
  series: [{
    type: 'bar', barWidth: 40,
    data: [
      { value: financePayable.paidAmount, itemStyle: { color: '#409EFF' } },
      { value: financePayable.unpaidAmount, itemStyle: { color: '#F56C6C' } }
    ]
  }]
}))

// ====== 数据加载 ======

const loadData = async () => {
  loading.value = true
  try {
    const [summary, stocks, receivable, payable, funnel, rank] = await Promise.allSettled([
      getDashboardSummary(),
      getLowStock(),
      getFinanceReceivable(),
      getFinancePayable(),
      getSalesFunnel(),
      getSalesRank(10)
    ])

    if (summary.status === 'fulfilled' && summary.value) {
      Object.keys(data).forEach(k => {
        if (summary.value[k] != null) data[k] = summary.value[k]
      })
    }

    if (stocks.status === 'fulfilled' && Array.isArray(stocks.value)) {
      lowStockCount.value = stocks.value.length
    }

    if (receivable.status === 'fulfilled' && receivable.value) {
      financeReceivable.totalAmount = receivable.value.totalAmount || 0
      financeReceivable.paidAmount = receivable.value.paidAmount || 0
      financeReceivable.unpaidAmount = receivable.value.unpaidAmount || 0
    }

    if (payable.status === 'fulfilled' && payable.value) {
      financePayable.totalAmount = payable.value.totalAmount || 0
      financePayable.paidAmount = payable.value.paidAmount || 0
      financePayable.unpaidAmount = payable.value.unpaidAmount || 0
    }

    if (funnel.status === 'fulfilled' && Array.isArray(funnel.value)) {
      funnelData.value = funnel.value
    }

    if (rank.status === 'fulfilled' && Array.isArray(rank.value)) {
      rankList.value = rank.value
    }
  } catch (e) {
    console.error('Dashboard load failed', e)
  } finally {
    loading.value = false
    dataLoaded.value = true
    updateTime.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  margin-bottom: 16px;
  .header-title { font-size: 16px; font-weight: bold; color: #303133; }
  .header-time { font-size: 13px; color: #909399; margin-left: 16px; }
}

.panel {
  background: #fff;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  height: 100%;
  cursor: pointer;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  .panel-title { font-size: 14px; font-weight: 600; color: #303133; }
  .panel-link { font-size: 13px; color: #409EFF; cursor: pointer; &:hover { text-decoration: underline; } }
}

.panel-body { padding: 16px; }

.big-amount { font-size: 24px; font-weight: bold; color: #303133; margin-bottom: 16px; }

.sub-stats {
  .stat-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
  .stat-value { font-size: 13px; color: #606266; }
}

.target-body { display: flex; align-items: center; gap: 20px; }
.target-chart-area { flex-shrink: 0; width: 140px; }
.target-info {
  flex: 1;
  .target-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; border-bottom: 1px dashed #f0f0f0; }
  .target-label { color: #909399; }
  .target-val { color: #303133; font-weight: 500; }
}

.customer-body { display: flex; flex-direction: column; align-items: center; gap: 20px; padding: 24px 16px; }
.customer-block {
  text-align: center;
  &.clickable { cursor: pointer; &:hover { opacity: 0.8; } }
  .customer-num { font-size: 36px; font-weight: bold; line-height: 1; }
  .customer-label { font-size: 13px; color: #909399; margin-top: 8px; }
}
.customer-divider { width: 60%; height: 1px; background: #ebeef5; }

.purchase-body { display: flex; align-items: center; gap: 24px; }
.purchase-chart-area { text-align: center; flex-shrink: 0; width: 220px; }
.purchase-stats {
  flex: 1;
  .purchase-stat-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px dashed #f0f0f0; font-size: 13px; &:last-child { border-bottom: none; } }
  .purchase-stat-label { color: #909399; }
  .purchase-stat-value { color: #303133; }
}

.inventory-body { display: flex; align-items: center; gap: 24px; }
.inventory-chart-area { flex-shrink: 0; text-align: center; width: 180px; }
.inventory-stats {
  flex: 1;
  .inventory-stat-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px dashed #f0f0f0; font-size: 13px; &:last-child { border-bottom: none; } }
  .inv-label { color: #909399; }
  .inv-value { font-weight: 500; }
}

/* 排行榜样式 */
.rank-panel {
  cursor: default;
}
.rank-tip { font-size: 11px; color: #C0C4CC; }
.rank-body { padding: 8px 16px; max-height: 320px; overflow-y: auto; }
.rank-empty { text-align: center; color: #C0C4CC; padding: 40px 0; font-size: 14px; }
.rank-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
  &:last-child { border-bottom: none; }
  &.rank-top3 { font-weight: 600; }
}
.rank-index {
  width: 32px; text-align: center; font-size: 16px;
  &.rank-1 { font-size: 20px; }
  &.rank-2 { font-size: 18px; }
  &.rank-3 { font-size: 16px; }
}
.rank-name { flex: 1; color: #303133; padding-left: 8px; }
.rank-amount { color: #F56C6C; font-weight: 600; min-width: 100px; text-align: right; }
.rank-count { color: #909399; min-width: 50px; text-align: right; margin-left: 8px; }

/* 漏斗详情 */
.funnel-container { display: flex; align-items: stretch; gap: 16px; }
.funnel-chart-area { flex: 1; min-width: 0; }
.funnel-detail {
  width: 240px; flex-shrink: 0;
  display: flex; flex-direction: column;
}
.funnel-detail-header {
  display: grid; grid-template-columns: 70px 50px 1fr 60px;
  font-size: 12px; color: #909399; font-weight: 600;
  padding: 8px 0; border-bottom: 2px solid #ebeef5;
}
.funnel-detail-row {
  display: grid; grid-template-columns: 70px 50px 1fr 60px;
  font-size: 13px; padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  &:last-child { border-bottom: none; }
}
.fd-stage { color: #303133; font-weight: 500; }
.fd-count { color: #606266; text-align: center; }
.fd-amount { color: #303133; text-align: right; }
.fd-rate { color: #909399; text-align: right; &.rate-good { color: #67C23A; font-weight: 600; } }
.funnel-detail-empty { text-align: center; color: #C0C4CC; padding: 40px 0; }

.color-blue { color: #409EFF; }
.color-green { color: #67C23A; }
.color-orange { color: #E6A23C; }
.color-red { color: #F56C6C; }
</style>
