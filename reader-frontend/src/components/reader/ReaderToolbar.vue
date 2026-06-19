<template>
  <div class="reader-toolbar" :class="{ visible: show }">
    <!-- Top bar -->
    <div class="toolbar-top">
      <el-button :icon="Back" text class="toolbar-btn" @click="$emit('back')">返回</el-button>
      <span class="toolbar-title">{{ title }}</span>
      <div class="toolbar-actions">
        <span class="page-indicator">{{ currentPage }} / {{ totalPages }}</span>

        <!-- Mode toggle group -->
        <el-button-group class="mode-group">
          <el-tooltip content="单页模式" placement="bottom">
            <el-button
              :icon="Document"
              text
              class="toolbar-btn"
              :class="{ active: readingMode === 'single' }"
              @click="$emit('mode-change', 'single')"
            />
          </el-tooltip>
          <el-tooltip content="双页模式" placement="bottom">
            <el-button
              :icon="Monitor"
              text
              class="toolbar-btn"
              :class="{ active: readingMode === 'double' }"
              @click="$emit('mode-change', 'double')"
            />
          </el-tooltip>
          <el-tooltip content="滚动模式" placement="bottom">
            <el-button
              :icon="Reading"
              text
              class="toolbar-btn"
              :class="{ active: readingMode === 'scroll' }"
              @click="$emit('mode-change', 'scroll')"
            />
          </el-tooltip>
        </el-button-group>

        <!-- Auto-play -->
        <el-tooltip :content="autoPlay ? '停止自动播放' : '自动播放 (P)'" placement="bottom">
          <el-button
            :icon="autoPlay ? VideoPause : VideoPlay"
            text
            class="toolbar-btn"
            :class="{ active: autoPlay }"
            @click="$emit('auto-play-toggle')"
          />
        </el-tooltip>

        <!-- Direction toggle -->
        <el-tooltip :content="directionTooltip" placement="bottom">
          <el-button
            :icon="Rank"
            text
            class="toolbar-btn direction-btn"
            @click="$emit('direction-toggle')"
          >
            {{ directionLabel }}
          </el-button>
        </el-tooltip>

        <!-- Fit mode -->
        <el-tooltip :content="fitModeTooltip" placement="bottom">
          <el-button
            :icon="ScaleToOriginal"
            text
            class="toolbar-btn fit-btn"
            @click="$emit('fit-mode-change')"
          >
            {{ fitModeLabel }}
          </el-button>
        </el-tooltip>

        <el-tooltip content="全屏 (F)" placement="bottom">
          <el-button
            :icon="FullScreen"
            text
            class="toolbar-btn"
            @click="$emit('fullscreen-toggle')"
          />
        </el-tooltip>
        <el-tooltip content="设置" placement="bottom">
          <el-button
            :icon="Setting"
            text
            class="toolbar-btn"
            @click="$emit('settings-toggle')"
          />
        </el-tooltip>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="toolbar-bottom" v-if="totalPages > 0">
      <el-slider
        :model-value="currentPage"
        :min="1"
        :max="totalPages"
        :step="1"
        class="page-slider"
        @change="$emit('page-change', $event)"
        @mousedown="$emit('slider-drag-start')"
        @mouseup="$emit('slider-drag-end')"
      />
      <div class="page-input-group">
        <span>跳转到</span>
        <el-input-number
          :model-value="currentPage"
          :min="1"
          :max="totalPages"
          :step="1"
          size="small"
          class="page-input"
          @change="$emit('page-change', $event)"
        />
        <span>页</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Back,
  FullScreen,
  Setting,
  Monitor,
  Document,
  Reading,
  Rank,
  ScaleToOriginal,
  VideoPlay,
  VideoPause
} from '@element-plus/icons-vue'

const props = defineProps({
  currentPage: { type: Number, default: 1 },
  totalPages: { type: Number, default: 0 },
  title: { type: String, default: '' },
  show: { type: Boolean, default: true },
  readingMode: { type: String, default: 'single' },
  readingDirection: { type: String, default: 'ltr' },
  fitMode: { type: String, default: 'width' },
  autoPlay: { type: Boolean, default: false }
})

defineEmits([
  'page-change',
  'fullscreen-toggle',
  'settings-toggle',
  'back',
  'mode-change',
  'direction-toggle',
  'fit-mode-change',
  'auto-play-toggle',
  'slider-drag-start',
  'slider-drag-end'
])

const directionLabel = computed(() => {
  return props.readingDirection === 'rtl' ? '右→左' : '左→右'
})

const directionTooltip = computed(() => {
  return props.readingDirection === 'rtl' ? '切换为从左到右 (R)' : '切换为从右到左 (R)'
})

const fitModeLabel = computed(() => {
  const labels = { width: '适合宽度', height: '适合高度', window: '适合窗口', original: '原始大小' }
  return labels[props.fitMode] || '适合宽度'
})

const fitModeTooltip = computed(() => {
  return `当前: ${fitModeLabel.value}，点击切换`
})
</script>

<style scoped lang="scss">
.reader-toolbar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 200;
  opacity: 0;
  transform: translateY(-10px);
  transition: opacity 0.3s ease, transform 0.3s ease;
  pointer-events: none;

  &.visible {
    opacity: 1;
    transform: translateY(0);
    pointer-events: auto;
  }
}

.toolbar-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.7), transparent);
  color: #fff;
}

.toolbar-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 16px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  color: #fff !important;

  &:hover {
    color: rgba(255, 255, 255, 0.8) !important;
    background-color: rgba(255, 255, 255, 0.1) !important;
  }

  &.active {
    color: #409eff !important;
    background-color: rgba(64, 158, 255, 0.15) !important;
  }
}

.mode-group {
  display: inline-flex;

  .toolbar-btn {
    border-radius: 0;
    &:first-child { border-radius: 4px 0 0 4px; }
    &:last-child { border-radius: 0 4px 4px 0; }
  }
}

.direction-btn,
.fit-btn {
  font-size: 12px;
  padding: 8px 10px;
}

.page-indicator {
  font-size: 14px;
  margin-right: 8px;
  white-space: nowrap;
}

.toolbar-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 40px 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-slider {
  flex: 1;

  :deep(.el-slider__runway) { background-color: rgba(255, 255, 255, 0.3); }
  :deep(.el-slider__bar) { background-color: #409eff; }
  :deep(.el-slider__button) { border-color: #409eff; }
}

.page-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
  white-space: nowrap;
}

.page-input {
  width: 100px;
}

@media (max-width: 768px) {
  .toolbar-actions {
    gap: 4px;
  }

  .direction-btn,
  .fit-btn {
    display: none;
  }

  .page-indicator {
    font-size: 12px;
    margin-right: 4px;
  }
}
</style>
