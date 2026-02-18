# Wang AI Code Mother Front

基于 Vue 3 + Vite + TypeScript 的 AI 代码生成平台前端项目。该项目提供了一个可视化的界面，允许用户通过自然语言与 AI 进行交互，生成、预览并可视化编辑网站代码。

## ✨ 核心特性

- **🤖 AI 对话生成**: 通过与 AI 助手对话，自动生成 HTML/CSS/JS 代码或多文件应用。
- **👁️ 可视化编辑**: 
  - 支持直接在预览窗口中通过点击元素进行可视化编辑。
  - 选中元素后可查看其标签、文本、选择器及源代码片段。
  - 编辑操作实时反馈给 AI，辅助生成更精准的代码修改建议。
- **📱 响应式预览**: 实时预览生成的网页效果，支持不同设备尺寸模拟。
- **📦 应用管理**: 创建、查看、更新和删除 AI 应用。
- **🚀 一键部署**: 支持将生成的应用一键部署到服务器。
- **💾 代码下载**: 支持下载生成的应用代码包。
- **👥 用户系统**: 完整的用户注册、登录及个人信息管理功能。
- **👮 管理后台**: 管理员可对应用和用户进行统一管理。

## 🛠️ 技术栈

- **核心框架**: [Vue 3](https://vuejs.org/) (Composition API)
- **构建工具**: [Vite](https://vitejs.dev/)
- **语言**: [TypeScript](https://www.typescriptlang.org/)
- **UI 组件库**: [Ant Design Vue](https://www.antdv.com/)
- **状态管理**: [Pinia](https://pinia.vuejs.org/)
- **路由管理**: [Vue Router](https://router.vuejs.org/)
- **HTTP 请求**: [Axios](https://axios-http.com/)
- **Markdown 渲染**: [Marked](https://marked.js.org/)
- **代码规范**: ESLint + Prettier

## 📂 目录结构

```
src/
├── api/                # API 接口定义
├── hooks/              # 组合式函数 (如 useVisualEditor.ts)
├── layouts/            # 布局组件
├── router/             # 路由配置
├── stores/             # Pinia 状态管理
├── views/              # 页面组件
│   ├── admin/          # 管理员页面
│   ├── app/            # 应用相关页面 (聊天、编辑)
│   ├── user/           # 用户相关页面
│   └── ...             # 其他页面
├── App.vue             # 根组件
├── main.ts             # 入口文件
└── request.ts          # Axios 封装
```

## 🚀 快速开始

### 环境要求

- Node.js >= 18
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 开发模式运行

```bash
npm run dev
```

访问 http://localhost:5173 即可看到项目运行效果。

### 构建生产环境

```bash
npm run build
```

### 代码检查与格式化

```bash
# 运行 ESLint 检查
npm run lint

# 运行 Prettier 格式化
npm run format
```

### 生成 API 接口类型

```bash
npm run openapi2ts
```

## 📝 可视化编辑功能说明

项目集成了强大的可视化编辑功能 (`src/hooks/useVisualEditor.ts`)，主要包含：

1.  **元素高亮**: 鼠标悬停在预览区域的元素上时，会自动显示高亮边框。
2.  **元素选中**: 点击元素即可选中，并获取该元素的详细信息（标签名、文本内容、CSS 选择器、HTML 代码片段等）。
3.  **智能提示**: 选中的元素信息会自动注入到 AI 对话的提示词中，帮助 AI 理解用户想要修改的具体位置。
4.  **样式隔离**: 编辑器的样式和脚本通过 iframe 注入，不影响生成的代码本身。

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进本项目！

## 📄 许可证

[MIT](LICENSE)