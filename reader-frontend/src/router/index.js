import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/library'
  },
  {
    path: '/library',
    name: 'Library',
    component: () => import('../views/LibraryView.vue'),
    meta: { transition: 'fade' }
  },
  {
    path: '/library/:id',
    name: 'ComicDetail',
    component: () => import('../views/ComicDetailView.vue'),
    meta: { transition: 'slide-fade' }
  },
  {
    path: '/reader/:id',
    name: 'Reader',
    component: () => import('../views/ReaderView.vue'),
    meta: { transition: 'reader' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/FavoritesView.vue'),
    meta: { transition: 'fade' }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('../views/HistoryView.vue'),
    meta: { transition: 'fade' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/settings/SettingsView.vue'),
    meta: { transition: 'fade' }
  },
  {
    path: '/settings/sources',
    name: 'Sources',
    component: () => import('../views/settings/SourcesView.vue'),
    meta: { transition: 'slide-fade' }
  },
  {
    path: '/settings/tags',
    name: 'Tags',
    component: () => import('../views/settings/TagsView.vue'),
    meta: { transition: 'slide-fade' }
  },
  {
    path: '/stats',
    name: 'Stats',
    component: () => import('../views/StatsView.vue'),
    meta: { transition: 'fade' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
