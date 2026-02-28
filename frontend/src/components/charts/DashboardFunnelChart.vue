<template>
  <div ref="chartRef" :style="{ width: '100%', height: height }" v-loading="loading">
    <div v-if="isEmpty" class="chart-empty">
      <el-empty description="暂无数据" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, default: () => ({}) },
  height: { type: String, default: '300px' },
  loading: { type: Boolean, default: false },
  isEmpty: { type: Boolean, default: false }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value || props.isEmpty) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(props.option, true)
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(() => props.option, () => {
  nextTick(initChart)
}, { deep: true })

watch(() => props.isEmpty, (val) => {
  if (!val) nextTick(initChart)
})

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
