<template>
  <section class="admin-page">
    <AdminPageHeader
      title="数据库备份"
      desc="每天凌晨自动把整个数据库导出一份压缩文件，出问题时能倒回某一天。备份文件在宿主机上，删容器也不会丢。"
      :count="data.total"
      count-label="份备份"
    >
      <template #actions>
        <el-button @click="fetchList" :loading="data.loading">刷新</el-button>
      </template>
    </AdminPageHeader>

    <el-alert
      v-if="data.stale"
      class="backup-alert"
      type="warning"
      show-icon
      :closable="false"
      title="最近一次备份已经超过 36 小时"
      description="计划任务可能停了。在项目目录下运行 deploy/backup-mysql.sh 可以立刻补一份；如果一直不跑，建议检查 Windows 计划任务里的 LemonLedger 数据库备份。"
    />

    <div class="stat-grid">
      <div class="stat-card">
        <span class="stat-label">最近一次备份</span>
        <strong class="stat-value">{{ data.latest ? data.latest.createdAt : '还没有' }}</strong>
        <span class="stat-sub">{{ data.latest ? `距今 ${ageText(data.latest.ageHours)}` : '先跑一次 deploy/backup-mysql.sh' }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">备份文件</span>
        <strong class="stat-value">{{ formatNumber(data.total) }} 份</strong>
        <span class="stat-sub">最多保留最近 14 份，超出自动删旧的</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">占用空间</span>
        <strong class="stat-value">{{ data.totalSizeText || '0 B' }}</strong>
        <span class="stat-sub">压缩后的 .sql.gz</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">备份内容</span>
        <strong class="stat-value">整个 ledger 库</strong>
        <span class="stat-sub">结构 + 数据 + 视图 + 触发器</span>
      </div>
    </div>

    <div class="table-card is-compact">
      <el-table :data="data.files" v-loading="data.loading" empty-text="还没有备份文件，先在项目目录运行 deploy/backup-mysql.sh">
        <el-table-column prop="fileName" label="文件名" min-width="280" show-overflow-tooltip />
        <el-table-column label="大小" min-width="110" align="center">
          <template #default="scope">
            <span class="num">{{ scope.row.sizeText }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="备份时间" min-width="180" align="center">
          <template #default="scope">
            <span class="date-cell">{{ scope.row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="多久以前" min-width="120" align="center">
          <template #default="scope">
            <span :class="['backup-age', Number(scope.row.ageHours) > 36 ? 'is-stale' : '']">
              {{ ageText(scope.row.ageHours) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="backup-help">
      <div class="backup-help__block">
        <h3>自动备份是怎么跑的</h3>
        <p>
          Windows 计划任务「LemonLedger 数据库备份」每天 <strong>02:30</strong> 调用
          <code>deploy/backup-mysql.sh</code>，导出成功后自动删掉超过 14 天的旧文件；
          每天的结果写在 <code>backups/backup.log</code>。
        </p>
      </div>
      <div class="backup-help__block">
        <h3>要恢复某一天</h3>
        <p>在项目目录执行下面这行（把文件名换成表格里的）：</p>
        <div class="backup-cmd">
          <code>bash deploy/backup-mysql.sh --restore backups/{{ data.files[0]?.fileName || 'ledger-20260101-023000.sql.gz' }}</code>
          <el-button size="small" @click="copyRestoreCommand">复制</el-button>
        </div>
        <p class="muted">恢复会覆盖当前库里的同名表，先在测试环境验证一遍更稳。</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageHeader from '../AdminPageHeader.vue'
import { formatNumber } from '../adminShared.js'
import { queryBackupList } from '../../../network/admin/backup.js'

const data = reactive({
  files: [],
  total: 0,
  totalSizeText: '',
  latest: null,
  stale: false,
  loading: false
})

function ageText(hours) {
  const value = Number(hours)
  if (!Number.isFinite(value)) return '未知'
  if (value < 1) return '不到 1 小时'
  if (value < 24) return `${value} 小时前`
  return `${Math.floor(value / 24)} 天前`
}

async function fetchList() {
  data.loading = true
  try {
    const { data: res } = await queryBackupList()
    data.files = res.data?.files || []
    data.total = res.data?.fileCount || 0
    data.totalSizeText = res.data?.totalSizeText || ''
    data.latest = res.data?.latest || null
    data.stale = Boolean(res.data?.stale)
  } catch (error) {
    ElMessage.error('备份列表加载失败')
    console.error(error)
  } finally {
    data.loading = false
  }
}

async function copyRestoreCommand() {
  const name = data.files[0]?.fileName || 'ledger-20260101-023000.sql.gz'
  const command = `bash deploy/backup-mysql.sh --restore backups/${name}`
  try {
    await navigator.clipboard.writeText(command)
    ElMessage.success('恢复命令已复制')
  } catch {
    ElMessage.warning('浏览器不让复制，手动选中这行吧：' + command)
  }
}

onMounted(fetchList)
</script>

<style scoped>
.backup-alert {
  margin-bottom: 14px;
  border-radius: 12px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 14px;
  margin-bottom: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #e6edf9;
  border-radius: 12px;
}

.stat-label {
  font-size: 12.5px;
  color: var(--admin-text-secondary);
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.stat-sub {
  font-size: 12px;
  color: var(--admin-text-muted);
}

.backup-age {
  font-variant-numeric: tabular-nums;
  color: var(--admin-text-secondary);
}

.backup-age.is-stale {
  color: #e0382a;
  font-weight: 600;
}

.backup-help {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 14px;
}

.backup-help__block {
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #e6edf9;
  border-radius: 12px;
}

.backup-help__block h3 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--text-primary);
}

.backup-help__block p {
  margin: 0 0 8px;
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--admin-text-secondary);
}

.backup-help__block code {
  padding: 1px 5px;
  border-radius: 4px;
  background: #f2f6fd;
  color: var(--text-primary);
  font-size: 12px;
}

.backup-cmd {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #f7f9fd;
  border-radius: 8px;
}

.backup-cmd code {
  flex: 1;
  background: transparent;
  word-break: break-all;
}
.table-card.is-compact {
  flex: 0 0 auto;
  min-height: 0;
}
</style>
