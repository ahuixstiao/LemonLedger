<template>
  <header class="admin-page-head">
    <div class="admin-page-head__text">
      <h2 class="admin-page-head__title">{{ title }}</h2>
      <p v-if="desc" class="admin-page-head__desc">{{ desc }}</p>
    </div>
    <div class="admin-page-head__aside">
      <slot name="stats" />
      <span v-if="count !== null && count !== undefined" class="admin-count-chip">
        <strong>{{ count }}</strong>
        <span>{{ countLabel }}</span>
      </span>
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' },
  count: { type: [Number, String], default: null },
  countLabel: { type: String, default: '共' }
})
</script>
