# pet-mgt 宠物领养平台 — 开发周期计划

---

## 概览

| 属性 | 值 |
|------|-----|
| 总阶段 | 6 个（P0 ~ P5） |
| 预计总工时 | 约 40~60 小时 |
| 开发模式 | 按阶段顺序推进，每阶段完成后验收 |
| 分支策略 | 每阶段一个 feature 分支，完成后合并 |

---

## 阶段总览

```
P0 基础框架 ──→ P1 宠物浏览 ──→ P2 用户功能 ──→ P3 管理功能 ──→ P4 AI 模块 ──→ P5 收尾
   1~2天           1~2天          2~3天          3~4天         2~3天         1~2天
```

---

## P0 — 基础框架搭建

**目标：** 项目可启动、可登录、有角色权限、有基础页面布局。

### P0.1 项目初始化
| 项 | 内容 |
|----|------|
| 产出 | `pom.xml`、`PetMgtApplication.java`、`application.properties` |
| 要点 | Spring Boot 4.x 父项目，引入 Web / Thymeleaf / Security / MyBatis-Plus / MySQL / Lombok / Thymeleaf Security 扩展 |
| 验证 | `mvn spring-boot:run` 启动成功 |

### P0.2 数据库建表
| 项 | 内容 |
|----|------|
| 产出 | `schema.sql`、`data.sql` |
| 涉及表 | `user`、`role`、`user_role`、`pet_breed`、`pet`、`pet_image`、`adoption_application`、`ai_match_record`、`ai_review_record` （共 9 张） |
| 验证 | 启动后数据库自动初始化，表结构存在，初始数据可查 |

### P0.3 Spring Security 配置
| 项 | 内容 |
|----|------|
| 产出 | `SecurityConfig.java`、`UserDetailsServiceImpl.java`、`UserMapper.java`、`RoleMapper.java` |
| 要点 | URL 权限矩阵（`/`、`/pets/**`、`/login`、`/register` 放行；`/user/**` 需登录；`/admin/**` 需 ADMIN 角色）；BCrypt 密码编码；自定义登录页 |
| 验证 | 访问 `/user/**` 跳转登录；admin/user 可登录；登出正常 |

### P0.4 基础页面布局
| 项 | 内容 |
|----|------|
| 产出 | `fragments/header.html`、`fragments/nav.html`、`fragments/footer.html`、`home.html`、`auth/login.html`、`auth/register.html` |
| 要点 | Bootstrap 5 CDN；导航栏角色自适应（`sec:authorize`）；响应式折叠导航 |
| 验证 | 首页可访问；登录/注册页面可访问；导航栏根据登录状态变化 |

### P0.5 静态资源与 MVC 配置
| 项 | 内容 |
|----|------|
| 产出 | `MvcConfig.java`、`MyBatisPlusConfig.java` |
| 要点 | `/uploads/**` 映射到 `file:uploads/`；MyBatis-Plus 分页插件 |
| 验证 | 静态资源可加载；分页插件生效 |

### P0 验证清单
- [ ] 项目可正常启动
- [ ] 数据库 9 张表自动创建
- [ ] admin / 123456 可登录并看到管理入口
- [ ] user / 123456 可登录并看不到管理入口
- [ ] 未登录用户访问 `/user/profile` 跳转登录页
- [ ] 首页有导航栏且响应式折叠正常

---

## P1 — 宠物浏览功能

**目标：** 未登录用户可浏览宠物列表、查看详情、分页筛选。

### P1.1 宠物列表页
| 项 | 内容 |
|----|------|
| 产出 | `PetController.java`、`PetService.java`、`pet/list.html`、`fragments/pagination.html` |
| 要点 | 卡片布局（`col-12 col-md-6 col-lg-4 col-xl-3`）；分页（MyBatis-Plus Page）；按类型/品种/性别/状态/名称筛选；封面缩略图展示；状态 badge |
| 涉及表 | `pet`、`pet_breed`、`pet_image` |
| 验证 | 列表分页展示；筛选条件生效；响应式列数变化 |

