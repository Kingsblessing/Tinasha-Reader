export const DEFAULT_SETTINGS = {
  theme: 'light',
  language: 'zh-CN',
  readingMode: 'single',
  readingDirection: 'ltr',
  fitMode: 'width',
  preloadPages: 3,
  animationEnabled: true,
  autoSaveProgress: true,
  pageSize: 20
}

export const SUPPORTED_IMAGE_TYPES = [
  'image/jpeg',
  'image/png',
  'image/webp',
  'image/gif',
  'image/bmp'
]

export const ReadingMode = {
  SINGLE: 'single',
  DOUBLE: 'double',
  SCROLL: 'scroll'
}

export const ReadingModeLabels = {
  [ReadingMode.SINGLE]: '单页模式',
  [ReadingMode.DOUBLE]: '双页模式',
  [ReadingMode.SCROLL]: '滚动模式'
}

export const ReadingModeIcons = {
  [ReadingMode.SINGLE]: 'Document',
  [ReadingMode.DOUBLE]: 'Notebook',
  [ReadingMode.SCROLL]: 'Reading'
}

export const FitMode = {
  WIDTH: 'width',
  HEIGHT: 'height',
  WINDOW: 'window',
  ORIGINAL: 'original'
}

export const FitModeLabels = {
  [FitMode.WIDTH]: '适应宽度',
  [FitMode.HEIGHT]: '适应高度',
  [FitMode.WINDOW]: '适应窗口',
  [FitMode.ORIGINAL]: '原始尺寸'
}

export const SortOptions = {
  TITLE: 'title',
  DATE: 'date',
  RATING: 'rating',
  LAST_READ: 'lastRead',
  CREATED: 'created'
}

export const SortOptionLabels = {
  [SortOptions.TITLE]: '按标题',
  [SortOptions.DATE]: '按日期',
  [SortOptions.RATING]: '按评分',
  [SortOptions.LAST_READ]: '最近阅读',
  [SortOptions.CREATED]: '最近添加'
}

export const SourceStatus = {
  IDLE: 'IDLE',
  SCANNING: 'SCANNING',
  ERROR: 'ERROR'
}

export const SourceStatusLabels = {
  [SourceStatus.IDLE]: '空闲',
  [SourceStatus.SCANNING]: '扫描中',
  [SourceStatus.ERROR]: '错误'
}

export const ComicStatus = {
  ONGOING: 'ONGOING',
  COMPLETED: 'COMPLETED',
  HIATUS: 'HIATUS'
}

export const ComicStatusLabels = {
  [ComicStatus.ONGOING]: '连载中',
  [ComicStatus.COMPLETED]: '已完结',
  [ComicStatus.HIATUS]: '休刊'
}

export const SourceType = {
  FOLDER: 'FOLDER',
  CBZ: 'CBZ',
  CBR: 'CBR',
  SEVEN_Z: 'SEVEN_Z',
  PDF: 'PDF'
}

export const SourceTypeLabels = {
  [SourceType.FOLDER]: '文件夹',
  [SourceType.CBZ]: 'CBZ压缩包',
  [SourceType.CBR]: 'CBR压缩包',
  [SourceType.SEVEN_Z]: '7z压缩包',
  [SourceType.PDF]: 'PDF文件'
}
