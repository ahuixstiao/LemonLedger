import { createWebHistory, createRouter } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../view/Home/Home.vue') },
  { path: '/workOrder', component: () => import('../view/Home/JobWorkOrder.vue') },
  {
    path: '/admin',
    component: () => import('../view/Admin/Admin.vue'),
    redirect: '/admin/factoryBillList',
    children: [
      {
        path: 'jobList',
        component: () => import('../view/Admin/AdminComponent/Employee.vue'),
        meta: {
          title: '员工档案',
          desc: '维护员工姓名与工作方式；误删的记录会进入「删除」状态，可随时切回来查看。'
        }
      },
      {
        path: 'factoryList',
        component: () => import('../view/Admin/AdminComponent/FactoryList.vue'),
        meta: {
          title: '成衣厂列表',
          desc: '维护合作成衣厂的基础信息，账单和毛利都按厂名归集。'
        }
      },
      {
        path: 'factoryBillList',
        component: () => import('../view/Admin/AdminComponent/FactoryBill.vue'),
        meta: {
          title: '成衣厂账单',
          desc: '按厂、床号、款式和日期查账；这里的数量与金额会直接进入毛利统计。'
        }
      },
      {
        path: 'grossProfit',
        component: () => import('../view/Admin/AdminComponent/GrossProfit.vue'),
        meta: {
          title: '毛利统计',
          desc: '账单收入减去工资支出，按成衣厂汇总；不选日期时默认看本月 1 号到今天。'
        }
      },
      {
        path: 'factoryPieceRateList',
        component: () => import('../view/Admin/AdminComponent/FactoryPieceRate.vue'),
        meta: {
          title: '成衣厂计件单价',
          desc: '一个款式 + 工作类型对应一个厂价，账单金额就按这里的单价计算。'
        }
      },
      {
        path: 'jobPieceRateList',
        component: () => import('../view/Admin/AdminComponent/JobPieceRate.vue'),
        meta: {
          title: '员工计件单价',
          desc: '按工作方式 + 工作类型维护员工工价，工资核算直接取这份数据。'
        }
      },
      {
        path: 'jobCategoryList',
        component: () => import('../view/Admin/AdminComponent/JobCategory.vue'),
        meta: {
          title: '工作类型',
          desc: '工作类型是账单、单价和工单共用的字典，新增后立即出现在下拉框里。'
        }
      },
      {
        path: 'jobModeList',
        component: () => import('../view/Admin/AdminComponent/JobMode.vue'),
        meta: {
          title: '工作方式',
          desc: '工作方式用于区分压花、刮胶等计件口径，和员工计件单价绑定。'
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
