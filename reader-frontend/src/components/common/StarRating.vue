<template>
  <div class="star-rating" :class="{ readonly, small: size === 'small' }">
    <span
      v-for="star in 5"
      :key="star"
      class="star"
      :class="{ filled: star <= Math.floor(modelValue), half: star === Math.ceil(modelValue) && modelValue % 1 !== 0 }"
      @click="onStarClick(star)"
    >
      <el-icon><StarFilled /></el-icon>
    </span>
  </div>
</template>

<script setup>
import { StarFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  readonly: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['update:modelValue'])

function onStarClick(star) {
  if (props.readonly) return
  const newValue = star === props.modelValue ? 0 : star
  emit('update:modelValue', newValue)
}
</script>

<style scoped lang="scss">
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.star {
  color: var(--border-color);
  cursor: pointer;
  transition: color 0.15s ease, transform 0.15s ease;
  font-size: 18px;

  &.filled {
    color: #f7ba2a;
  }

  &.half {
    color: #f7ba2a;
    opacity: 0.6;
  }

  &:hover {
    transform: scale(1.1);
  }
}

.readonly .star {
  cursor: default;

  &:hover {
    transform: none;
  }
}

.small .star {
  font-size: 14px;
}
</style>
