# Admin API 清单（RESTful）

> 基础返回结构：`Result<T>`
> 
> 通用分页参数：`currentPage`、`pageSize`、`flag`

## 1. 员工管理 `/admin/emp`

### GET `/admin/emp`
- 说明：按条件查询员工（支持名称筛选和分页）
- Query:
  - `name` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 5)
  - `flag` (optional, default 0)

### POST `/admin/emp`
- 说明：新增员工
- Body: `Employee`

---

## 2. 成衣厂管理 `/admin/factory`

### GET `/admin/factory`
- 说明：按条件查询成衣厂
- Query:
  - `factoryName` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 5)
  - `flag` (optional, default 0)

### POST `/admin/factory`
- 说明：新增成衣厂
- Body: `Factory`

---

## 3. 成衣厂账单 `/admin/factoryBill`

### GET `/admin/factoryBill`
- 说明：按条件查询账单分页（款式与工作类型信息来自报价表关联）
- Query:
  - `factoryId` (optional)
  - `number` (optional)
  - `styleNumber` (optional, 对报价表款式编号模糊查询)
  - `flag` (optional, default 0)
  - `startDate` (optional)
  - `endDate` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 5)

### POST `/admin/factoryBill`
- 说明：新增账单（账单款式与工作类型由 `quotationId + factoryId` 关联报价表获取）
- Body: `FactoryBill`
  - `factoryId` (required)
  - `quotationId` (required)
  - `number` (required)
  - `quantity` (required)
  - `createdDate` (required)

### PUT `/admin/factoryBill/{id}`
- 说明：更新账单（账单款式与工作类型由 `quotationId + factoryId` 关联报价表获取）
- Path:
  - `id`
- Body: `FactoryBill`
  - `factoryId` (optional, 不传则沿用原记录)
  - `quotationId` (optional, 不传则沿用原记录)
  - `number` (optional)
  - `quantity` (optional)
  - `createdDate` (optional)

### DELETE `/admin/factoryBill/{id}`
- 说明：删除账单
- Path:
  - `id`

### GET `/admin/factoryBill/bill/{factoryId}`
- 说明：统计账单
- Query:
  - `startDate` (optional)
  - `endDate` (optional)
  - `flag` (optional, default 0)

### GET `/admin/factoryBill/excel/{factoryId}`
- 说明：导出账单 Excel
- Query:
  - `startDate` (optional)
  - `endDate` (optional)

---

## 4. 成衣厂报价 `/admin/factoryQuotation`

### GET `/admin/factoryQuotation`
- 说明：按条件查询成衣厂报价
- Query:
  - `factoryId` (optional)
  - `styleNumber` (optional)
  - `categoryId` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)
  - `flag` (optional, default 0)

### POST `/admin/factoryQuotation`
- 说明：新增成衣厂报价
- Body: `FactoryQuotation`

### PUT `/admin/factoryQuotation/{id}`
- 说明：更新成衣厂报价
- Path:
  - `id`
- Body: `FactoryQuotation`

### DELETE `/admin/factoryQuotation/{id}`
- 说明：删除成衣厂报价
- Path:
  - `id`

### GET `/admin/factoryQuotation/styleNumberList`
- 说明：按工厂查询款号列表
- Query:
  - `factoryId` (optional)
  - `flag` (optional, default 0)

---

## 5. 工作类型 `/admin/jobCategory`

### GET `/admin/jobCategory`
- 说明：按条件查询工作类型
- Query:
  - `category` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)
  - `flag` (optional, default 0)

### POST `/admin/jobCategory`
- 说明：新增工作类型
- Body: `JobCategory`

### DELETE `/admin/jobCategory/{id}`
- 说明：删除工作类型
- Path:
  - `id`

---

## 6. 工作方式 `/admin/jobMode`

### GET `/admin/jobMode`
- 说明：按条件查询工作方式
- Query:
  - `mode` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)
  - `flag` (optional, default 0)

### POST `/admin/jobMode`
- 说明：新增工作方式
- Body: `JobMode`

### DELETE `/admin/jobMode/{id}`
- 说明：删除工作方式
- Path:
  - `id`

---

## 7. 工作报价 `/admin/jobQuotation`

### GET `/admin/jobQuotation`
- 说明：按条件查询工作报价
- Query:
  - `modeId` (optional)
  - `categoryId` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)
  - `flag` (optional, default 0)

### POST `/admin/jobQuotation`
- 说明：新增工作报价
- Body: `JobQuotation`

### PUT `/admin/jobQuotation/{id}`
- 说明：更新工作报价
- Path:
  - `id`
- Body: `JobQuotation`

### DELETE `/admin/jobQuotation/{id}`
- 说明：删除工作报价
- Path:
  - `id`

---

## 8. 成衣厂工作类型 `/admin/factoryJobCategory`

### GET `/admin/factoryJobCategory/list`
- 说明：查询成衣厂工作类型列表（下拉用）
