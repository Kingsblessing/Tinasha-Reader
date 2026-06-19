<template>
  <div class="comic-detail-view" v-if="comic">
    <div class="detail-header">
      <el-button :icon="Back" text @click="$router.push('/library')">返回书架</el-button>
    </div>

    <div class="detail-content">
      <!-- Cover -->
      <div class="detail-cover">
        <img v-if="coverUrl" :src="coverUrl" :alt="comic.title" />
        <div v-else class="cover-placeholder">
          <el-icon :size="64"><Picture /></el-icon>
        </div>
      </div>

      <!-- Info -->
      <div class="detail-info">
        <h1 class="comic-title">{{ comic.title }}</h1>

        <div class="info-row" v-if="comic.author">
          <span class="info-label">作者：</span>
          <span>{{ comic.author }}</span>
        </div>

        <div class="info-row" v-if="comic.publisher">
          <span class="info-label">出版社：</span>
          <span>{{ comic.publisher }}</span>
        </div>

        <div class="info-row" v-if="comic.pageCount">
          <span class="info-label">页数：</span>
          <span>{{ comic.pageCount }} 页</span>
        </div>

        <div class="info-row">
          <span class="info-label">评分：</span>
          <StarRating v-model="comicRating" @update:model-value="onRatingChange" />
        </div>

        <!-- Tags -->
        <div class="info-row tags-row">
          <span class="info-label">标签：</span>
          <div class="tags-list">
            <el-tag
              v-for="tag in comicTags"
              :key="tag.id"
              closable
              @close="removeTag(tag)"
            >
              {{ tag.name }}
            </el-tag>
            <el-button size="small" @click="showAddTag = true">+ 添加标签</el-button>
          </div>
        </div>

        <!-- Description -->
        <div class="comic-description" v-if="comic.description">
          <h3>简介</h3>
          <p>{{ comic.description }}</p>
        </div>

        <!-- Action buttons -->
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="startReading">
            <el-icon><CaretRight /></el-icon>
            开始阅读
          </el-button>
          <el-button
            v-if="comic.lastReadPage > 0"
            size="large"
            @click="continueReading"
          >
            <el-icon><VideoPlay /></el-icon>
            继续阅读 (第{{ comic.lastReadPage }}页)
          </el-button>
          <el-button
            :type="isFavorite ? 'warning' : 'default'"
            size="large"
            @click="toggleFavorite"
          >
            <el-icon><StarFilled v-if="isFavorite" /><Star v-else /></el-icon>
            {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Reviews section -->
    <div class="reviews-section">
      <h3>评论</h3>
      <div class="review-form">
        <el-input
          v-model="newReview"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
        />
        <el-button type="primary" @click="submitReview" style="margin-top: 8px;">发表评论</el-button>
      </div>
      <div class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-content">{{ review.content }}</div>
          <div class="review-meta">
            <span>{{ formatDate(review.createdAt) }}</span>
            <el-button text size="small" @click="deleteReview(review.id)">删除</el-button>
          </div>
        </div>
        <EmptyState v-if="reviews.length === 0" message="暂无评论" icon="ChatDotSquare" />
      </div>
    </div>

    <!-- Add tag dialog -->
    <el-dialog v-model="showAddTag" title="添加标签" width="400px">
      <el-select v-model="selectedNewTag" filterable placeholder="选择标签" style="width: 100%;">
        <el-option
          v-for="tag in allTags"
          :key="tag.id"
          :label="tag.name"
          :value="tag.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="showAddTag = false">取消</el-button>
        <el-button type="primary" @click="addTag">确定</el-button>
      </template>
    </el-dialog>
  </div>

  <div v-else class="loading-container">
    <el-icon class="loading-spinner" :size="40"><Loading /></el-icon>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Back, Picture, CaretRight, VideoPlay, Star, StarFilled, Loading, ChatDotSquare } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StarRating from '../components/common/StarRating.vue'
import EmptyState from '../components/common/EmptyState.vue'
import * as comicApi from '../api/comicApi'
import * as tagApi from '../api/tagApi'
import * as favoriteApi from '../api/favoriteApi'
import * as reviewApi from '../api/reviewApi'
import { formatDate } from '../utils/format'

const route = useRoute()
const router = useRouter()

const comic = ref(null)
const coverUrl = ref('')
const comicRating = ref(0)
const comicTags = ref([])
const allTags = ref([])
const isFavorite = ref(false)
const favoriteId = ref(null)
const reviews = ref([])
const newReview = ref('')
const showAddTag = ref(false)
const selectedNewTag = ref(null)

