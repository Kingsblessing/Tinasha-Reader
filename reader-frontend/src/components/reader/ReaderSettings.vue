<template>
  <Teleport to="body">
    <Transition name="panel-slide">
      <div v-if="modelValue" class="settings-overlay" @click.self="emit('close')">
        <div class="settings-panel">
          <div class="panel-header">
            <h3>阅读设置</h3>
            <button class="close-btn" @click="emit('close')">
              <el-icon><Close /></el-icon>
            </button>
          </div>

          <div class="panel-body">
            <!-- Reading Mode -->
            <div class="setting-group">
              <label class="setting-label">阅读模式</label>
              <el-radio-group
                :model-value="readingMode"
                size="small"
                @change="(val) => emit('mode-change', val)"
              >
                <el-radio-button value="single">单页</el-radio-button>
                <el-radio-button value="double">双页</el-radio-button>
                <el-radio-button value="scroll">滚动</el-radio-button>
              </el-radio-group>
            </div>

            <!-- Reading Direction -->
            <div class="setting-group">
              <label class="setting-label">阅读方向</label>
              <el-button size="small" @click="emit('direction-toggle')">
                {{ readingDirection === 'rtl' ? '从右到左 (RTL)' : '从左到右 (LTR)' }}
              </el-button>
            </div>

            <!-- Fit Mode -->
            <div class="setting-group">
              <label class="setting-label">适配模式</label>
              <el-button size="small" @click="emit('fit-mode-change')">
                {{ fitModeLabel }}
              </el-button>
            </div>

            <!-- Auto-play Interval -->
            <div class="setting-group">
              <label class="setting-label">
                自动播放间隔: {{ autoPlayInterval }}秒
              </label>
              <el-slider
                :model-value="autoPlayInterval"
                :min="2"
                :max="30"
                :step="1"
                show-stops
                @change="(val) => emit('interval-change', val)"
              />
            </div>

            <!-- Keyboard Shortcuts -->
            <div class="setting-group">
              <label class="setting-label">快捷键</label>
              <table class="shortcuts-table">
                <thead>
                  <tr>
                    <th>按键</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="shortcut in shortcuts" :key="shortcut.key">
                    <td><kbd>{{ shortcut.key }}</kbd></td>
                    <td>{{ shortcut.action }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  readingMode: {
    type: String,
    default: 'single'
  },
  readingDirection: {
    type: String,
    default: 'ltr'
  },
  fitMode: {
    type: String,
    default: 'width'
  },
  autoPlayInterval: {
    type: Number,
    default: 5
  },
  shortcuts: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'update:modelValue',
  'mode-change',
  'direction-toggle',
  'fit-mode-change',
  'interval-change',
  'close'
])

const fitModeLabels = {
  width: '适应宽度',
  height: '适应高度',
  'original-fit': '适应原图',
  'contain-fit': '自适应缩放'
}

const fitModeLabel = computed(() => fitModeLabels[props.fitMode] || props.fitMode)
</script>

<style scoped lang="scss">
.settings-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: transparent;
}

.settings-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 320px;
  max-width: 90vw;
  background: #1a1a1a;
  border-left: 1px solid #333;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.5);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #333;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #e0e0e0;
  }
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.setting-group {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.setting-label {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: #999;
}

.shortcuts-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  th,
  td {
    padding: 8px 10px;
    text-align: left;
    border-bottom: 1px solid #2a2a2a;
    color: #ccc;
  }

  th {
    color: #888;
    font-weight: 500;
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  kbd {
    display: inline-block;
    padding: 2px 8px;
    font-size: 12px;
    font-family: monospace;
    color: #e0e0e0;
    background: #2a2a2a;
    border: 1px solid #444;
    border-radius: 4px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  }
}

// Slide-in animation
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.3s ease;

  .settings-panel {
    transition: transform 0.3s ease;
  }
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  background: transparent;

  .settings-panel {
    transform: translateX(100%);
  }
}

// Override Element Plus radio group dark theme
:deep(.el-radio-group) {
  .el-radio-button__inner {
    background: #2a2a2a;
    border-color: #444;
    color: #ccc;

    &:hover {
      color: #fff;
    }
  }

  .el-radio-button__original-radio:checked + .el-radio-button__inner {
    background: #409eff;
    border-color: #409eff;
    color: #fff;
  }
}

:deep(.el-slider) {
  .el-slider__runway {
    background-color: #333;
  }

  .el-slider__bar {
    background-color: #409eff;
  }

  .el-slider__button {
    border-color: #409eff;
    background-color: #1a1a1a;
  }

  .el-slider__stop {
    background-color: #444;
  }
}
</style>
