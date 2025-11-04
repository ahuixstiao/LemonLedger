import { createWebHistory, createRouter } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../view/Home.vue') },
  {
    path: '/admin',
    component: () => import('../view/Admin/Admin.vue'),
    children: [
      {
        path: '/admin/jobList',
        component: () => import('../view/Admin/AdminComponent/JobList.vue')
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
