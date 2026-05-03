# pet-mgt 宠物领养平台

基于 Spring Boot 的宠物领养平台，支持宠物信息管理、领养申请审核、AI 智能匹配等功能。

**技术栈**：Spring Boot 3.5.6 + MyBatis-Plus 3.5.9 + Thymeleaf + Bootstrap 5 + MySQL + Spring Security

## 运行方式

1. **环境要求**：JDK 17+、Maven 3.6+、MySQL 8.0+

2. **创建数据库**：
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pet_mgt DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```

3. **配置环境变量**（可选，不配时使用默认值）：
   - `DB_PASSWORD`：数据库密码（默认为 `123456`）
   - `DEEPSEEK_API_KEY`：DeepSeek API Key（不配则 AI 功能不可用）

4. **启动项目**：
   ```bash
   # 开发模式
   mvn spring-boot:run

   # 打包运行
   mvn package -DskipTests && java -jar target/petmgt-0.0.1-SNAPSHOT.jar
   ```

5. **访问地址**：http://localhost:8080

6. 项目首次启动时会自动创建表结构（`schema.sql`）并插入初始数据（`data.sql`）

## 测试账号

密码均为 `123456`

| 角色 | 用户名 | 说明 |
|------|--------|------|
| 管理员 | `admin` | 后台管理、宠物管理、申请审核 |
| 普通用户 | `user` | 浏览宠物、提交申请、使用 AI 匹配 |

## 主要功能

### 前台（公共）

- 首页、宠物列表、宠物详情
- 登录、注册

### 普通用户（`/user/**`）

- 个人中心
- 领养申请提交与查看
- AI 宠物匹配助手
- AI 养宠问答

### 管理员（`/admin/**`）

- 后台首页统计
- 用户管理、角色分配
- 宠物品种管理
- 宠物信息管理（含图片上传）
- 领养申请审核
- AI 审核辅助建议