async function loadComic() {
  try {
    const data = await comicApi.getById(route.params.id)
    comic.value = data
    comicRating.value = data.rating || 0
  } catch (e) {
    ElMessage.error('加载漫画详情失败')
  }
}

async function loadCover() {
  try {
    coverUrl.value = await comicApi.getCover(route.params.id)
  } catch (e) {
    coverUrl.value = ''
  }
}

async function loadTags() {
  try {
    comicTags.value = await tagApi.getComicTags(route.params.id) || []
  } catch (e) {
    comicTags.value = []
  }
}

async function loadAllTags() {
  try {
    allTags.value = await tagApi.getAll() || []
  } catch (e) {
    allTags.value = []
  }
}

async function loadReviews() {
  try {
    reviews.value = await reviewApi.getByComicId(route.params.id) || []
  } catch (e) {
    reviews.value = []
  }
}

async function checkFavorite() {
  try {
    const result = await favoriteApi.checkFavorited(route.params.id)
    isFavorite.value = result?.favorited || false
    favoriteId.value = result?.favoriteId || null
  } catch (e) {
    isFavorite.value = false
    favoriteId.value = null
  }
}

async function onRatingChange(rating) {
  try {
    await comicApi.updateRating(route.params.id, rating)
    ElMessage.success('评分已更新')
  } catch (e) {
    ElMessage.error('评分更新失败')
  }
}

async function toggleFavorite() {
  try {
    if (isFavorite.value) {
      if (favoriteId.value) {
        await favoriteApi.deleteById(favoriteId.value)
      }
      isFavorite.value = false
      favoriteId.value = null
      ElMessage.success('已取消收藏')
    } else {
      const result = await favoriteApi.create({ comicId: parseInt(route.params.id) })
      isFavorite.value = true
      favoriteId.value = result?.id || null
      ElMessage.success('已收藏')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function addTag() {
  if (!selectedNewTag.value) return
  try {
    await tagApi.addTagToComic(route.params.id, selectedNewTag.value)
    showAddTag.value = false
    selectedNewTag.value = null
    await loadTags()
    ElMessage.success('标签已添加')
  } catch (e) {
    ElMessage.error('添加标签失败')
  }
}

async function removeTag(tag) {
  try {
    await tagApi.removeTagFromComic(route.params.id, tag.id)
    await loadTags()
    ElMessage.success('标签已移除')
  } catch (e) {
    ElMessage.error('移除标签失败')
  }
}

async function submitReview() {
  if (!newReview.value.trim()) return
  try {
    await reviewApi.create(route.params.id, { content: newReview.value })
    newReview.value = ''
    await loadReviews()
    ElMessage.success('评论已发表')
  } catch (e) {
    ElMessage.error('发表评论失败')
  }
}

async function deleteReview(reviewId) {
  try {
    await ElMessageBox.confirm('确定删除此评论？', '提示', { type: 'warning' })
    await reviewApi.deleteReview(reviewId)
    await loadReviews()
    ElMessage.success('评论已删除')
  } catch (e) {
    // User cancelled
  }
}

function startReading() {
  router.push(`/reader/${route.params.id}`)
}

function continueReading() {
  router.push(`/reader/${route.params.id}`)
}

onMounted(async () => {
  await Promise.all([
    loadComic(),
    loadCover(),
    loadTags(),
    loadAllTags(),
    loadReviews(),
    checkFavorite()
  ])
})
</script>

<style scoped lang="scss">
.comic-detail-view {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
}

.detail-cover {
  flex-shrink: 0;
  width: 300px;

  img {
    width: 100%;
    border-radius: 8px;
    box-shadow: 0 4px 16px var(--shadow-color);
  }
}

.cover-placeholder {
  width: 100%;
  height: 420px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  color: var(--text-tertiary);
}

.detail-info {
  flex: 1;
}

.comic-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 15px;
  color: var(--text-secondary);
}

.info-label {
  color: var(--text-tertiary);
  margin-right: 8px;
  min-width: 60px;
}

.tags-row {
  align-items: flex-start;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.comic-description {
  margin-top: 20px;
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    margin-bottom: 8px;
    color: var(--text-primary);
  }

  p {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
}

.reviews-section {
  border-top: 1px solid var(--border-color);
  padding-top: 24px;

  h3 {
    font-size: 18px;
    margin-bottom: 16px;
    color: var(--text-primary);
  }
}

.review-form {
  margin-bottom: 24px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.review-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-tertiary);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.loading-spinner {
  animation: spin 1s linear infinite;
  color: var(--text-tertiary);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
  }

  .detail-cover {
    width: 200px;
    margin: 0 auto;
  }
}
</style>
