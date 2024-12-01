import { createRouter, createWebHistory } from 'vue-router'
import BookVue from '@/components/Book.vue'
import CardVue from '@/components/Card.vue'
import BorrowVue from '@/components/Borrow.vue'
import LookupBookVue from '@/components/LookupBook.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/book'
    },
    {
      path: '/book',
      component: BookVue
    },
    {
      path: '/card',
      component: CardVue
    },
    {
      path: '/borrow',
      component: BorrowVue
    },
    {
      path: '/lookup-book',  // add a new route for LookupBook.vue
      component: LookupBookVue
    }
  ]
})

export default router
