<template>
  <div class="admin-container">
    <div class="right-container">
      <el-menu
        :default-active="activeMenu"
        router
      >
        <el-menu-item index="/admin/jobList">
          <el-icon>
            <setting />
          </el-icon>
          <span>员工列表</span>
        </el-menu-item>

        <el-menu-item index="/admin/factoryList">
          <el-icon>
            <setting />
          </el-icon>
          <span>成衣厂列表</span>
        </el-menu-item>

        <el-menu-item index="/admin/factoryBillList">
          <el-icon>
            <setting />
          </el-icon>
          <span>成衣厂账单列表</span>
        </el-menu-item>

        <el-menu-item index="/admin/factoryQuotationList">
          <el-icon>
            <setting />
          </el-icon>
          <span>成衣厂报价列表</span>
        </el-menu-item>

        <el-menu-item index="/admin/jobQuotationList">
          <el-icon>
            <setting />
          </el-icon>
          <span>工作报价</span>
        </el-menu-item>

        <el-menu-item index="/admin/jobCategoryList">
          <el-icon>
            <setting />
          </el-icon>
          <span>工作类型列表</span>
        </el-menu-item>

        <el-menu-item index="/admin/jobModeList">
          <el-icon>
            <setting />
          </el-icon>
          <span>工作方式列表</span>
        </el-menu-item>

      </el-menu>
    </div>
    <div class="left-container">
      <RouterView />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Setting } from '@element-plus/icons-vue'

const route = useRoute()

/**
 * 根据当前路由动态高亮后台菜单，避免默认高亮与实际页面不一致。
 */
const activeMenu = computed(() => {
  if (route.path === '/admin') {
    return '/admin/factoryBillList'
  }
  return route.path
})

</script>

<style scoped>
.admin-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.nav-container {
  height: 80px;
  width: 100%;
  border-bottom: 1px solid var(--el-border-color);
}

.el-menu {
  height: 100%;
  overflow-y: auto;
}

.left-container {
  width: 100%;
  overflow: auto;
}

.el-menu {
  height: 100%;
}
</style>