### P1.2 宠物详情页
| 项 | 内容 |
|----|------|
| 产出 | `pet/detail.html`（PetController 增加详情接口） |
| 要点 | 完整字段展示；多图片缩略图 + 点击放大；可领养时显示"申请领养"按钮；已领养时禁用按钮；未登录点击跳转登录 |
| 验证 | 详情页信息完整；图片可放大；状态联动按钮正确 |

### P1.3 首页
| 项 | 内容 |
|----|------|
| 产出 | `HomeController.java`、`home.html` |
| 要点 | 展示最新/推荐宠物卡片；统计数字（可选）；欢迎信息 |
| 验证 | 首页有内容展示 |

### P1 验证清单
- [ ] `/pets` 宠物列表正常分页
- [ ] 筛选条件全部生效（类型、品种、性别、状态、名称）
- [ ] 手机端单列、平板双列、桌面多列
- [ ] 宠物详情页信息完整
- [ ] 图片点击可放大
- [ ] 未登录点"申请领养"跳转登录

---

## P2 — 用户功能

**目标：** 完整用户侧主流程：注册 → 浏览 → 申请 → 查看申请 → 取消申请。

### P2.1 用户注册
| 项 | 内容 |
|----|------|
| 产出 | `AuthController.java`（register 接口）、`UserService.java`、`RegisterForm.java`、`auth/register.html` |
| 要点 | 字段校验（用户名非空/不重复、密码一致性、邮箱格式）；BCrypt 加密；默认 ROLE_USER；注册成功跳转登录页 |
| 验证 | 注册成功后可登录；重复用户名报错；密码不一致报错 |

### P2.2 个人中心
| 项 | 内容 |
|----|------|
| 产出 | `ProfileController.java`、`user/profile.html` |
| 要点 | 显示/修改邮箱、头像；`SecurityUtil.java` 获取当前用户 |
| 验证 | 可查看个人信息；可修改邮箱和头像 |

### P2.3 提交领养申请
| 项 | 内容 |
|----|------|
| 产出 | `ApplicationForm.java`、`user/ApplicationController.java`、`ApplicationService.java`、`user/application-form.html` |
| 要点 | 校验宠物状态为 available；校验无重复 pending 申请；`@Transactional`；提交后宠物状态变更为 pending |
| 涉及表 | `adoption_application`、`pet` |
| 验证 | 可提交申请；重复申请被拦截；已领养宠物不可申请；宠物状态联动正确 |

### P2.4 我的申请
| 项 | 内容 |
|----|------|
| 产出 | `user/applications.html`（列表 + 状态标签） |
| 要点 | 状态颜色标签（pending=黄、approved=绿、rejected=红、cancelled=灰）；分页；可取消 pending 申请 |
| 验证 | 列表正确展示；状态标签颜色正确；取消申请生效；取消后宠物状态恢复 |

### P2 验证清单
- [ ] 注册 → 登录 → 浏览 → 申请 → 查看申请 全流程通
- [ ] 重复申请被拦截
- [ ] 取消申请后宠物恢复可领养
- [ ] 个人中心可修改信息
- [ ] 用户只能看到自己的申请

---

## P3 — 管理功能

**目标：** 完整管理员侧功能：统计、用户管理、品种管理、宠物管理（含图片上传）、申请审核。

### P3.1 后台首页统计
| 项 | 内容 |
|----|------|
| 产出 | `admin/AdminController.java`、`admin/index.html` |
| 要点 | 统计卡片：用户总数、宠物总数、可领养数、已领养数、待审核数 |
| 验证 | 统计数据正确 |

### P3.2 用户管理
| 项 | 内容 |
|----|------|
| 产出 | `admin/UserController.java`、`admin/users.html`、`admin/user-form.html` |
| 要点 | 列表 + 分页；新增用户（含角色分配）；编辑用户（邮箱/头像/启用/角色）；删除用户（确认弹窗，不能删除自己） |
| 涉及表 | `user`、`user_role` |
| 验证 | CRUD 全部正常；删除保护生效 |

### P3.3 品种管理
| 项 | 内容 |
|----|------|
| 产出 | `admin/BreedController.java`、`BreedService.java`、`admin/breeds.html`、`admin/breed-form.html` |
| 要点 | 列表分页；新增/编辑（品种名称、宠物类型、描述）；删除时检查关联宠物 |
| 涉及表 | `pet_breed` |
| 验证 | CRUD 正常；有宠物关联的品种不可删除 |

