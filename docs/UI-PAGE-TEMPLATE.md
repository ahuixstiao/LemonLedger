# 新页面模板（后台 / 前台）

新建页面先照着这两个文件抄结构，不要另起一套样式。

| 端 | 模板文件 | 入口骨架 |
| --- | --- | --- |
| 后台 | `ledger-view/src/view/Admin/AdminComponent/FactoryStyle.vue` | `src/view/Admin/Admin.vue`（左侧菜单 + 顶栏） |
| 前台 | `ledger-view/src/view/Home/Home.vue` | `src/view/Home/HomeComponent/FrontShell.vue`（顶部导航） |

共用件：`AdminPageHeader.vue`、`AdminComponent/adminShared.js`（`formatMoney` / `formatNumber` / `formatDate` / `confirmDelete`）、`AdminComponent/factoryCrudShared.js`（弹窗增删改）、`src/base/dialogShortcuts.js`（Ctrl+Enter / Esc / Alt+A）、`src/base/submitKey.js`（提交幂等）。

---

## 一、后台页面骨架（列表页）

```vue
<template>
  <div class="admin-page">
    <AdminPageHeader title="页面标题" desc="一句话说明这个页面干什么。">
      <template #stats>
        <span class="admin-count-chip"><strong>{{ formatNumber(data.total) }}</strong> 条记录</span>
      </template>
      <template #actions>
        <el-button type="primary" @click="openCreateDialog(...)" title="Alt + A">
          <el-icon><Plus /></el-icon> 添加
        </el-button>
      </template>
    </AdminPageHeader>

    <div class="filter-card">
      <div class="filter-bar">
        <el-select v-model="data.factoryId" class="filter-wide" placeholder="全部成衣厂" clearable>…</el-select>
        <el-input v-model="data.keyword" placeholder="搜索关键字" clearable @keyup.enter="onSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div class="filter-actions">
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 卡片只包表格；分页在卡片外面，卡片与分页之间由 .admin-page 的 gap 撑开 14px -->
    <div class="table-card">
      <el-table :data="data.tableData" empty-text="还没有记录" class="front-table" height="100%">
        <el-table-column label="名称" min-width="200" align="center">…entity-cell 头像 + 名称…</el-table-column>
        <el-table-column label="金额" width="120" align="center"><span class="money">…</span></el-table-column>
        <el-table-column label="状态" width="110" align="center"><span class="status-pill is-active">正常</span></el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">…编辑 / 删除（is-link 按钮）…</el-table-column>
      </el-table>
    </div>

    <div class="pager-row">
      <el-pagination v-model:current-page="data.currentPage" v-model:page-size="data.pageSize"
        :total="data.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        background @current-change="fetchList" @size-change="onSizeChange" />
    </div>

    <el-dialog v-model="data.editDialogVisible" :title="…" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">…</el-form>
      <template #footer>
        <span class="dialog-hotkey-hint">Ctrl + Enter 提交 · Esc 关闭</span>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="validateDialogForm(formRef)">新增</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

页面级样式基本不用写：`.admin-page / .admin-page-head / .filter-card / .filter-bar / .table-card / .pager-row / .entity-cell / .status-pill / .money / .num / .muted / .dialog-hotkey-hint` 都在 `src/style.css`。确实要加，只写这个页面独有的（如 `.sync-range`），并放在 `<style scoped>` 里。

## 二、前台页面骨架

```vue
<template>
  <FrontShell>
    <div class="admin-page">
      <div class="admin-page-head">
        <div>
          <h2 class="admin-page-head__title">页面标题</h2>
          <p class="admin-page-head__desc">一句话说明。</p>
        </div>
        <div class="admin-page-head__aside">
          <span class="admin-count-chip"><strong>{{ data.total }}</strong> 条记录</span>
          <el-button type="primary" @click="…">主操作</el-button>
        </div>
      </div>

      <div class="filter-card">
        <CommonFilterBar v-model="filters" :fields="filterFields" :initial-value="filterInitial"
          @query="fetchList" @reset="onReset" />
      </div>

      <div class="table-card">
        <HomeTable :table-data="…" :total="…" :current-page="…" :page-size="…"
          :mobile="data.isNarrow" :empty-text="tableEmptyText" @… />
      </div>
    </div>
  </FrontShell>
</template>
```

前台特有约定：

- 外层必须包 `FrontShell`（顶栏 + 内容区），不要自己在页面里写顶栏；
- 断点统一 960px，`data.isNarrow = window.matchMedia('(max-width: 960px)').matches`，窄屏交给组件的 `mobile` 卡片列表；
- 筛选用 `CommonFilterBar`（字段宽度固定 180 / `.filter-wide` 220 / 日期 `.filter-dates` 150），改动走 300ms 防抖自动查询，也可以保留「查询」按钮；
- 默认日期区间用 `src/base/dateRange.js` 的 `recentDateRange(30)`，空列表要有区分「没数据 / 条件太窄」的空态文案；
- 图片地址统一走 `${VITE_BASE_URL}/admin/factoryStyle/imageView/…` 这类后端代理路径，不要写死磁盘路径。

## 三、新建页面要一起改的地方

1. `src/router/router.js`：加 child（`meta: { title, desc }`，后台还要 `icon` 在菜单里）；
2. 后台在 `src/view/Admin/Admin.vue` 菜单分组里加一项；前台在 `FrontShell.vue` 导航里加一项；
3. `src/network/…` 加接口，`network/index.js` 或 `network/admin/index.js` 补充导出；
4. 写操作接口带幂等键（`newSubmitKey()` → 请求 `idempotencyKey`），提交成功换新 key；
5. 金额用 `formatMoney`，数量 `formatNumber`，日期 `formatDate`；删除前 `await confirmDelete(...)`。

## 四、交付前自检

- [ ] `npm run build` 通过；
- [ ] 1440×900 和 390×844 各截一张图：页头、筛选、卡片、分页位置都对（分页在卡片外、底部留间距）；
- [ ] 表格列宽用 `min-width`，不写百分比；`align="center"`；
- [ ] 弹窗宽度 400 / 420 / 460 / 520px，`label-position="top"`，footer 带快捷键提示；
- [ ] 空列表、加载中、加载失败三种状态都有文案；
- [ ] 后台接口 200、页面 200，跑一次真实数据（不是只看 mock）。
