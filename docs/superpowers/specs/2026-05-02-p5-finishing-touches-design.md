# P5 收尾与打磨 — 设计文档

---

## 概述

P5 是开发周期最后一阶段，目标：系统完整、体验良好、异常处理到位。分三个子任务：全局异常处理（P5.1）、前端交互完善（P5.2）、数据与体验收尾（P5.3）。

## P5.1 全局异常处理

**方案：** 新增 `BusinessException` + `@ControllerAdvice` GlobalExceptionHandler，重构所有 Controller 移除 try-catch，改为 throw BusinessException。

**处理流程：**
- `BusinessException` → Flash 消息 + 重定向回来源页（Referer fallback `/`）
- 其他 `Exception` → 日志记录 + `error.html` 通用错误页
- 404 → `404.html` 自定义页面

**重构范围（8 个文件）：**
- `AuthController.register()` — "注册失败"
- `ProfileController.updateProfile()` — "更新失败"
- `user/ApplicationController.submit()` / `cancel()` — "提交失败" / "取消失败"
- `admin/ApplicationController.review()` — "审核失败"
- `admin/BreedController.delete()` — "删除失败"
- `admin/PetManageController.create()` / `update()` — "发布失败" / "更新失败"
- `admin/UserController.create()` / `update()` / `delete()` — 各操作失败

## P5.2 前端交互完善

已有项（不重复）：图片上传预览、删除确认 Modal、AI 问答 Fetch 异步

**新增：**

1. **密码一致性 JS 校验** — `register.html` 表单 submit 事件拦截，对比 password 与 confirmPassword，不一致则阻止提交并 alert/显示提示。

2. **品种联动** — `pet-form.html` 增加宠物类型下拉框，切换时 fetch `GET /api/breeds?petType=...` 返回 JSON 数组更新品种下拉框选项。需新增 BreedController 中的 JSON 接口。

## P5.3 数据与体验收尾

1. **data.sql 充实** — 增加多个品种（猫/狗/兔各多个品种）和宠物记录（覆盖不同状态）。
2. **导航高亮** — `nav.html` 通过请求路径给当前页 `.nav-link` 加 `.active` 类。
3. **页面标题一致性** — 逐页检查 model 中有 `title` 属性，header fragment 中 `<title>` 正确渲染。
4. **表单提示** — 各表单补充 placeholder 和 text-muted 说明。

## 文件变更清单

```
新增:
  exception/BusinessException.java
  handler/GlobalExceptionHandler.java
  templates/error.html
  templates/404.html

修改（异常重构）:
  controller/AuthController.java
  controller/user/ProfileController.java
  controller/user/ApplicationController.java
  controller/admin/ApplicationController.java
  controller/admin/BreedController.java
  controller/admin/PetManageController.java
  controller/admin/UserController.java

修改（前端交互）:
  templates/auth/register.html
  templates/admin/pet-form.html
  controller/admin/BreedController.java  (新增 /api/breeds 接口)

修改（体验）:
  templates/fragments/nav.html
  data.sql
```

## 验证要点

- 所有业务异常有中文 Flash 提示并正确重定向
- 404/500 有自定义页面
- 注册密码不一致在前端被拦截
- 品种下拉联动正常
- 导航高亮当前页面
- 完整用户旅程无缺陷