### P3.4 宠物管理
| 项 | 内容 |
|----|------|
| 产出 | `admin/PetManageController.java`、`admin/pets.html`、`admin/pet-form.html`、`FileStorageService.java`、`FileUtil.java`、`FileUploadConfig.java` |
| 要点 | 列表分页 + 搜索筛选；新增宠物（含多图上传、封面设置、上传前预览）；编辑宠物（修改信息 + 图片管理：保留/新增/删除/重设封面）；删除宠物（确认弹窗，有申请记录的限制） |
| 涉及表 | `pet`、`pet_image` |
| 验证 | 图片上传成功（限 jpg/jpeg/png、≤2MB、UUID 命名）；缩略图生成；宠物 CRUD 正常；图片增删改正常 |

### P3.5 申请审核
| 项 | 内容 |
|----|------|
| 产出 | `admin/ApplicationController.java`、`admin/applications.html`、`admin/application-detail.html` |
| 要点 | 列表分页 + 按状态/宠物名/申请人筛选；查看详情（含申请人信息 + 宠物信息）；审核通过（当前→approved + 宠物→adopted + 其他 pending→rejected）；审核拒绝（填写原因 + 宠物恢复 available） |
| 涉及表 | `adoption_application`、`pet` |
| 验证 | 通过后宠物状态联动正确；其他申请自动拒绝；拒绝后宠物恢复；审核备注保存 |

### P3 验证清单
- [ ] 后台统计数字正确
- [ ] 用户 CRUD 正常
- [ ] 品种 CRUD 正常，删除保护生效
- [ ] 宠物发布含多图上传正常
- [ ] 缩略图生成并正确展示
- [ ] 审核通过全联动正确
- [ ] 审核拒绝全联动正确

---

## P4 — AI 智能模块

**目标：** AI 功能完整可用：匹配助手、匹配历史、审核建议、养宠问答。

### P4.1 AiService 通用封装
| 项 | 内容 |
|----|------|
| 产出 | `AiService.java` |
| 要点 | DeepSeek API（OpenAI 兼容格式）；RestTemplate 调用；system + user messages；超时 10s；异常返回 null |
| 验证 | 用简单 Prompt 测试调用成功；超时/异常不崩溃 |

### P4.2 AI 宠物匹配
| 项 | 内容 |
|----|------|
| 产出 | `AiMatchService.java`、`AiMatchRequest.java`、`AiMatchResult.java`、`user/AiMatchController.java`、`user/ai-match.html`、`user/ai-match-result.html` |
| 要点 | 问卷表单 → 查询 available 宠物 → 构建 Prompt → 调 AI → 解析 JSON 数组 → 保存记录 → 展示卡片结果 |
| 涉及表 | `ai_match_record` |
| 验证 | 问卷提交后返回推荐结果；结果解析正确；记录保存到数据库 |

### P4.3 AI 匹配历史
| 项 | 内容 |
|----|------|
| 产出 | `user/ai-match-history.html`、`admin/AiRecordController.java`、`admin/ai-records.html` |
| 要点 | 用户看自己的历史（倒序）；管理员看所有用户记录 |
| 验证 | 历史记录正确展示；权限隔离正确 |

### P4.4 AI 审核建议
| 项 | 内容 |
|----|------|
| 产出 | `AiReviewService.java`、`AiReviewResult.java`、`admin/application-detail.html`（嵌入 AI 建议区域） |
| 要点 | 申请详情页调用 AI；传入申请人 + 宠物信息；返回适配度/适合点/风险点/建议；结果保存到 `ai_review_record`；页面标注"AI 建议仅供参考" |
| 涉及表 | `ai_review_record` |
| 验证 | 审核建议可生成；保存正确；不替代人工审核 |

### P4.5 AI 养宠问答
| 项 | 内容 |
|----|------|
| 产出 | `AiChatService.java`、`user/AiChatController.java`、`user/ai-chat.html` |
| 要点 | 输入框 + 发送按钮；Fetch API 异步请求；结果展示在页面；医疗问题拒绝回答并提示咨询兽医 |
| 验证 | 问答正常；医疗问题被拦截；AI 不可用时友好提示 |

