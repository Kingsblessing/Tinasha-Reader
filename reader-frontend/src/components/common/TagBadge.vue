<template>
  <el-tag
    :type="type"
    :size="size"
    :closable="closable"
    :style="tagStyle"
    class="tag-badge"
    @close="emit('close')"
    @click="emit('click', tag)"
  >
    {{ tag.name }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  tag: {
    type: Object,
    required: true
  },
  closable: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'small',
    validator: (val) => ['small', 'default'].includes(val)
  }
})

const emit = defineEmits(['close', 'click'])

const type = computed(() => {
  if (props.tag.color) return undefined
  return 'info'
})

const tagStyle = computed(() => {
  if (props.tag.color) {
    return {
      '--el-tag-bg-color': props.tag.color,
      '--el-tag-border-color': props.tag.color,
      '--el-tag-text-color': '#ffffff'
    }
  }
  return {}
})
</script>

<style scoped lang="scss">
.tag-badge {
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.85;
  }
}
</style>
