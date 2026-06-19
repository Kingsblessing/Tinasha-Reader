# 📚 Tinasha Comic — 你的本地漫画管家

一个自己用的本地漫画阅读器，支持自动扫描本地目录、多种阅读模式、JM漫画一键导入，界面好看还能换暗色主题。

初衷很简单：市面上的阅读器要么广告多，要么功能臃肿，不如自己写一个干净的。

---

## ✨ 它能干嘛

**漫画管理**
- 自动扫描本地文件夹，识别漫画（支持文件夹/CBZ/CBR/7Z/PDF）
- 拖拽选目录，批量添加来源
- 标签分组、收藏夹、评分、评论，一套齐活

**阅读体验**
- 三种阅读模式：单页 / 双页 / 瀑布流，一键切换
- 进度条拖拽跳页、自动播放、全屏沉浸
- 键盘快捷键全覆盖（方向键翻页、D切模式、F全屏、P自动播放…）
- 阅读进度自动保存，下次打开接着看

**JM 导入**
- 输入车牌号，自动下载+导入，标签简介全读取
- 下载目录和图片格式可自定义

**数据统计**
- 今日/本周/本月阅读页数
- 总阅读时间、每日柱状图
- 最近阅读快速入口

**其他**
- 深色 / 浅色主题一键切换
- 手机端响应式布局，躺着也能看
- 数据一键备份/恢复（JSON 格式）
- 完整 REST API，方便后续扩展

---

## 🛠️ 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Java 21 + Spring Boot 3.4 + Spring JDBC |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3 + Vite 6 + Element Plus |
| 漫画下载 | jmcomic (Python CLI) |

---

## 🚀 快速开始

### 前置条件
- Java 21+
- Node.js 18+
- MySQL 8.0+

### 1. 创建数据库
```sql
CREATE DATABASE reader CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 改数据库密码
编辑 `reader-backend/src/main/resources/application.yml`，把 `password` 改成你自己的 MySQL 密码。

### 3. 一键启动
双击 `start.bat`，会弹出两个窗口：
- 后端：`http://localhost:8080`
- 前端：`http://localhost:5173`

### 4. 添加漫画来源
打开前端 → 设置 → 来源 → 添加来源 → 浏览选择你的漫画文件夹 → 扫描

搞定，开看 🎉

---

## 📂 项目结构

```
Reader/
├── reader-backend/          # Spring Boot 后端
│   ├── src/main/java/com/reader/
│   │   ├── controller/      # REST API（漫画、来源、标签、收藏、历史、备份、JM）
│   │   ├── service/         # 业务逻辑
│   │   ├── repository/      # 数据访问（Spring JDBC）
│   │   ├── model/           # 实体、DTO、VO
│   │   ├── util/            # 文件扫描、图片处理、漫画解析
│   │   └── config/          # CORS、静态资源
│   └── tools/jm/            # jmcomic 下载器 + 配置
│
├── reader-frontend/         # Vue 3 前端
│   └── src/
│       ├── views/           # 页面（书架、阅读器、收藏、历史、统计、设置）
│       ├── components/      # 组件（阅读器、卡片、侧边栏、工具栏）
│       ├── api/             # Axios API 封装
│       ├── composables/     # 组合式函数（主题、预加载、阅读设置）
│       └── stores/          # 状态管理
│
├── start.bat                # 一键启动
├── stop.bat                 # 一键停止
└── build-release.bat        # 打包发布
```

---

## ⌨️ 快捷键

| 按键 | 功能 |
|------|------|
| `←` `→` | 翻页 |
| `Space` | 下一页 |
| `Home` / `End` | 首页 / 末页 |
| `D` | 切换阅读模式 |
| `R` | 切换阅读方向 |
| `P` | 自动播放 |
| `[` `]` | 调整播放速度 |
| `F` | 全屏 |
| `?` | 快捷键帮助 |
| `Esc` | 返回 |

---

## 📡 API

后端提供完整 REST API，基础路径 `/api/v1`：

| 模块 | 端点 | 说明 |
|------|------|------|
| 漫画 | `GET /comics` | 列表（分页/搜索/筛选） |
| 来源 | `POST /sources` | 添加来源 |
| 标签 | `GET /tags` | 标签管理 |
| 收藏 | `POST /favorites` | 收藏/取消 |
| 历史 | `GET /history` | 阅读记录 |
| 统计 | `GET /history/stats` | 阅读统计 |
| 备份 | `GET /backup/export` | 导出全部数据 |
| JM | `POST /jm/import` | 车牌号导入漫画 |

---

## 📝 备注

- 数据库首次启动会自动建表（`spring.sql.init.mode=always`）
- JM 导入需要 `jmcomic.exe`，已放在 `reader-backend/tools/jm/` 下
- 打包发布运行 `build-release.bat`，产物是一个可执行 JAR
- 局域网访问：确保防火墙放行 8080 和 5173 端口

---

> 写着玩的，够用就行。有问题提 issue，没问题点个 star ⭐
