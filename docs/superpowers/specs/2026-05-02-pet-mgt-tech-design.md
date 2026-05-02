# pet-mgt 宠物领养平台 — 技术方案设计

---

## 1. 技术选型

| 技术 | 版本/说明 | 用途 |
|------|-----------|------|
| Spring Boot | 4.x | 后端主框架 |
| Spring MVC | 内置 | Web 请求处理 |
| Thymeleaf | 内置 | 服务端页面模板 |
| Bootstrap | 5.x | 响应式前端布局 |
| MyBatis-Plus | 3.5+ | 数据库访问、分页、条件构造器 |
| MySQL | 8.0+ | 数据库 |
| Spring Security | 6.x（Spring Boot 4.x 内置） | 登录认证与角色权限 |
| DeepSeek API | OpenAI 兼容格式 | AI 智能功能 |
| Lombok | 最新版 | 简化实体类 |
| Maven | 3.9+ | 构建管理 |
| JDK | 17+ | 运行环境 |

---

## 2. 项目结构

```
pet-mgt/
├── pom.xml
├── src/main/java/com/petmgt/
│   ├── PetMgtApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   ├── MvcConfig.java               # 静态资源映射
│   │   ├── MyBatisPlusConfig.java       # MyBatis-Plus 分页插件
│   │   └── FileUploadConfig.java        # 文件上传属性配置
│   ├── controller/
│   │   ├── HomeController.java          # 首页
│   │   ├── PetController.java           # 公共宠物浏览
│   │   ├── AuthController.java          # 登录/注册
│   │   ├── admin/
│   │   │   ├── AdminController.java     # 后台首页（统计）
│   │   │   ├── UserController.java      # 用户管理
│   │   │   ├── BreedController.java     # 品种管理
│   │   │   ├── PetManageController.java # 宠物管理
│   │   │   ├── ApplicationController.java # 申请审核
│   │   │   └── AiRecordController.java  # AI 记录查看
│   │   └── user/
│   │       ├── ProfileController.java   # 个人中心
│   │       ├── ApplicationController.java # 我的申请
│   │       ├── AiMatchController.java   # AI 匹配
│   │       └── AiChatController.java    # AI 问答
│   ├── service/
│   │   ├── UserService.java
│   │   ├── PetService.java
│   │   ├── BreedService.java
│   │   ├── ApplicationService.java
│   │   ├── FileStorageService.java      # 文件存储
│   │   └── ai/
│   │       ├── AiService.java           # 通用 AI 调用封装
│   │       ├── AiMatchService.java      # 宠物匹配
│   │       ├── AiReviewService.java     # 审核建议
│   │       └── AiChatService.java       # 养宠问答
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   ├── RoleMapper.java
│   │   ├── PetMapper.java
│   │   ├── BreedMapper.java
│   │   ├── PetImageMapper.java
│   │   ├── ApplicationMapper.java
│   │   ├── AiMatchRecordMapper.java
│   │   └── AiReviewRecordMapper.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Pet.java
│   │   ├── Breed.java
│   │   ├── PetImage.java
│   │   ├── Application.java
│   │   ├── AiMatchRecord.java
│   │   └── AiReviewRecord.java
│   ├── dto/
│   │   ├── RegisterForm.java
│   │   ├── ApplicationForm.java
│   │   ├── AiMatchRequest.java
│   │   ├── AiMatchResult.java
│   │   ├── AiReviewResult.java
│   │   └── PetSearchCriteria.java
│   └── util/
│       ├── FileUtil.java               # 图片上传/缩略图
│       └── SecurityUtil.java           # 获取当前登录用户
├── src/main/resources/
│   ├── application.properties
│   ├── schema.sql                       # DDL 建表
│   ├── data.sql                         # 初始数据
│   └── templates/
│       ├── fragments/
│       │   ├── header.html              # <head> 公共引用
│       │   ├── nav.html                 # 导航栏（角色自适应）
│       │   ├── footer.html              # 页脚
│       │   └── pagination.html          # 分页组件
│       ├── home.html                    # 首页
│       ├── auth/
│       │   ├── login.html               # 登录
│       │   └── register.html            # 注册
│       ├── pet/
│       │   ├── list.html                # 宠物列表
│       │   └── detail.html              # 宠物详情
│       ├── user/
│       │   ├── profile.html             # 个人中心
│       │   ├── applications.html        # 我的申请
│       │   ├── application-form.html    # 提交申请
│       │   ├── ai-match.html            # AI 匹配问卷
│       │   ├── ai-match-result.html     # AI 匹配结果
│       │   ├── ai-match-history.html    # 匹配历史
│       │   └── ai-chat.html             # AI 问答
│       └── admin/
│           ├── index.html               # 后台首页统计
│           ├── users.html               # 用户列表
│           ├── user-form.html           # 新增/编辑用户
│           ├── breeds.html              # 品种列表
│           ├── breed-form.html          # 新增/编辑品种
│           ├── pets.html                # 宠物管理列表
│           ├── pet-form.html            # 新增/编辑宠物
│           ├── applications.html        # 申请审核列表
│           ├── application-detail.html  # 申请详情+AI建议
│           └── ai-records.html          # AI 匹配记录
└── uploads/pets/                         # 图片上传目录
```

