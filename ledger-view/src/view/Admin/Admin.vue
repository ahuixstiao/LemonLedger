<template>
  <div class="admin-container">
    <div class="right-container">
      <el-menu
          default-active="/admin/factoryBillList"
          @open="handleOpen"
          @close="handleClose"
          router
      >
        <el-menu-item index="/admin/jobList">
          <el-icon>
            <setting/>
          </el-icon>
          <span>员工列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/factoryList">
          <el-icon>
            <setting/>
          </el-icon>
          <span>成衣厂列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/factoryBillList">
          <el-icon>
            <setting/>
          </el-icon>
          <span>成衣厂账单列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/factoryQuotationList">
          <el-icon>
            <setting/>
          </el-icon>
          <span>成衣厂报价列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/2">
          <el-icon>
            <setting/>
          </el-icon>
          <span>工作报价</span>
        </el-menu-item>
        <el-menu-item index="/admin/3">
          <el-icon>
            <setting/>
          </el-icon>
          <span>工作分类列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/4">
          <el-icon>
            <setting/>
          </el-icon>
          <span>工作方式列表</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="left-container">
      <RouterView/>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const data = reactive({})

const handleOpen = (key, keyPath) => {
  //console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
  //console.log(key, keyPath)
}
</script>

<style scoped>
.admin-container {
  width: 100vw;
  height: calc(100vh - 81px);
  display: flex;
  overflow: hidden;
}

.nav-container {
  height: 80px;
  width: 100%;
  border-bottom: 1px solid var(--el-border-color);
}

.right-container {
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
