import { createWebHistory, createRouter } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../view/Home.vue') },
  {
    path: '/admin',
    component: () => import('../view/Admin/Admin.vue'),
    children: [
      {
        path: '/admin/joblist',
        component: () => import('../view/Admin/AdminComponent/JobList.vue')
      },
      {
        path: '/admin/factorylist',
        component: () => import('../view/Admin/AdminComponent/FactoryList.vue')
      },
      {
        path: '/admin/factorybill',
        component: () => import('../view/Admin/AdminComponent/FactoryBill.vue')
      },
      {
        path: '/admin/factoryquotation',
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
