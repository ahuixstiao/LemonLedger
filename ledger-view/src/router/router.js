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
        component: () => import('../view/Admin/AdminComponent/Employee.vue')
      },
      {
        path: 'factoryList',
        component: () => import('../view/Admin/AdminComponent/FactoryList.vue')
      },
      {
        path: 'factoryBillList',
        component: () => import('../view/Admin/AdminComponent/FactoryBill.vue')
      },
      {
        path: 'factoryQuotationList',
        component: () =>
          import('../view/Admin/AdminComponent/FactoryQuotation.vue')
      },
      {
        path: 'jobQuotationList',
        component: () =>
          import('../view/Admin/AdminComponent/JobQuotation.vue')
      },
      {
        path: 'jobCategoryList',
        component: () =>
          import('../view/Admin/AdminComponent/JobCategory.vue')
      },
      {
        path: 'jobModeList',
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
