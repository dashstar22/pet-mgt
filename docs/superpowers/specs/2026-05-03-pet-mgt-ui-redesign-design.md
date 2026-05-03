# UI Redesign: 活泼趣味风 — 浆果跳跳糖

## 背景

当前 pet-mgt 平台使用纯原生 Bootstrap 5.3 CDN，无任何自定义样式，界面单调统一，缺乏品牌识别度和视觉层次感。需要一个完整的视觉升级方案。

## 设计方向

- **风格**: 活泼趣味风（鲜艳配色、大色块、有趣的动效、年轻化体验）
- **配色方案**: 浆果跳跳糖
  - Primary: `#7C3AED` (紫)
  - Accent: `#F472B6` (粉)
  - Gold: `#FBBF24` (金)
  - Dark: `#1E1B4B` (深紫黑)
  - Text: `#334155` (深灰)
  - Muted: `#94A3B8` (浅灰)
  - Bg: `#F8FAFC` (浅灰白)
- **Hero 风格**: 渐变插画风（大色块渐变背景 + 宠物 emoji 装饰）
- **实现方式**: 方案 A — Custom CSS 叠加 Bootstrap

## Design System

### CSS Variables

```css
:root {
  --color-primary: #7C3AED;
  --color-accent: #F472B6;
  --color-gold: #FBBF24;
  --color-dark: #1E1B4B;
  --color-text: #334155;
  --color-muted: #94A3B8;
  --color-bg: #F8FAFC;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;
  --shadow-sm: 0 2px 8px rgba(124,58,237,0.08);
  --shadow-md: 0 4px 20px rgba(124,58,237,0.12);
  --shadow-lg: 0 8px 32px rgba(124,58,237,0.16);
  --transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
```

### 排版

- 标题使用 `#1E1B4B` 深色，正文 `#334155`，辅助文本 `#94A3B8`
- 字重梯度: 800 (大标题) → 700 (h2/h3) → 600 (lead) → 400 (body) → 400 (small)

### 按钮

- Primary: 紫色→粉色渐变，圆角药丸形状
- Outline: 紫色边框 + 透明背景
- Ghost: 浅紫背景 + 紫色文字
- Dark: 深紫黑背景

### 状态标签

- 可领养: 浅绿背景 + 绿色文字 + 脉冲呼吸动画
- 待审核: 浅黄背景 + 橙色文字
- 已领养: 浅灰背景 + 灰色文字

## 页面改造范围

| 页面 | 改造内容 |
|------|---------|
| 首页 | Hero 区域（渐变背景 + emoji 装饰 + 渐变按钮），宠物类型快捷入口，最新宠物卡片 |
| 宠物列表 | 筛选卡片样式，宠物卡片（圆角+阴影+hover 上浮），分页组件 |
| 宠物详情 | 图片画廊，信息表格，领养按钮 |
| 登录/注册 | 表单卡片居中，按钮，链接样式 |
| 个人中心 | 信息卡片，表单样式 |
| AI 匹配/问答 | 聊天界面，匹配结果卡片 |
| 后台管理 | 表格，表单，仪表盘 |
| 公共组件 | 导航栏（紫色渐变），页脚，弹窗 |

## 微交互与动画

| 位置 | 效果 | 实现 |
|------|------|------|
| 卡片 hover | 上浮 4px + 阴影增强 | CSS transition |
| 按钮 hover | 轻微缩放 + 亮度提升 | CSS transform + filter |
| 导航链接 hover | 下划线滑动出现 | CSS ::after pseudo-element |
| 页面加载 | 卡片淡入上移 | CSS @keyframes animation |
| 可领养标签 | 柔和脉冲呼吸 | CSS animation |
| 通知/提示 | 滑入 + 自动消失 | Bootstrap 原生 + 自定义 |

## 实现计划

### 文件变更

1. **新增**: `src/main/resources/static/css/pet-mgt.css` — 所有自定义样式
2. **修改**: `src/main/resources/templates/fragments/header.html` — 引入新 CSS 文件（1 行）
3. **修改**: 各页面模板 — 少量 class 调整（将原有 Bootstrap class 替换/补充为自定义 class）

### 改动原则

- 不修改 Bootstrap CDN 引入，仅在其上层叠加
- 模板 HTML 结构尽量不变，优先通过 CSS 选择器覆盖样式
- 只在需要新增结构（如 Hero 渐变背景）时才修改 HTML
- 空状态、错误页面等一并升级

## 不包含

- 宠物真实照片上传（后台上传功能已存在，不涉及 UI 改造）
- 后台管理页面详细设计（将在后续迭代中处理）
- 深色模式
- 国际化/多语言
- 移动端 App