---

## 3. 数据库设计

### 3.1 表关系

```
user ──┬── user_role ──── role
       │
       ├── adoption_application ─── ai_review_record
       │         │
       │         └── pet ─── pet_image
       │                  │
       └── ai_match_record │
                           │
                      pet_breed
```

### 3.2 建表 DDL

#### user 用户表

```sql
CREATE TABLE `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username`   VARCHAR(50)  NOT NULL UNIQUE,
  `password`   VARCHAR(255) NOT NULL,
  `email`      VARCHAR(100) DEFAULT NULL,
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `enabled`    TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### role 角色表

```sql
CREATE TABLE `role` (
  `id`        BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `role_name` VARCHAR(50) NOT NULL UNIQUE
);
```

#### user_role 用户角色关联表

```sql
CREATE TABLE `user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
);
```

#### pet_breed 宠物品种表

```sql
CREATE TABLE `pet_breed` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `breed_name`  VARCHAR(100) NOT NULL,
  `pet_type`    VARCHAR(50)  NOT NULL,
  `description` TEXT         DEFAULT NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### pet 宠物信息表

```sql
CREATE TABLE `pet` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name`                  VARCHAR(100) NOT NULL,
  `breed_id`              BIGINT       NOT NULL,
  `gender`                VARCHAR(10)  NOT NULL,
  `age`                   INT          NOT NULL,
  `weight`                DECIMAL(5,2) DEFAULT NULL,
  `health_status`         VARCHAR(50)  NOT NULL,
  `vaccine_status`        VARCHAR(50)  DEFAULT NULL,
  `sterilization_status`  VARCHAR(50)  DEFAULT NULL,
  `personality`           TEXT         NOT NULL,
  `adoption_requirement`  TEXT         DEFAULT NULL,
  `status`                VARCHAR(20)  NOT NULL DEFAULT 'available',
  `created_by`            BIGINT       DEFAULT NULL,
  `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_pet_status`    (`status`),
  INDEX `idx_pet_breed_id`  (`breed_id`)
);
```

#### pet_image 宠物图片表

```sql
CREATE TABLE `pet_image` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `pet_id`     BIGINT       NOT NULL,
  `image_url`  VARCHAR(255) NOT NULL,
  `is_cover`   TINYINT      NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_image_pet_id` (`pet_id`)
);
```

#### adoption_application 领养申请表

```sql
CREATE TABLE `adoption_application` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `pet_id`          BIGINT       NOT NULL,
  `user_id`         BIGINT       NOT NULL,
  `phone`           VARCHAR(20)  NOT NULL,
  `address`         VARCHAR(255) NOT NULL,
  `experience`      TEXT         DEFAULT NULL,
  `accompany_time`  VARCHAR(50)  NOT NULL,
  `reason`          TEXT         NOT NULL,
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'pending',
  `review_comment`  TEXT         DEFAULT NULL,
  `reviewed_by`     BIGINT       DEFAULT NULL,
  `reviewed_at`     DATETIME     DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_app_status`   (`status`),
  INDEX `idx_app_user_id`  (`user_id`),
  INDEX `idx_app_pet_id`   (`pet_id`)
);
```

#### ai_match_record AI 匹配记录表

```sql
CREATE TABLE `ai_match_record` (
  `id`                 BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`            BIGINT   NOT NULL,
  `preference_text`    TEXT     DEFAULT NULL,
  `result_text`        TEXT     DEFAULT NULL,
  `recommended_pet_id` BIGINT   DEFAULT NULL,
  `match_score`        INT      DEFAULT NULL,
  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_match_user_id`  (`user_id`),
  INDEX `idx_match_time`     (`created_at`)
);
```

