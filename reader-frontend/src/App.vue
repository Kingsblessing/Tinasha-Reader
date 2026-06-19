<template>
  <AppLayout>
    <router-view v-slot="{ Component, route }">
      <transition :name="route.meta.transition || 'fade'" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </AppLayout>
</template>

<script setup>
import AppLayout from './components/layout/AppLayout.vue'
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Slide-fade transition (for nested pages) */
.slide-fade-enter-active {
  transition: all 0.25s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

/* Slide-left transition (for going back) */
.slide-left-enter-active {
  transition: all 0.25s ease-out;
}
.slide-left-leave-active {
  transition: all 0.2s ease-in;
}
.slide-left-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

/* Reader transition (no animation - instant switch) */
.reader-enter-active,
.reader-leave-active {
  transition: none;
}
</style>
