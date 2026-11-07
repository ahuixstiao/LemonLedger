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
- 说明：按条件查询账单分页（款式与工作类型信息来自计件单价表关联）
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
- 说明：新增账单（账单款式与工作类型由 `pieceRateId + factoryId` 关联计件单价表获取；`number + factoryId + pieceRateId` 在配置的天数窗口内重复时会拒绝）
- 其它 Query:
  - `confirmDuplicate` (optional, default false, 返工/补做时传 true 跳过重复校验)
- Body: `FactoryBill`
  - `factoryId` (required)
  - `pieceRateId` (required)
  - `number` (required)
  - `quantity` (required)
  - `createdDate` (required)

### PUT `/admin/factoryBill/{id}`
- 说明：更新账单（账单款式与工作类型由 `pieceRateId + factoryId` 关联计件单价表获取；重复校验同上）
- 其它 Query:
  - `confirmDuplicate` (optional, default false)
- Path:
  - `id`
- Body: `FactoryBill`
  - `factoryId` (optional, 不传则沿用原记录)
  - `pieceRateId` (optional, 不传则沿用原记录)
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

### GET `/admin/factoryBill/gross`
- 说明：按成衣厂汇总某段时间的毛利（账单收入 − 工资支出）
- Query:
  - `startDate` (optional, 不传默认本月 1 号)
  - `endDate` (optional, 不传默认今天)

### GET `/admin/factoryBill/excel/{factoryId}`
- 说明：导出账单 Excel
- Query:
  - `startDate` (optional)
  - `endDate` (optional)

---

## 4. 成衣厂计件单价 `/admin/factoryPieceRate`

### GET `/admin/factoryPieceRate`
- 说明：按条件查询成衣厂计件单价
- Query:
  - `factoryId` (optional)
  - `styleNumber` (optional)
  - `categoryId` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)
  - `flag` (optional, default 0)

### POST `/admin/factoryPieceRate`
- 说明：新增成衣厂计件单价
- Body: `FactoryPieceRate`

### PUT `/admin/factoryPieceRate/{id}`
- 说明：更新成衣厂计件单价
- Path:
  - `id`
- Body: `FactoryPieceRate`

### DELETE `/admin/factoryPieceRate/{id}`
- 说明：删除成衣厂计件单价
- Path:
  - `id`

### GET `/admin/factoryPieceRate/styleNumberList`
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

### GET `/admin/jobCategory/list`
- 说明：查询全部未删除的工作类型（下拉框用；账单、成衣厂计件单价、员工计件单价共用这一份字典）

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

## 7. 员工计件单价 `/admin/jobPieceRate`

### GET `/admin/jobPieceRate`
- 说明：按条件查询员工计件单价
- Query:
  - `modeId` (optional)
  - `categoryId` (optional)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)
  - `flag` (optional, default 0)

### POST `/admin/jobPieceRate`
- 说明：新增员工计件单价
- Body: `JobPieceRate`

### PUT `/admin/jobPieceRate/{id}`
- 说明：更新员工计件单价
- Path:
  - `id`
- Body: `JobPieceRate`

### DELETE `/admin/jobPieceRate/{id}`
- 说明：删除员工计件单价
- Path:
  - `id`

---

## 8. 床 /admin/batch

> 床不需要手工维护：工单、账单、工作记录录入时会按 (成衣厂, 床号, 款式) 自动建档。
> 原来的「成衣厂工作类型 /admin/factoryJobCategory」已经合并进 `/admin/jobCategory`，不再单独提供接口。

### GET `/admin/batch`
- 说明：分页查询床（可选按厂、床号、款式过滤）
- Query:
  - `factoryId` (optional)
  - `number` (optional, 模糊)
  - `styleNumber` (optional, 模糊)
  - `currentPage` (optional, default 1)
  - `pageSize` (optional, default 10)

### DELETE `/admin/batch/{id}`
- 说明：删除误建的床（逻辑删除；被账单、工单、工作记录引用时会拒绝）
- Path:
  - `id`