#### ai_review_record AI 审核建议记录表

```sql
CREATE TABLE `ai_review_record` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `application_id`  BIGINT      NOT NULL,
  `result_text`     TEXT        DEFAULT NULL,
  `score`           INT         DEFAULT NULL,
  `suggestion`      VARCHAR(50) DEFAULT NULL,
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_review_app_id` (`application_id`)
);
```

### 3.3 索引策略汇总

| 表 | 索引 | 用途 |
|----|------|------|
| user | username UNIQUE | 登录查找 |
| pet | status | 按领养状态筛选 |
| pet | breed_id | 按品种筛选 |
| adoption_application | status | 按审核状态筛选 |
| adoption_application | user_id | 用户查看自身申请 |
| adoption_application | pet_id | 防止重复申请校验 |
| ai_match_record | user_id + created_at | 用户历史记录（时间倒序） |
| pet_image | pet_id | 宠物图片关联查询 |

### 3.4 初始数据

```sql
-- 角色
INSERT INTO role (role_name) VALUES ('ROLE_USER');
INSERT INTO role (role_name) VALUES ('ROLE_ADMIN');

-- 测试用户（密码均为 123456 的 BCrypt 密文）
INSERT INTO user (username, password, email, enabled)
VALUES ('admin', '$2a$10$...', 'admin@petmgt.com', 1);
INSERT INTO user (username, password, email, enabled)
VALUES ('user', '$2a$10$...', 'user@petmgt.com', 1);

-- 用户角色关联
INSERT INTO user_role (user_id, role_id) VALUES (1, 2); -- admin = ROLE_ADMIN
INSERT INTO user_role (user_id, role_id) VALUES (2, 1); -- user  = ROLE_USER

-- 测试品种
INSERT INTO pet_breed (breed_name, pet_type) VALUES
('British Shorthair', '猫'),
('Persian', '猫'),
('Golden Retriever', '狗'),
('Corgi', '狗'),
('Holland Lop', '兔');

-- 测试宠物
INSERT INTO pet (name, breed_id, gender, age, weight, health_status,
                  vaccine_status, sterilization_status, personality,
                  adoption_requirement, status, created_by)
VALUES
('Mimi', 1, '母', 2, 3.5, '健康', '已接种', '已绝育',
 '性格温顺亲人，喜欢安静环境，适合公寓生活',
 '需要稳定的居住环境，定期提供猫粮和猫砂', 'available', 1),
('Lucky', 3, '公', 1, 12.0, '健康', '已接种', '未绝育',
 '活泼好动，喜欢户外活动，对主人忠诚',
 '需要有足够活动空间和每日遛狗时间', 'available', 1);
