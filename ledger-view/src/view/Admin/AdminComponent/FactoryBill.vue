<template>
  <div class="factorybill-container">
    <div class="factorybill-button">
      <el-button type="primary">添加</el-button>
      <el-select
        v-model="data.factoryId"
        filterable
        placeholder="请选择工厂名称"
      >
        <el-option
          v-for="(item, index) in data.factoryList"
          :label="item.factoryName"
          :value="item.id"
        />
      </el-select>
      <el-select
        v-model="data.categoryId"
        filterable
        placeholder="请选择款式编号"
      >
        <el-option
          v-for="(item, index) in data.categoryList"
          :label="item.category"
          :value="item.id"
        />
      </el-select>
      <el-date-picker
        v-model="data.startDate"
        type="date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
        placeholder="工作记录日期"
      />
      <el-date-picker
        v-model="data.endDate"
        type="date"
        format="YYYY/MM/DD"
        value-format="YYYY-MM-DD"
        placeholder="工作记录日期"
      />
      <el-button icon="Search" @click="queryFactoryBillListHandle" />
    </div>
    <div class="factorybill-data">
      <el-table :data="data.tableData" style="width: 100%">
        <el-table-column prop="id" label="id" />
        <el-table-column prop="factoryName" label="工厂名称" />
        <el-table-column prop="styleNumber" label="样式款号" />
        <el-table-column prop="number" label="床号 " />
        <el-table-column prop="quantity" label="数量" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button @click="handleEdit(scope.$index, scope.row)">
              编辑
            </el-button>
            <el-button
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="factorybill-page">
      <el-pagination
        background
        :total="data.total"
        v-model:current-page="data.currentPage"
        v-model:page-size="data.pageSize"
        :page-sizes="[5, 10, 20, 50, 100]"
        layout="sizes, prev, pager, next, jumper, ->"
        @current-change="queryFactoryBillListHandle()"
        @size-change="queryFactoryBillListHandle()"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import {
  queryFactoryBillList,
  queryFactoryList
} from '../../../nwtwork/index.js'

onMounted(() => {
  queryFactoryListHandle()
  queryFactoryBillListHandle()
})
const data = reactive({
  tableData: [],
  factoryList: [],
  factoryId: '',
  number: '',
  startDate: '',
  endDate: '',
  styleNumber: '',
  categoryId: '',
  currentPage: 1,
  pageSize: 10,
  total: 0
})
// 查询厂名列表
const queryFactoryListHandle = async () => {
  const { data: res } = await queryFactoryList()
  data.factoryList = res.data
  console.log(res.data)
}

const queryFactoryBillListHandle = async () => {
  const { data: res } = await queryFactoryBillList(
    data.factoryId,
    data.number,
    data.startDate,
    data.endDate,
    data.styleNumber,
    data.categoryId,
    data.currentPage,
    data.pageSize
  )
  data.tableData = res.data
  console.log(res.data)
}
</script>

<style scoped>
.factorybill-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.factorybill-button {
  display: flex;
  flex-wrap: wrap;
  flex-shrink: 0;
  padding: 10px;
  gap: 10px;
  border-bottom: 1px solid var(--el-border-color);
}
.factorybill-button div {
  width: 200px;
}
.factorybill-data {
  flex: 1;
  overflow: auto;
}
.factorybill-page {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
}
</style>