### P4 验证清单
- [ ] AI 匹配返回合理推荐结果
- [ ] 匹配记录保存并可在历史页查看
- [ ] 管理员可查看所有 AI 匹配记录
- [ ] 审核建议在申请详情页展示
- [ ] 养宠问答可用
- [ ] AI 不可用时（关 API Key）所有核心业务不受影响
- [ ] "AI 建议仅供参考"标注可见

---

## P5 — 收尾与打磨

**目标：** 系统完整、体验良好、异常处理到位。

### P5.1 全局异常处理
| 项 | 内容 |
|----|------|
| 产出 | `GlobalExceptionHandler.java`、`BusinessException.java`、`error.html`、`404.html` |
| 要点 | 业务异常 → Flash 消息 + 重定向；通用异常 → 错误页；404 → 自定义页面 |
| 验证 | 各类异常场景有友好提示 |

### P5.2 前端交互完善
| 项 | 内容 |
|----|------|
| 要点 | 密码一致性 JS 校验；图片上传预览；删除确认 Modal；AI 问答 Fetch 异步；品种联动（类型change→加载品种）；表单 HTML5 + JS 双重校验 |
| 验证 | 各交互功能正常 |

### P5.3 数据与体验收尾
| 项 | 内容 |
|----|------|
| 要点 | 测试数据完善（多品种、多宠物）；所有页面标题统一规范；操作成功/失败 Flash 消息；表单 placeholder 和提示；导航高亮当前页 |
| 验证 | 完整走通所有用户旅程 |

### P5 验证清单
- [ ] 所有异常有友好提示
- [ ] JS 交互全部正常
- [ ] 测试数据丰富可演示
- [ ] 管理员完整流程：登录 → 统计 → 品种管理 → 宠物发布 → 审核申请 → 查看 AI 记录
- [ ] 用户完整流程：注册 → 登录 → 浏览 → AI 匹配 → 提交申请 → 查看申请 → AI 问答
- [ ] 未登录用户流程：浏览首页 → 宠物列表 → 宠物详情 → 点申请跳转登录
- [ ] 所有页面响应式正常

---

## 依赖关系

```
P0 ──────→ P1 ──────→ P2 ──────→ P3 ──────→ P4 ──────→ P5
基础框架   宠物浏览    用户功能    管理功能    AI模块      收尾

P2 可与 P3 部分并行（P2 用户侧 + P3 管理员侧属不同 Controller）
P4 依赖 P2/P3 的业务数据（需有宠物 + 申请数据才能测 AI）
```

## 每阶段开发流程

```
1. 创建 feature 分支（如 feature/p1-pet-browse）
2. 按本阶段任务清单逐项实现
3. 自测验证清单全部通过
4. 代码审查
5. 合并到主分支
6. 进入下一阶段
```

---

## 文件产出对照（按阶段）

### P0
```
pom.xml
src/main/java/com/petmgt/PetMgtApplication.java
src/main/java/com/petmgt/config/SecurityConfig.java
src/main/java/com/petmgt/config/MvcConfig.java
src/main/java/com/petmgt/config/MyBatisPlusConfig.java
src/main/java/com/petmgt/entity/User.java
src/main/java/com/petmgt/entity/Role.java
src/main/java/com/petmgt/mapper/UserMapper.java
src/main/java/com/petmgt/mapper/RoleMapper.java
src/main/java/com/petmgt/service/UserDetailsServiceImpl.java
src/main/java/com/petmgt/controller/HomeController.java
src/main/java/com/petmgt/controller/AuthController.java
src/main/resources/application.properties
src/main/resources/schema.sql
src/main/resources/data.sql
src/main/resources/templates/fragments/header.html
src/main/resources/templates/fragments/nav.html
src/main/resources/templates/fragments/footer.html
src/main/resources/templates/home.html
src/main/resources/templates/auth/login.html
src/main/resources/templates/auth/register.html
```