```

---

## 4. Spring Security 安全方案

### 4.1 权限矩阵

| 路径 | 权限 |
|------|------|
| `/`, `/pets/**`, `/login`, `/register`, `/uploads/**` | 所有人 |
| `/user/**` | ROLE_USER / ROLE_ADMIN |
| `/admin/**` | ROLE_ADMIN |

### 4.2 SecurityConfig 核心配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/pets/**", "/login", "/register",
                                 "/uploads/**", "/css/**", "/js/**", "/images/**")
                    .permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.3 用户认证加载

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getEnabled()) {
            throw new UsernameNotFoundException("用户不存在或已禁用");
        }
        List<String> roleNames = roleMapper.findRoleNamesByUserId(user.getId());
        List<GrantedAuthority> authorities = roleNames.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), user.getPassword(), authorities);
    }
}
```

### 4.4 关键 Mapper 查询

```java
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}

public interface RoleMapper extends BaseMapper<Role> {
    @Select("SELECT r.role_name FROM role r " +
            "JOIN user_role ur ON r.id = ur.role_id " +
            "JOIN user u ON ur.user_id = u.id WHERE u.id = #{userId}")
    List<String> findRoleNamesByUserId(Long userId);
}
```

---

## 5. MyBatis-Plus 配置

### 5.1 分页插件

```java
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor =
            new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setOverflow(true);  // 溢出自动修正
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
```

### 5.2 典型分页查询写法

```java
// PetService.listWithPage()
Page<Pet> page = new Page<>(pageNum, pageSize);
LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(criteria.getStatus() != null, Pet::getStatus, criteria.getStatus())
       .eq(criteria.getBreedId() != null, Pet::getBreedId, criteria.getBreedId())
       .eq(criteria.getGender() != null, Pet::getGender, criteria.getGender())
       .like(criteria.getName() != null, Pet::getName, criteria.getName())
       .orderByDesc(Pet::getCreatedAt);
Page<Pet> result = petMapper.selectPage(page, wrapper);
```

### 5.3 实体类示例

```java
@Data
@TableName("pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long breedId;
    private String gender;
    private Integer age;
    private BigDecimal weight;
    private String healthStatus;
    private String vaccineStatus;
    private String sterilizationStatus;
    private String personality;
    private String adoptionRequirement;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联非数据库字段
    @TableField(exist = false)
    private String breedName;
    @TableField(exist = false)
    private String coverImageUrl;
}
```

---

## 6. AI 模块设计

### 6.1 整体架构

```
AiService (通用封装：API Key, Endpoint, HTTP 调用, 异常处理)
    ├── AiMatchService   # 构建匹配 Prompt → 调用 AiService → 解析结果 → 保存记录
    ├── AiReviewService  # 构建审核 Prompt → 调用 AiService → 解析结果 → 保存记录
    └── AiChatService    # 构建对话 Prompt → 调用 AiService → 返回文本
```

### 6.2 AiService — 通用调用封装

```java
@Service
public class AiService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.endpoint}")
    private String endpoint;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate;

    public String chat(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                endpoint, request, Map.class);

            // 解析 choices[0].message.content
            Map<String, Object> choice = ((List<Map<String, Object>>)
                response.getBody().get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("AI API 调用失败", e);
            return null;
        }
    }
}
```

### 6.3 AiMatchService — 宠物匹配

```java
@Service
public class AiMatchService {

    private final AiService aiService;
    private final PetService petService;
    private final AiMatchRecordMapper matchRecordMapper;

    public List<AiMatchResult> match(AiMatchRequest request, Long userId) {
        // 1. 查询 status='available' 的宠物
        List<Pet> availablePets = petService.findAvailable();

        if (availablePets.isEmpty()) {
            return List.of(); // 暂无可推荐宠物
        }

        // 2. 构建 Prompt
        String systemPrompt = """
            你是一个宠物领养匹配助手。根据用户偏好和可领养宠物列表，
            分析并推荐最匹配的宠物。返回 JSON 数组格式:
            [{"petId":1,"petName":"Mimi","matchScore":92,
              "reason":"推荐理由","notes":"注意事项","suggestApply":true}]
            只推荐 matchScore >= 60 的，按分数降序排列，最多 5 个。
            """;

        String userMessage = buildMatchPrompt(request, availablePets);

        // 3. 调用 AI
        String aiResponse = aiService.chat(systemPrompt, userMessage);
        if (aiResponse == null) {
            throw new AiServiceException("AI 服务暂时不可用，请稍后再试");
        }

        // 4. 解析结果
        List<AiMatchResult> results = parseMatchResult(aiResponse);

        // 5. 保存匹配记录
        saveMatchRecord(request, aiResponse, results, userId);

        return results;
    }
}
```

**System Prompt 要点：**
- 匹配维度：宠物类型匹配 > 性格兼容 > 健康状态接受度 > 陪伴时间 > 居住环境
- 输出格式必须是 JSON 数组，方便 Java 序列化
- 要求给出具体推荐理由，不是通用模板

### 6.4 AiReviewService — 审核建议

```java
@Service
public class AiReviewService {

    public AiReviewResult review(Application application, Pet pet) {
        String systemPrompt = """
            你是一个宠物领养审核辅助助手。分析申请人信息与宠物的匹配程度。
            输出 JSON:
            {"score":78,"strengths":"...","risks":"...",
             "suggestion":"建议通过/谨慎通过/建议拒绝","notes":"..."}
            只提供参考建议，不代替管理员决策。
            """;

        String userMessage = buildReviewPrompt(application, pet);
        String aiResponse = aiService.chat(systemPrompt, userMessage);
        // 解析并保存
    }
}
```

### 6.5 AiChatService — 养宠问答

```java
@Service
public class AiChatService {

    public String chat(String userQuestion) {
        String systemPrompt = """
            你是一个宠物养护问答助手。仅回答与宠物饲养、护理、领养准备
            相关的通用建议问题。如果用户询问疾病诊断、用药、紧急情况，
            请回复：'建议咨询专业兽医，AI 无法提供医疗建议。'
            """;

        return aiService.chat(systemPrompt, userQuestion);
    }
}
```

### 6.6 AI 调用失败处理

| 场景 | 处理方式 |
|------|----------|
| API 超时（10s） | RestTemplate 超时设置，返回 null，上层提示"AI 服务暂时不可用" |
| API 返回错误 | 记录日志，返回友好提示 |
| 响应解析失败 | 记录原始响应，返回"AI 返回数据格式异常" |
| AI 不可用 | 不影响浏览宠物、提交申请、管理员审核等核心功能 |

### 6.7 application.properties 中 AI 配置

```properties
ai.deepseek.api-key=${DEEPSEEK_API_KEY}
ai.deepseek.endpoint=https://api.deepseek.com/v1/chat/completions
ai.deepseek.model=deepseek-chat
ai.timeout=10000
```

---

## 7. 文件上传方案

### 7.1 上传目录与访问配置

```java
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

### 7.2 上传属性配置

```properties
# application.properties
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=10MB
file.upload.dir=uploads/pets
file.allowed-extensions=jpg,jpeg,png
```

### 7.3 FileStorageService 实现要点

```java
@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    public String store(MultipartFile file) {
        // 1. 校验扩展名
        // 2. 校验文件大小 ≤ 2MB
        // 3. 生成 UUID 文件名: uuid.ext
        // 4. 保存到 uploads/pets/
        return savedFileName;
    }

    public String createThumbnail(String originalFileName) {
        // 使用 Java BufferedImage + ImageIO
        // 缩放到 300x300，命名: thumb_uuid.ext
        return thumbnailFileName;
    }

    public void delete(String fileName) {
        // 删除原图和缩略图
    }
}
```

### 7.4 图片展示策略

| 场景 | 使用的图片 | 尺寸 |
|------|-----------|------|
| 宠物列表卡片 | is_cover=1 的缩略图 | 300×300 |
| 宠物详情页 | 所有图片缩略图 + 点击放大原图 | 300×300 / 原尺寸 |
| 申请记录 | 关联宠物的封面缩略图 | 150×150 |
| 用户头像 | 原图或固定尺寸 | 100×100 |

---

## 8. 核心业务流程实现

### 8.1 用户注册

```
POST /register
  → 校验: username 非空、不重复、password 非空、两次密码一致、email 格式正确
  → BCrypt 加密密码
  → 插入 user 表（enabled=1）
  → 插入 user_role 关联 ROLE_USER
  → 重定向到 /login?registered
```

### 8.2 提交领养申请

```java
@Transactional
public void submitApplication(ApplicationForm form, Long userId) {
    Pet pet = petMapper.selectById(form.getPetId());

    // 1. 校验宠物状态为 available
    if (!"available".equals(pet.getStatus())) {
        throw new BusinessException("该宠物当前不可申请");
    }

    // 2. 校验无重复 pending 申请（同一用户+同一宠物）
    Long count = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
        .eq(Application::getUserId, userId)
        .eq(Application::getPetId, form.getPetId())
        .eq(Application::getStatus, "pending"));
    if (count > 0) {
        throw new BusinessException("您已提交过该宠物的领养申请，请勿重复申请");
    }

    // 3. 写入申请
    Application app = new Application();
    BeanUtils.copyProperties(form, app);
    app.setUserId(userId);
    app.setStatus("pending");
    applicationMapper.insert(app);

    // 4. 更新宠物状态为 pending
    pet.setStatus("pending");
    petMapper.updateById(pet);
}
```

### 8.3 审核通过

```java
@Transactional
public void approve(Long applicationId, Long adminId, String comment) {
    Application app = applicationMapper.selectById(applicationId);

    // 1. 当前申请 → approved
    app.setStatus("approved");
    app.setReviewedBy(adminId);
    app.setReviewedAt(LocalDateTime.now());
    app.setReviewComment(comment);
    applicationMapper.updateById(app);

    // 2. 宠物 → adopted
    Pet pet = petMapper.selectById(app.getPetId());
    pet.setStatus("adopted");
    petMapper.updateById(pet);

    // 3. 该宠物其他 pending 申请 → 批量拒绝
    applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
        .eq(Application::getPetId, app.getPetId())
        .eq(Application::getStatus, "pending")
        .ne(Application::getId, applicationId)
        .set(Application::getStatus, "rejected")
        .set(Application::getReviewComment, "该宠物已被领养，系统自动拒绝")
        .set(Application::getReviewedBy, adminId)
        .set(Application::getReviewedAt, LocalDateTime.now()));
}
```

### 8.4 审核拒绝

```java
@Transactional
public void reject(Long applicationId, Long adminId, String reason) {
    Application app = applicationMapper.selectById(applicationId);
    app.setStatus("rejected");
    app.setReviewedBy(adminId);
    app.setReviewedAt(LocalDateTime.now());
    app.setReviewComment(reason);
    applicationMapper.updateById(app);

    // 宠物状态恢复为 available（若无其他 pending 申请）
    Long pendingCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
        .eq(Application::getPetId, app.getPetId())
        .eq(Application::getStatus, "pending"));
    if (pendingCount == 0) {
        Pet pet = petMapper.selectById(app.getPetId());
        pet.setStatus("available");
        petMapper.updateById(pet);
    }
}
```

### 8.5 用户取消申请

```java
// 用户只能取消自己 pending 状态的申请
@Transactional
public void cancel(Long applicationId, Long userId) {
    Application app = applicationMapper.selectById(applicationId);
    if (!app.getUserId().equals(userId)) {
        throw new BusinessException("无权操作");
    }
    if (!"pending".equals(app.getStatus())) {
        throw new BusinessException("只能取消待审核状态的申请");
    }
    app.setStatus("cancelled");
    applicationMapper.updateById(app);

    // 宠物状态恢复
    Long pendingCount = applicationMapper.selectCount(new LambdaQueryWrapper<Application>()
        .eq(Application::getPetId, app.getPetId())
        .eq(Application::getStatus, "pending"));
    if (pendingCount == 0) {
        Pet pet = petMapper.selectById(app.getPetId());
        pet.setStatus("available");
        petMapper.updateById(pet);
    }
}
```

---

## 9. 页面与前端设计

### 9.1 Thymeleaf 布局复用

**fragments/header.html** — 所有页面的 `<head>` 公共部分：

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:fragment="head(title)">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title} + ' - pet-mgt'">pet-mgt</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
```

**fragments/nav.html** — 导航栏根据角色自适应：

```html
<nav th:fragment="nav" class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" href="/">pet-mgt</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="/pets">宠物列表</a>
                </li>
                <!-- 已登录 -->
                <th:block sec:authorize="isAuthenticated()">
                    <li class="nav-item">
                        <a class="nav-link" href="/user/ai-match">AI 匹配</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/user/applications">我的申请</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/user/ai-chat">养宠问答</a>
                    </li>
                </th:block>
                <!-- 管理员 -->
                <th:block sec:authorize="hasRole('ADMIN')">
                    <li class="nav-item">
                        <a class="nav-link" href="/admin">后台管理</a>
                    </li>
                </th:block>
            </ul>
            <ul class="navbar-nav">
                <th:block sec:authorize="!isAuthenticated()">
                    <li class="nav-item">
                        <a class="nav-link" href="/login">登录</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/register">注册</a>
                    </li>
                </th:block>
                <th:block sec:authorize="isAuthenticated()">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#"
                           data-bs-toggle="dropdown">
                            <span sec:authentication="name">用户名</span>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="/user/profile">个人中心</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li>
                                <form th:action="@{/logout}" method="post">
                                    <button class="dropdown-item" type="submit">退出登录</button>
                                </form>
                            </li>
                        </ul>
                    </li>
                </th:block>
            </ul>
        </div>
    </div>
</nav>
```

### 9.2 响应式断点

| 屏幕尺寸 | 宽度 | 宠物列表列数 | 导航栏 |
|----------|------|:----------:|--------|
| 手机 | < 768px | 1 列 | 折叠 |
| 平板 | 768-992px | 2 列 | 折叠/展开 |
| 桌面 | ≥ 992px | 3-4 列 | 完全展开 |

宠物列表栅格：

```html
<div class="row">
    <div class="col-12 col-md-6 col-lg-4 col-xl-3"
         th:each="pet : ${page.records}">
        <div class="card h-100">
            <img th:src="${pet.coverImageUrl}" class="card-img-top"
                 th:alt="${pet.name}" style="height:200px;object-fit:cover;">
            <div class="card-body">
                <h5 class="card-title" th:text="${pet.name}">宠物名</h5>
                <p class="card-text">
                    <span class="badge bg-info" th:text="${pet.breedName}">品种</span>
                    <span class="badge" th:classappend="..."
                          th:text="${pet.status}">状态</span>
                </p>
            </div>
            <div class="card-footer">
                <a th:href="@{/pets/{id}(id=${pet.id})}"
                   class="btn btn-primary btn-sm w-100">查看详情</a>
            </div>
        </div>
    </div>
</div>
```

### 9.3 分页组件

**fragments/pagination.html** — 所有列表页复用：

```html
<nav th:fragment="pagination(page, baseUrl)" th:if="${page.pages > 1}">
    <ul class="pagination justify-content-center">
        <li class="page-item" th:classappend="${!page.hasPrevious} ? 'disabled'">
            <a class="page-link" th:href="@{${baseUrl}(page=${page.current-1})}">上一页</a>
        </li>
        <li class="page-item" th:each="i : ${#numbers.sequence(1, page.pages)}"
            th:classappend="${i == page.current} ? 'active'">
            <a class="page-link" th:href="@{${baseUrl}(page=${i})}" th:text="${i}">1</a>
        </li>
        <li class="page-item" th:classappend="${!page.hasNext} ? 'disabled'">
            <a class="page-link" th:href="@{${baseUrl}(page=${page.current+1})}">下一页</a>
        </li>
    </ul>
</nav>
```

### 9.4 状态标签样式

```html
<th:block th:switch="${app.status}">
    <span th:case="'pending'"   class="badge bg-warning text-dark">待审核</span>
    <span th:case="'approved'"  class="badge bg-success">已通过</span>
    <span th:case="'rejected'"  class="badge bg-danger">已拒绝</span>
    <span th:case="'cancelled'" class="badge bg-secondary">已取消</span>
</th:block>
```

### 9.5 JavaScript 功能点

| 功能 | 实现 |
|------|------|
| 密码一致性校验 | JS 监听 input 事件，实时比较 |
| 图片上传预览 | FileReader API 读取为 data URL 显示缩略图 |
| 删除确认 | Bootstrap Modal 弹窗，确认后提交表单 |
| AI 问答 | Fetch API POST → JSON → 页面更新 |
| 品种联动 | 宠物类型 change → fetch 加载对应品种下拉 |
| 表单验证 | HTML5 required/pattern 属性 + JS 补充校验 |

### 9.6 后台首页统计卡片

使用 Bootstrap 5 Grid + Card：

```html
<div class="row">
    <div class="col-md-4 col-lg-2">
        <div class="card text-white bg-primary">
            <div class="card-body text-center">
                <h3 th:text="${stats.totalUsers}">0</h3>
                <p class="mb-0">用户总数</p>
            </div>
        </div>
    </div>
    <div class="col-md-4 col-lg-2">
        <div class="card text-white bg-success">
            <div class="card-body text-center">
                <h3 th:text="${stats.totalPets}">0</h3>
                <p class="mb-0">宠物总数</p>
            </div>
        </div>
    </div>
    <!-- 可领养数、已领养数、待审核数 同理 -->
</div>
```

---

## 10. 配置文件汇总

### 10.1 application.properties

```properties
# 数据源
spring.datasource.url=jdbc:mysql://localhost:3306/pet_mgt?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# SQL 初始化
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

# MyBatis-Plus
mybatis-plus.mapper-locations=classpath:mapper/**/*.xml
mybatis-plus.configuration.map-underscore-to-camel-case=true

