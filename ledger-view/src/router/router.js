import { createWebHistory, createRouter } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../view/Home.vue') },
  { path: '/workOrder', component: () => import('../view/JobWorkOrder.vue') },
  {
    path: '/admin',
    component: () => import('../view/Admin/Admin.vue'),
    children: [
      {
        path: '/admin/jobList',
        component: () => import('../view/Admin/AdminComponent/Employee.vue')
      },
      {
        path: '/admin/factoryList',
        component: () => import('../view/Admin/AdminComponent/FactoryList.vue')
      },
      {
        path: '/admin/factoryBillList',
        component: () => import('../view/Admin/AdminComponent/FactoryBill.vue')
      },
      {
        path: '/admin/factoryQuotationList',
        component: () =>
          import('../view/Admin/AdminComponent/FactoryQuotation.vue')
      },
      {
        path: '/admin/jobQuotationList',
        component: () =>
          import('../view/Admin/AdminComponent/JobQuotation.vue')
      },
      {
        path: '/admin/jobCategoryList',
        component: () =>
          import('../view/Admin/AdminComponent/JobCategory.vue')
      },
      {
        path: '/admin/jobModeList',
        component: () =>
          import('../view/Admin/AdminComponent/JobMode.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