### P1
```
src/main/java/com/petmgt/entity/Pet.java
src/main/java/com/petmgt/entity/Breed.java
src/main/java/com/petmgt/entity/PetImage.java
src/main/java/com/petmgt/mapper/PetMapper.java
src/main/java/com/petmgt/mapper/BreedMapper.java
src/main/java/com/petmgt/mapper/PetImageMapper.java
src/main/java/com/petmgt/service/PetService.java
src/main/java/com/petmgt/dto/PetSearchCriteria.java
src/main/java/com/petmgt/controller/PetController.java
src/main/resources/templates/pet/list.html
src/main/resources/templates/pet/detail.html
src/main/resources/templates/fragments/pagination.html
```

### P2
```
src/main/java/com/petmgt/entity/Application.java
src/main/java/com/petmgt/mapper/ApplicationMapper.java
src/main/java/com/petmgt/service/UserService.java
src/main/java/com/petmgt/service/ApplicationService.java
src/main/java/com/petmgt/dto/RegisterForm.java
src/main/java/com/petmgt/dto/ApplicationForm.java
src/main/java/com/petmgt/util/SecurityUtil.java
src/main/java/com/petmgt/controller/user/ProfileController.java
src/main/java/com/petmgt/controller/user/ApplicationController.java
src/main/resources/templates/user/profile.html
src/main/resources/templates/user/applications.html
src/main/resources/templates/user/application-form.html
```

### P3
```
src/main/java/com/petmgt/config/FileUploadConfig.java
src/main/java/com/petmgt/service/FileStorageService.java
src/main/java/com/petmgt/service/BreedService.java
src/main/java/com/petmgt/util/FileUtil.java
src/main/java/com/petmgt/controller/admin/AdminController.java
src/main/java/com/petmgt/controller/admin/UserController.java
src/main/java/com/petmgt/controller/admin/BreedController.java
src/main/java/com/petmgt/controller/admin/PetManageController.java
src/main/java/com/petmgt/controller/admin/ApplicationController.java
src/main/resources/templates/admin/index.html
src/main/resources/templates/admin/users.html
src/main/resources/templates/admin/user-form.html
src/main/resources/templates/admin/breeds.html
src/main/resources/templates/admin/breed-form.html
src/main/resources/templates/admin/pets.html
src/main/resources/templates/admin/pet-form.html
src/main/resources/templates/admin/applications.html
src/main/resources/templates/admin/application-detail.html
```

### P4
```
src/main/java/com/petmgt/entity/AiMatchRecord.java
src/main/java/com/petmgt/entity/AiReviewRecord.java
src/main/java/com/petmgt/mapper/AiMatchRecordMapper.java
src/main/java/com/petmgt/mapper/AiReviewRecordMapper.java
src/main/java/com/petmgt/service/ai/AiService.java
src/main/java/com/petmgt/service/ai/AiMatchService.java
src/main/java/com/petmgt/service/ai/AiReviewService.java
src/main/java/com/petmgt/service/ai/AiChatService.java
src/main/java/com/petmgt/dto/AiMatchRequest.java
src/main/java/com/petmgt/dto/AiMatchResult.java
src/main/java/com/petmgt/dto/AiReviewResult.java
src/main/java/com/petmgt/controller/user/AiMatchController.java
src/main/java/com/petmgt/controller/user/AiChatController.java
src/main/java/com/petmgt/controller/admin/AiRecordController.java
src/main/resources/templates/user/ai-match.html
src/main/resources/templates/user/ai-match-result.html
src/main/resources/templates/user/ai-match-history.html
src/main/resources/templates/user/ai-chat.html
src/main/resources/templates/admin/ai-records.html
```

### P5
```
src/main/java/com/petmgt/handler/GlobalExceptionHandler.java
src/main/java/com/petmgt/exception/BusinessException.java
src/main/resources/templates/error.html
src/main/resources/templates/404.html
```

---

## 风险与注意事项

| 风险 | 应对 |
|------|------|
| Spring Boot 4.x 与 MyBatis-Plus 兼容性 | P0 第一时间验证启动 + 数据库操作 |
| 图片上传路径问题 | 使用相对路径 `uploads/pets/`，MvcConfig 映射 |
| AI API 调用不稳定 | 所有 AI 调用有 try-catch，失败不影响核心业务 |
| 宠物状态并发更新 | `@Transactional` + 数据库行锁，P3 审核逻辑中处理 |
| Thymeleaf + Spring Security 6 标签 | P0 引入 `thymeleaf-extras-springsecurity6` |
