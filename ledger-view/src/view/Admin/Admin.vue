<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <RouterLink to="/admin/factoryBillList" class="admin-brand">
        <span class="admin-brand__mark">L</span>
        <span class="admin-brand__text">
          <strong>LemonLedger</strong>
          <small>后台管理</small>
        </span>
      </RouterLink>

      <nav class="admin-nav" aria-label="后台菜单">
        <div v-for="group in menuGroups" :key="group.title" class="admin-nav__group">
          <p class="admin-nav__label">{{ group.title }}</p>
          <RouterLink
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="admin-nav__item"
            :class="{ 'is-active': activeMenu === item.path }"
          >
            <el-icon class="admin-nav__icon">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.label }}</span>
          </RouterLink>
        </div>
      </nav>

      <RouterLink to="/" class="admin-back">
        <el-icon><Back /></el-icon>
        <span>返回生产中心</span>
      </RouterLink>
    </aside>

    <div class="admin-main">
      <header class="admin-topbar">
        <div class="admin-breadcrumb">
          <span class="admin-breadcrumb__root">后台管理</span>
          <el-icon class="admin-breadcrumb__sep"><ArrowRight /></el-icon>
          <span class="admin-breadcrumb__current">{{ currentTitle }}</span>
        </div>
        <div class="admin-topbar__meta">
          <span class="admin-date">{{ todayText }}</span>
        </div>
      </header>

      <main class="admin-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  ArrowRight,
  Back,
  CollectionTag,
  Money,
  OfficeBuilding,
  Operation,
  PriceTag,
  Tickets,
  TrendCharts,
  User
} from '@element-plus/icons-vue'

const route = useRoute()

/**
 * 菜单按业务分组：先看账单和毛利，再维护基础档案，最后是单价。
 * 这样从「今天要核账」到「改一个单价」的动线更短。
 */
const menuGroups = [
  {
    title: '账单与统计',
    items: [
      { label: '成衣厂账单', path: '/admin/factoryBillList', icon: Tickets },
      { label: '毛利统计', path: '/admin/grossProfit', icon: TrendCharts }
    ]
  },
  {
    title: '基础档案',
    items: [
      { label: '员工档案', path: '/admin/jobList', icon: User },
      { label: '成衣厂列表', path: '/admin/factoryList', icon: OfficeBuilding },
      { label: '工作类型', path: '/admin/jobCategoryList', icon: CollectionTag },
      { label: '工作方式', path: '/admin/jobModeList', icon: Operation }
    ]
  },
  {
    title: '计件单价',
    items: [
      { label: '成衣厂计件单价', path: '/admin/factoryPieceRateList', icon: PriceTag },
      { label: '员工计件单价', path: '/admin/jobPieceRateList', icon: Money }
    ]
  }
]

/** 根据当前路由高亮后台菜单，避免默认高亮与实际页面不一致。 */
const activeMenu = computed(() => {
  if (route.path === '/admin') return '/admin/factoryBillList'
  return route.path
})

const currentTitle = computed(() => route.meta?.title || '后台管理')

const todayText = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(new Date())
)
</script>

<style scoped>
.admin-shell {
  height: 100%;
  display: flex;
  background: #f5f7fb;
  color: var(--text-primary);
}

/* ---------- 侧边栏 ---------- */
.admin-sidebar {
  width: 236px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px 14px;
  background: #ffffff;
  border-right: 1px solid #e9eef7;
  overflow-y: auto;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 2px 6px 6px;
  text-decoration: none;
  color: inherit;
}

.admin-brand__mark {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--brand-1), var(--brand-2));
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  box-shadow: 0 6px 14px rgba(63, 124, 255, 0.28);
}

.admin-brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.admin-brand__text strong {
  font-size: 15px;
  letter-spacing: -0.01em;
}

.admin-brand__text small {
  font-size: 11px;
  color: #8a97ad;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.admin-nav__group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.admin-nav__label {
  margin: 0 0 4px;
  padding: 0 12px;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: #9aa7bd;
}

.admin-nav__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 9px;
  font-size: 13.5px;
  color: #4b5b7a;
  text-decoration: none;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.admin-nav__item:hover {
  background: #f5f8ff;
  color: #2f67df;
}

.admin-nav__item.is-active {
  background: #eef4ff;
  color: #2f67df;
  font-weight: 600;
}

.admin-nav__icon {
  font-size: 16px;
}

.admin-back {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 12px 2px;
  border-top: 1px solid #eef2f8;
  font-size: 13px;
  color: #8a97ad;
  text-decoration: none;
}

.admin-back:hover {
  color: var(--brand-1);
}

/* ---------- 主区域 ---------- */
.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-topbar {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #e9eef7;
}

.admin-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #8a97ad;
}

.admin-breadcrumb__sep {
  font-size: 12px;
  color: #c2cbdb;
}

.admin-breadcrumb__current {
  color: var(--text-primary);
  font-weight: 600;
}

.admin-date {
  font-size: 12.5px;
  color: #8a97ad;
}

.admin-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px 28px;
}

/* ---------- 窄屏：侧边栏变顶部横向菜单 ---------- */
@media (max-width: 960px) {
  .admin-shell {
    flex-direction: column;
  }

  .admin-sidebar {
    width: auto;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    padding: 10px 14px;
    border-right: none;
    border-bottom: 1px solid #e9eef7;
    overflow: visible;
  }

  .admin-brand {
    padding: 0 2px;
    margin-right: 0;
  }

  .admin-brand__text small,
  .admin-back,
  .admin-nav__label {
    display: none;
  }

  .admin-nav {
    flex-direction: row;
    flex-wrap: wrap;
    flex: none;
    gap: 6px;
    overflow: visible;
  }

  .admin-nav__group {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 6px;
  }

  .admin-nav__item {
    padding: 8px 10px;
    white-space: nowrap;
  }

  .admin-topbar {
    display: none;
  }

  .admin-content {
    padding: 14px;
  }
}
</style>