# 文件上传
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=10MB
file.upload.dir=uploads/pets
file.allowed-extensions=jpg,jpeg,png

# AI
ai.deepseek.api-key=${DEEPSEEK_API_KEY}
ai.deepseek.endpoint=https://api.deepseek.com/v1/chat/completions
ai.deepseek.model=deepseek-chat
ai.timeout=10000

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```

### 10.2 pom.xml 关键依赖

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version>
</parent>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.9</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Thymeleaf Spring Security 扩展 -->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 11. 实施顺序建议

| 阶段 | 内容 | 产出 |
|------|------|------|
| **P0 基础框架** | 项目初始化、数据库建表、Spring Security、基础页面布局 | 可登录、有导航、有角色权限 |
| **P1 宠物浏览** | 宠物列表/详情、分页、筛选、图片展示 | 未登录用户可浏览宠物 |
| **P2 用户功能** | 注册、申请提交、我的申请、取消申请 | 完整用户侧主流程 |
| **P3 管理功能** | 品种管理、宠物管理、图片上传、申请审核 | 完整管理员侧功能 |
| **P4 AI 模块** | AiService 封装、匹配助手、审核建议、文案生成、养宠问答 | AI 功能完整可用 |
| **P5 收尾** | 统计首页、测试数据完善、错误页面、全局异常处理 | 系统完整 |

---

## 12. 异常与错误处理

### 12.1 全局异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException e, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", e.getMessage());
        return "redirect:" + e.getRedirectUrl();
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model) {
        log.error("系统异常", e);
        model.addAttribute("message", "系统繁忙，请稍后再试");
        return "error";
    }
}
```

