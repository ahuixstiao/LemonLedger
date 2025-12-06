<template>
  <div class="factoryBill-container">
    <!-- 筛选条件 -->
    <div class="factoryBill-button">
      <el-button type="primary" @click="">添加</el-button>

    </div>
    <!-- 成衣厂报价表格 -->

    <!-- 分页组件 -->
    <div class="factoryBill-page">
      <el-pagination
          background
          :total="data.total"
          v-model:current-page="data.currentPage"
          v-model:page-size="data.pageSize"
          :page-sizes="[5, 10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper, ->"
          @current-change=""
          @size-change=""
      />
    </div>

    <!--  添加或编辑成衣厂账单信息弹窗  -->


  </div>
</template>


<script setup>

import {reactive} from "vue";

const data = reactive({

  currentPage: 1,
  pageSize: 10,
  total: 0,
})

</script>

<style  scoped>

</style>