<template>
  <div class="front-shell">
    <header class="front-topbar">
      <router-link class="front-brand" to="/">
        <span class="front-brand-mark">L</span>
        <span class="front-brand-text">
          <strong>LemonLedger</strong>
          <small>生产中心</small>
        </span>
      </router-link>

      <nav class="front-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="front-nav-item"
          :class="{ 'is-current': isCurrent(item.path) }"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <span class="front-topbar-meta">{{ today }}</span>
    </header>

    <main class="front-content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { label: '工单总览', path: '/' },
  { label: '认领工单', path: '/workOrder' }
]

const currentPath = computed(() => route.path)

const isCurrent = path => (path === '/' ? currentPath.value === '/' : currentPath.value.startsWith(path))

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})
</script>