### 12.2 常见异常场景

| 异常 | HTTP 状态 | 页面行为 |
|------|-----------|----------|
| 未登录访问 /user/** | 302 | 跳转登录页 |
| 普通用户访问 /admin/** | 403 | 显示 403 禁止访问 |
| 重复提交申请 | 200 | 返回表单页 + 错误提示 |
| AI 调用失败 | 200 | 当前页 + 警告提示"AI 暂不可用" |
| 文件格式不合法 | 200 | 返回表单页 + 格式错误提示 |
| 资源不存在 | 404 | 自定义 404 页面 |

---

## 13. 安全注意事项

1. **密码加密：** 注册和修改密码时必须 BCrypt 加密，明文不入库
2. **CSRF 防护：** Thymeleaf 表单自动携带 `_csrf` Token
3. **文件上传：** 仅允许 jpg/jpeg/png，校验文件魔术字节（非仅扩展名）
4. **路径遍历：** 上传文件名使用 UUID，丢弃原始文件名
5. **权限校验：** Controller 层加 `@PreAuthorize` 或 URL 配置，Service 层校验数据归属（用户只能操作自己的数据）
6. **AI API Key：** 通过环境变量传入，不写入配置文件或代码仓库
7. **SQL 注入：** MyBatis-Plus 参数化查询防御，避免 `${}` 拼接

---

## 14. 非功能需求落地

| 非功能需求 | 实现方式 |
|-----------|----------|
| 响应式 | Bootstrap 5 栅格断点 col-12 → col-md-6 → col-lg-4 |
| 分页 | MyBatis-Plus Page + 自定义分页 Thymeleaf fragment |
| 缩略图 | 上传时生成 300×300 缩略图，列表/卡片使用缩略图 |
| AI 降级 | AiService.chat() 异常返回 null，上层展示友好提示 |
| 删除确认 | Bootstrap Modal + JS 确认后提交 |
| 表单校验 | HTML5 + JS 双重校验 |
| 状态颜色 | Bootstrap badge bg-warning/success/danger/secondary |
| 缩略图放大 | CSS Modal 或新窗口打开原图 |

---

## 15. 需求覆盖检查

| 需求章节 | 需求内容 | 设计对应 |
|----------|----------|----------|
| 5.1 用户认证与权限 | 登录/注册/退出/权限控制 | 第 4 节 SecurityConfig |
| 6.1 浏览宠物列表 | 卡片展示、分页、筛选 | 第 5、9 节 MyBatis-Plus + Thymeleaf |
| 6.2 宠物详情 | 详情页、图片放大、申请按钮 | 第 9 节 pet/detail.html |
| 6.3 提交领养申请 | 表单、校验、状态联动 | 第 8.2 节 |
| 6.4 我的申请 | 列表、状态标签、取消 | 第 8.5 节 + 状态标签 |
| 7.1-7.5 管理员功能 | 统计/用户/品种/宠物/审核 | 第 8.3-8.4 节 + 页面清单 |
| 8.1-8.5 AI 功能 | 匹配/历史/介绍/审核/问答 | 第 6 节 AI 模块 |
| 9 页面需求 | 完整页面路由 | 第 2 节项目结构 templates/ |
| 10 数据需求 | 9 张表 | 第 3 节 DDL |
| 12 非功能需求 | 安全/易用/响应式/性能/可维护 | 第 13、14 节 |
