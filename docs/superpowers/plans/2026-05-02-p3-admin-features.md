# P3 — 管理功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现完整管理员侧功能：统计仪表盘、用户管理、品种管理、宠物管理（含图片上传）、申请审核。

**Architecture:** 新增 `controller/admin/` 包下 5 个 Controller，新增 `BreedService`、`FileStorageService`，新增 `FileUploadConfig` 和 `FileUtil`，扩展 `ApplicationService` 加入审核逻辑。模板全部放在 `templates/admin/` 下。

**Tech Stack:** Spring Boot 3.5.6, MyBatis-Plus 3.5.9, Thymeleaf, Bootstrap 5.3 CDN, Java BufferedImage 缩略图

**Prerequisite:** P0/P1/P2 已完成 — 所有实体、Mapper、基础页面均已就绪。

---

### Task 1: FileUploadConfig — 文件上传属性配置

**Files:**
- Create: `src/main/java/com/petmgt/config/FileUploadConfig.java`

- [ ] **Step 1: Create FileUploadConfig**

```java
package com.petmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {

    private String uploadDir = "uploads/pets";
    private String allowedExtensions = "jpg,jpeg,png";

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    public String getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(String allowedExtensions) { this.allowedExtensions = allowedExtensions; }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 2: FileUtil — 图片工具类

**Files:**
- Create: `src/main/java/com/petmgt/util/FileUtil.java`

- [ ] **Step 1: Create FileUtil with thumbnail generation**

```java
package com.petmgt.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileUtil {

    private static final int THUMB_WIDTH = 300;
    private static final int THUMB_HEIGHT = 300;

    public static String createThumbnail(String originalPath) throws IOException {
        File originalFile = new File(originalPath);
        BufferedImage originalImage = ImageIO.read(originalFile);
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }

        BufferedImage thumbnail = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, THUMB_WIDTH, THUMB_HEIGHT, null);
        g2d.dispose();

        String thumbPath = originalFile.getParent() + File.separator + "thumb_" + originalFile.getName();
        ImageIO.write(thumbnail, "jpg", new File(thumbPath));
        return "thumb_" + originalFile.getName();
    }
}
```

---

### Task 3: FileStorageService — 文件存储服务

**Files:**
- Create: `src/main/java/com/petmgt/service/FileStorageService.java`

- [ ] **Step 1: Create FileStorageService**

```java
package com.petmgt.service;

import com.petmgt.config.FileUploadConfig;
import com.petmgt.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final FileUploadConfig config;

    public FileStorageService(FileUploadConfig config) {
        this.config = config;
    }

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getExtension(originalFilename).toLowerCase();
        List<String> allowed = Arrays.asList(config.getAllowedExtensions().split(","));
        if (!allowed.contains(extension)) {
            throw new IllegalArgumentException("仅支持以下格式: " + config.getAllowedExtensions());
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 2MB");
        }

        String newFileName = UUID.randomUUID().toString() + "." + extension;
        File uploadDir = new File(config.getUploadDir());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File dest = new File(uploadDir, newFileName);
        file.transferTo(dest);

        try {
            FileUtil.createThumbnail(dest.getAbsolutePath());
        } catch (IOException e) {
            // 缩略图失败不阻塞主流程
        }

        return newFileName;
    }

    public void delete(String fileName) {
        if (fileName == null || fileName.isEmpty()) return;
        File dir = new File(config.getUploadDir());
        File file = new File(dir, fileName);
        if (file.exists()) file.delete();
        File thumb = new File(dir, "thumb_" + fileName);
        if (thumb.exists()) thumb.delete();
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? "" : filename.substring(dotIndex + 1);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 4: BreedService — 品种业务逻辑

**Files:**
- Create: `src/main/java/com/petmgt/service/BreedService.java`

- [ ] **Step 1: Create BreedService**

```java
package com.petmgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetMapper;
import org.springframework.stereotype.Service;

@Service
public class BreedService {

    private final BreedMapper breedMapper;
    private final PetMapper petMapper;

    public BreedService(BreedMapper breedMapper, PetMapper petMapper) {
        this.breedMapper = breedMapper;
        this.petMapper = petMapper;
    }

    public Page<Breed> list(int pageNum, int pageSize, String petType) {
        Page<Breed> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Breed> wrapper = new LambdaQueryWrapper<>();
        if (petType != null && !petType.isEmpty()) {
            wrapper.eq(Breed::getPetType, petType);
        }
        wrapper.orderByDesc(Breed::getCreatedAt);
        return breedMapper.selectPage(page, wrapper);
    }

    public void save(Breed breed) {
        breedMapper.insert(breed);
    }

    public void update(Breed breed) {
        breedMapper.updateById(breed);
    }

    public void delete(Long id) {
        Long petCount = petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getBreedId, id));
        if (petCount > 0) {
            throw new IllegalArgumentException("该品种下还有 " + petCount + " 只宠物，无法删除");
        }
        breedMapper.deleteById(id);
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 5: AdminController — 后台首页统计

**Files:**
- Create: `src/main/java/com/petmgt/controller/admin/AdminController.java`
- Create: `src/main/resources/templates/admin/index.html`

- [ ] **Step 1: Create AdminController**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final ApplicationMapper applicationMapper;

    public AdminController(UserMapper userMapper, PetMapper petMapper,
                           ApplicationMapper applicationMapper) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "后台管理");
        model.addAttribute("totalUsers", userMapper.selectCount(null));
        model.addAttribute("totalPets", petMapper.selectCount(null));
        model.addAttribute("availablePets", petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "available")));
        model.addAttribute("adoptedPets", petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "adopted")));
        model.addAttribute("pendingApps", applicationMapper.selectCount(
            new LambdaQueryWrapper<Application>().eq(Application::getStatus, "pending")));
        return "admin/index";
    }
}
```

- [ ] **Step 2: Create admin/index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <h1 class="mb-4">后台管理</h1>
    <div class="row g-3">
        <div class="col-6 col-md-4 col-lg-2">
            <div class="card text-white bg-primary">
                <div class="card-body text-center">
                    <h3 th:text="${totalUsers}">0</h3>
                    <p class="mb-0">用户总数</p>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-4 col-lg-2">
            <div class="card text-white bg-success">
                <div class="card-body text-center">
                    <h3 th:text="${totalPets}">0</h3>
                    <p class="mb-0">宠物总数</p>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-4 col-lg-2">
            <div class="card text-white bg-info">
                <div class="card-body text-center">
                    <h3 th:text="${availablePets}">0</h3>
                    <p class="mb-0">可领养</p>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-4 col-lg-2">
            <div class="card text-white bg-secondary">
                <div class="card-body text-center">
                    <h3 th:text="${adoptedPets}">0</h3>
                    <p class="mb-0">已领养</p>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-4 col-lg-2">
            <div class="card text-white bg-warning">
                <div class="card-body text-center">
                    <h3 th:text="${pendingApps}">0</h3>
                    <p class="mb-0">待审核</p>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-4 g-3">
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">用户管理</h5>
                    <p class="card-text">管理用户账号、角色分配</p>
                    <a href="/admin/users" class="btn btn-primary">进入</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">品种管理</h5>
                    <p class="card-text">管理宠物品种信息</p>
                    <a href="/admin/breeds" class="btn btn-primary">进入</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">宠物管理</h5>
                    <p class="card-text">发布宠物、管理图片</p>
                    <a href="/admin/pets" class="btn btn-primary">进入</a>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">申请审核</h5>
                    <p class="card-text">审核用户领养申请</p>
                    <a href="/admin/applications" class="btn btn-primary">进入</a>
                </div>
            </div>
        </div>
    </div>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 3: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 6: Admin UserController — 用户管理

**Files:**
- Create: `src/main/java/com/petmgt/controller/admin/UserController.java`
- Create: `src/main/resources/templates/admin/users.html`
- Create: `src/main/resources/templates/admin/user-form.html`

- [ ] **Step 1: Create admin UserController**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Role;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, RoleMapper roleMapper,
                          PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(userPage, wrapper);

        java.util.Map<Long, java.util.List<String>> roleNames = new java.util.LinkedHashMap<>();
        for (User user : result.getRecords()) {
            user.setPassword(null);
            roleNames.put(user.getId(), roleMapper.findRoleNamesByUserId(user.getId()));
        }

        model.addAttribute("title", "用户管理");
        model.addAttribute("page", result);
        model.addAttribute("roleNames", roleNames);
        return "admin/users";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("title", "新增用户");
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleMapper.selectList(null));
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @PostMapping("/create")
    public String create(User user, @RequestParam(required = false) List<Long> roleIds,
                         RedirectAttributes redirectAttributes) {
        try {
            if (userMapper.findByUsername(user.getUsername()) != null) {
                redirectAttributes.addFlashAttribute("error", "用户名已存在");
                return "redirect:/admin/users/create";
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setEnabled(user.getEnabled() != null ? user.getEnabled() : 1);
            userMapper.insert(user);
            if (roleIds != null) {
                for (Long roleId : roleIds) {
                    roleMapper.insertUserRole(user.getId(), roleId);
                }
            }
            redirectAttributes.addFlashAttribute("success", "用户创建成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "创建失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        user.setPassword(null);
        List<String> currentRoles = roleMapper.findRoleNamesByUserId(id);
        model.addAttribute("title", "编辑用户");
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleMapper.selectList(null));
        model.addAttribute("currentRoleNames", currentRoles);
        model.addAttribute("isEdit", true);
        return "admin/user-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, User user,
                       @RequestParam(required = false) List<Long> roleIds,
                       RedirectAttributes redirectAttributes) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser != null && currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "不能编辑自己的账号");
                return "redirect:/admin/users/" + id + "/edit";
            }
            user.setId(id);
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(null);
            }
            userMapper.updateById(user);
            redirectAttributes.addFlashAttribute("success", "用户更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "不能删除自己的账号");
            return "redirect:/admin/users";
        }
        userMapper.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "用户已删除");
        return "redirect:/admin/users";
    }
}
```

- [ ] **Step 2: Create admin/users.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>用户管理</h1>
        <a href="/admin/users/create" class="btn btn-primary">新增用户</a>
    </div>

    <div th:if="${success}" class="alert alert-success alert-dismissible fade show" role="alert">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${error}" class="alert alert-danger alert-dismissible fade show" role="alert">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <div class="table-responsive">
        <table class="table table-striped table-hover">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>邮箱</th>
                    <th>角色</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="user : ${page.records}">
                    <td th:text="${user.id}">1</td>
                    <td th:text="${user.username}">admin</td>
                    <td th:text="${user.email}">-</td>
                    <td>
                        <th:block th:each="roleName : ${roleNames.get(user.id)}">
                            <span th:if="${roleName == 'ROLE_ADMIN'}" class="badge bg-danger">管理员</span>
                            <span th:if="${roleName == 'ROLE_USER'}" class="badge bg-primary">用户</span>
                        </th:block>
                    </td>
                    <td>
                        <span th:if="${user.enabled == 1}" class="badge bg-success">启用</span>
                        <span th:unless="${user.enabled == 1}" class="badge bg-secondary">禁用</span>
                    </td>
                    <td th:text="${#temporals.format(user.createdAt, 'yyyy-MM-dd HH:mm')}">-</td>
                    <td>
                        <a th:href="@{/admin/users/{id}/edit(id=${user.id})}" class="btn btn-sm btn-outline-primary">编辑</a>
                        <button class="btn btn-sm btn-outline-danger" data-bs-toggle="modal"
                                th:attr="data-bs-target='#deleteModal' + ${user.id}">删除</button>

                        <div class="modal fade" th:id="'deleteModal' + ${user.id}" tabindex="-1">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">确认删除</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body">
                                        确定要删除用户 <strong th:text="${user.username}"></strong> 吗？此操作不可撤销。
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                        <form th:action="@{/admin/users/{id}/delete(id=${user.id})}" method="post" class="d-inline">
                                            <button type="submit" class="btn btn-danger">确认删除</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div th:replace="~{fragments/pagination :: pagination(page=${page}, baseUrl='/admin/users')}"></div>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 3: Create admin/user-form.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <h1 th:text="${isEdit} ? '编辑用户' : '新增用户'"></h1>

    <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>

    <form th:action="${isEdit} ? @{/admin/users/{id}/edit(id=${user.id})} : @{/admin/users/create}"
          method="post" class="mt-3" style="max-width:600px;">
        <div class="mb-3">
            <label class="form-label">用户名</label>
            <input type="text" name="username" class="form-control"
                   th:value="${user.username}" required minlength="3">
        </div>
        <div class="mb-3">
            <label class="form-label">
                密码 <small th:if="${isEdit}" class="text-muted">（留空则不修改）</small>
            </label>
            <input type="password" name="password" class="form-control"
                   th:required="${!isEdit}" minlength="6">
        </div>
        <div class="mb-3">
            <label class="form-label">邮箱</label>
            <input type="email" name="email" class="form-control" th:value="${user.email}">
        </div>
        <div class="mb-3">
            <label class="form-label">头像 URL</label>
            <input type="text" name="avatarUrl" class="form-control" th:value="${user.avatarUrl}">
        </div>
        <div class="mb-3">
            <label class="form-label">角色</label>
            <div th:each="role : ${allRoles}">
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox" name="roleIds"
                           th:value="${role.id}" th:id="'role_' + ${role.id}">
                    <label class="form-check-label" th:for="'role_' + ${role.id}"
                           th:text="${role.roleName == 'ROLE_ADMIN' ? '管理员' : '普通用户'}"></label>
                </div>
            </div>
        </div>
        <div class="mb-3">
            <label class="form-label">状态</label>
            <select name="enabled" class="form-select">
                <option value="1" th:selected="${user.enabled == null || user.enabled == 1}">启用</option>
                <option value="0" th:selected="${user.enabled != null && user.enabled == 0}">禁用</option>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">保存</button>
        <a href="/admin/users" class="btn btn-secondary">取消</a>
    </form>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 4: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 7: Admin BreedController — 品种管理

**Files:**
- Create: `src/main/java/com/petmgt/controller/admin/BreedController.java`
- Create: `src/main/resources/templates/admin/breeds.html`
- Create: `src/main/resources/templates/admin/breed-form.html`

- [ ] **Step 1: Create admin BreedController**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.BreedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/breeds")
public class BreedController {

    private final BreedService breedService;
    private final BreedMapper breedMapper;

    public BreedController(BreedService breedService, BreedMapper breedMapper) {
        this.breedService = breedService;
        this.breedMapper = breedMapper;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String petType,
                       Model model) {
        Page<Breed> result = breedService.list(page, size, petType);
        model.addAttribute("title", "品种管理");
        model.addAttribute("page", result);
        model.addAttribute("petType", petType);
        return "admin/breeds";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("title", "新增品种");
        model.addAttribute("breed", new Breed());
        model.addAttribute("isEdit", false);
        return "admin/breed-form";
    }

    @PostMapping("/create")
    public String create(Breed breed, RedirectAttributes redirectAttributes) {
        breedService.save(breed);
        redirectAttributes.addFlashAttribute("success", "品种创建成功");
        return "redirect:/admin/breeds";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Breed breed = breedMapper.selectById(id);
        if (breed == null) {
            return "redirect:/admin/breeds";
        }
        model.addAttribute("title", "编辑品种");
        model.addAttribute("breed", breed);
        model.addAttribute("isEdit", true);
        return "admin/breed-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Breed breed, RedirectAttributes redirectAttributes) {
        breed.setId(id);
        breedService.update(breed);
        redirectAttributes.addFlashAttribute("success", "品种更新成功");
        return "redirect:/admin/breeds";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            breedService.delete(id);
            redirectAttributes.addFlashAttribute("success", "品种已删除");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/breeds";
    }
}
```

- [ ] **Step 2: Create admin/breeds.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>品种管理</h1>
        <a href="/admin/breeds/create" class="btn btn-primary">新增品种</a>
    </div>

    <div th:if="${success}" class="alert alert-success alert-dismissible fade show" role="alert">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${error}" class="alert alert-danger alert-dismissible fade show" role="alert">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <form class="row g-2 mb-3" method="get">
        <div class="col-auto">
            <select name="petType" class="form-select">
                <option value="">全部类型</option>
                <option value="猫" th:selected="${petType == '猫'}">猫</option>
                <option value="狗" th:selected="${petType == '狗'}">狗</option>
                <option value="兔" th:selected="${petType == '兔'}">兔</option>
            </select>
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-primary">筛选</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-hover">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>品种名称</th>
                    <th>宠物类型</th>
                    <th>描述</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="breed : ${page.records}">
                    <td th:text="${breed.id}">1</td>
                    <td th:text="${breed.breedName}">品种名</td>
                    <td><span class="badge bg-info" th:text="${breed.petType}"></span></td>
                    <td th:text="${breed.description}">-</td>
                    <td th:text="${#temporals.format(breed.createdAt, 'yyyy-MM-dd HH:mm')}">-</td>
                    <td>
                        <a th:href="@{/admin/breeds/{id}/edit(id=${breed.id})}" class="btn btn-sm btn-outline-primary">编辑</a>
                        <button class="btn btn-sm btn-outline-danger" data-bs-toggle="modal"
                                th:attr="data-bs-target='#deleteModal' + ${breed.id}">删除</button>

                        <div class="modal fade" th:id="'deleteModal' + ${breed.id}" tabindex="-1">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">确认删除</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body">
                                        确定要删除品种 <strong th:text="${breed.breedName}"></strong> 吗？
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                        <form th:action="@{/admin/breeds/{id}/delete(id=${breed.id})}" method="post" class="d-inline">
                                            <button type="submit" class="btn btn-danger">确认删除</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div th:replace="~{fragments/pagination :: pagination(page=${page}, baseUrl='/admin/breeds')}"></div>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 3: Create admin/breed-form.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <h1 th:text="${isEdit} ? '编辑品种' : '新增品种'"></h1>

    <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>

    <form th:action="${isEdit} ? @{/admin/breeds/{id}/edit(id=${breed.id})} : @{/admin/breeds/create}"
          method="post" class="mt-3" style="max-width:600px;">
        <div class="mb-3">
            <label class="form-label">品种名称</label>
            <input type="text" name="breedName" class="form-control" th:value="${breed.breedName}" required>
        </div>
        <div class="mb-3">
            <label class="form-label">宠物类型</label>
            <select name="petType" class="form-select" required>
                <option value="">请选择</option>
                <option value="猫" th:selected="${breed.petType == '猫'}">猫</option>
                <option value="狗" th:selected="${breed.petType == '狗'}">狗</option>
                <option value="兔" th:selected="${breed.petType == '兔'}">兔</option>
            </select>
        </div>
        <div class="mb-3">
            <label class="form-label">描述</label>
            <textarea name="description" class="form-control" rows="3" th:text="${breed.description}"></textarea>
        </div>
        <button type="submit" class="btn btn-primary">保存</button>
        <a href="/admin/breeds" class="btn btn-secondary">取消</a>
    </form>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 4: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 8: Admin PetManageController — 宠物管理（含图片上传）

**Files:**
- Create: `src/main/java/com/petmgt/controller/admin/PetManageController.java`
- Create: `src/main/resources/templates/admin/pets.html`
- Create: `src/main/resources/templates/admin/pet-form.html`

- [ ] **Step 1: Create admin PetManageController**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.service.FileStorageService;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/pets")
public class PetManageController {

    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final FileStorageService fileStorageService;

    public PetManageController(PetMapper petMapper, BreedMapper breedMapper,
                               PetImageMapper petImageMapper,
                               FileStorageService fileStorageService) {
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) Long breedId,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<Pet> petPage = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Pet::getName, name);
        }
        if (breedId != null) {
            wrapper.eq(Pet::getBreedId, breedId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Pet::getStatus, status);
        }
        wrapper.orderByDesc(Pet::getCreatedAt);
        Page<Pet> result = petMapper.selectPage(petPage, wrapper);

        for (Pet pet : result.getRecords()) {
            Breed breed = breedMapper.selectById(pet.getBreedId());
            if (breed != null) {
                pet.setBreedName(breed.getBreedName());
            }
        }

        model.addAttribute("title", "宠物管理");
        model.addAttribute("page", result);
        model.addAttribute("breeds", breedMapper.selectList(null));
        model.addAttribute("name", name);
        model.addAttribute("breedId", breedId);
        model.addAttribute("status", status);
        return "admin/pets";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("title", "发布宠物");
        model.addAttribute("pet", new Pet());
        model.addAttribute("breeds", breedMapper.selectList(null));
        model.addAttribute("isEdit", false);
        return "admin/pet-form";
    }

    @PostMapping("/create")
    public String create(Pet pet,
                         @RequestParam(required = false) List<MultipartFile> images,
                         @RequestParam(required = false) Integer coverIndex,
                         RedirectAttributes redirectAttributes) {
        try {
            pet.setStatus("available");
            pet.setCreatedBy(SecurityUtil.getCurrentUser().getId());
            petMapper.insert(pet);

            if (images != null) {
                saveImages(pet.getId(), images, coverIndex);
            }
            redirectAttributes.addFlashAttribute("success", "宠物发布成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "发布失败: " + e.getMessage());
        }
        return "redirect:/admin/pets";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return "redirect:/admin/pets";
        }
        List<PetImage> images = petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        model.addAttribute("title", "编辑宠物");
        model.addAttribute("pet", pet);
        model.addAttribute("breeds", breedMapper.selectList(null));
        model.addAttribute("images", images);
        model.addAttribute("isEdit", true);
        return "admin/pet-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Pet pet,
                       @RequestParam(required = false) List<MultipartFile> newImages,
                       @RequestParam(required = false) Integer coverIndex,
                       @RequestParam(required = false) List<Long> deleteImageIds,
                       RedirectAttributes redirectAttributes) {
        try {
            pet.setId(id);
            petMapper.updateById(pet);

            if (deleteImageIds != null) {
                for (Long imageId : deleteImageIds) {
                    PetImage img = petImageMapper.selectById(imageId);
                    if (img != null) {
                        fileStorageService.delete(img.getImageUrl());
                        petImageMapper.deleteById(imageId);
                    }
                }
            }

            if (newImages != null && !newImages.isEmpty()) {
                saveImages(id, newImages, coverIndex);
            }
            redirectAttributes.addFlashAttribute("success", "宠物更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/admin/pets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        List<PetImage> images = petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        for (PetImage img : images) {
            fileStorageService.delete(img.getImageUrl());
        }
        petImageMapper.delete(new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        petMapper.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "宠物已删除");
        return "redirect:/admin/pets";
    }

    private void saveImages(Long petId, List<MultipartFile> images, Integer coverIndex) {
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file.isEmpty()) continue;
            try {
                String fileName = fileStorageService.store(file);
                PetImage petImage = new PetImage();
                petImage.setPetId(petId);
                petImage.setImageUrl(fileName);
                petImage.setIsCover(coverIndex != null && coverIndex == i + 1 ? 1 : 0);
                petImageMapper.insert(petImage);
            } catch (IOException e) {
                // skip bad files
            }
        }

        // Ensure at least one cover if images exist and no cover was set
        if (coverIndex == null) {
            List<PetImage> existing = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, petId));
            if (!existing.isEmpty()) {
                boolean hasCover = existing.stream().anyMatch(img -> img.getIsCover() == 1);
                if (!hasCover) {
                    PetImage first = existing.get(0);
                    first.setIsCover(1);
                    petImageMapper.updateById(first);
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create admin/pets.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>宠物管理</h1>
        <a href="/admin/pets/create" class="btn btn-primary">发布宠物</a>
    </div>

    <div th:if="${success}" class="alert alert-success alert-dismissible fade show">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${error}" class="alert alert-danger alert-dismissible fade show">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <form class="row g-2 mb-3" method="get">
        <div class="col-auto">
            <input type="text" name="name" class="form-control" placeholder="宠物名称"
                   th:value="${name}">
        </div>
        <div class="col-auto">
            <select name="breedId" class="form-select">
                <option value="">全部品种</option>
                <option th:each="b : ${breeds}" th:value="${b.id}"
                        th:text="${b.breedName}"
                        th:selected="${breedId != null && breedId == b.id}"></option>
            </select>
        </div>
        <div class="col-auto">
            <select name="status" class="form-select">
                <option value="">全部状态</option>
                <option value="available" th:selected="${status == 'available'}">可领养</option>
                <option value="pending" th:selected="${status == 'pending'}">待审核</option>
                <option value="adopted" th:selected="${status == 'adopted'}">已领养</option>
            </select>
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-primary">搜索</button>
            <a href="/admin/pets" class="btn btn-outline-secondary">重置</a>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-hover">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>名称</th>
                    <th>品种</th>
                    <th>性别</th>
                    <th>年龄</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="pet : ${page.records}">
                    <td th:text="${pet.id}">1</td>
                    <td th:text="${pet.name}">宠物名</td>
                    <td><span class="badge bg-info" th:text="${pet.breedName}"></span></td>
                    <td th:text="${pet.gender}">-</td>
                    <td th:text="${pet.age} + '岁'">-</td>
                    <td>
                        <span th:if="${pet.status == 'available'}" class="badge bg-success">可领养</span>
                        <span th:if="${pet.status == 'pending'}" class="badge bg-warning text-dark">待审核</span>
                        <span th:if="${pet.status == 'adopted'}" class="badge bg-secondary">已领养</span>
                    </td>
                    <td th:text="${#temporals.format(pet.createdAt, 'yyyy-MM-dd HH:mm')}">-</td>
                    <td>
                        <a th:href="@{/admin/pets/{id}/edit(id=${pet.id})}" class="btn btn-sm btn-outline-primary">编辑</a>
                        <button class="btn btn-sm btn-outline-danger" data-bs-toggle="modal"
                                th:attr="data-bs-target='#deleteModal' + ${pet.id}">删除</button>

                        <div class="modal fade" th:id="'deleteModal' + ${pet.id}" tabindex="-1">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">确认删除</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body">
                                        确定要删除宠物 <strong th:text="${pet.name}"></strong> 及其所有图片吗？
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                        <form th:action="@{/admin/pets/{id}/delete(id=${pet.id})}" method="post" class="d-inline">
                                            <button type="submit" class="btn btn-danger">确认删除</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div th:replace="~{fragments/pagination :: pagination(page=${page}, baseUrl='/admin/pets')}"></div>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 3: Create admin/pet-form.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <h1 th:text="${isEdit} ? '编辑宠物' : '发布宠物'"></h1>

    <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>

    <form th:action="${isEdit} ? @{/admin/pets/{id}/edit(id=${pet.id})} : @{/admin/pets/create}"
          method="post" enctype="multipart/form-data">
        <div class="row">
            <div class="col-md-8">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">宠物名称</label>
                        <input type="text" name="name" class="form-control" th:value="${pet.name}" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">品种</label>
                        <select name="breedId" class="form-select" required>
                            <option value="">请选择</option>
                            <option th:each="b : ${breeds}" th:value="${b.id}"
                                    th:text="${b.breedName}"
                                    th:selected="${pet.breedId != null && pet.breedId == b.id}"></option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">性别</label>
                        <select name="gender" class="form-select" required>
                            <option value="">请选择</option>
                            <option value="公" th:selected="${pet.gender == '公'}">公</option>
                            <option value="母" th:selected="${pet.gender == '母'}">母</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">年龄（月）</label>
                        <input type="number" name="age" class="form-control" th:value="${pet.age}" required min="0">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">体重（kg）</label>
                        <input type="number" step="0.01" name="weight" class="form-control" th:value="${pet.weight}">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">健康状态</label>
                        <select name="healthStatus" class="form-select" required>
                            <option value="健康" th:selected="${pet.healthStatus == '健康'}">健康</option>
                            <option value="轻微疾病" th:selected="${pet.healthStatus == '轻微疾病'}">轻微疾病</option>
                            <option value="需特殊照顾" th:selected="${pet.healthStatus == '需特殊照顾'}">需特殊照顾</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">疫苗状态</label>
                        <select name="vaccineStatus" class="form-select">
                            <option value="">请选择</option>
                            <option value="已接种" th:selected="${pet.vaccineStatus == '已接种'}">已接种</option>
                            <option value="未接种" th:selected="${pet.vaccineStatus == '未接种'}">未接种</option>
                            <option value="部分接种" th:selected="${pet.vaccineStatus == '部分接种'}">部分接种</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">绝育状态</label>
                        <select name="sterilizationStatus" class="form-select">
                            <option value="">请选择</option>
                            <option value="已绝育" th:selected="${pet.sterilizationStatus == '已绝育'}">已绝育</option>
                            <option value="未绝育" th:selected="${pet.sterilizationStatus == '未绝育'}">未绝育</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label">性格描述</label>
                        <textarea name="personality" class="form-control" rows="3" required
                                  th:text="${pet.personality}"></textarea>
                    </div>
                    <div class="col-12">
                        <label class="form-label">领养要求</label>
                        <textarea name="adoptionRequirement" class="form-control" rows="3"
                                  th:text="${pet.adoptionRequirement}"></textarea>
                    </div>
                    <div class="col-md-6" th:if="${isEdit}">
                        <label class="form-label">状态</label>
                        <select name="status" class="form-select">
                            <option value="available" th:selected="${pet.status == 'available'}">可领养</option>
                            <option value="pending" th:selected="${pet.status == 'pending'}">待审核</option>
                            <option value="adopted" th:selected="${pet.status == 'adopted'}">已领养</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <label class="form-label">上传图片 (jpg/jpeg/png, ≤2MB)</label>

                <div th:if="${isEdit && images != null && !images.isEmpty()}">
                    <p class="text-muted small">已有图片（勾选后将删除）：</p>
                    <div class="row g-2 mb-3">
                        <div class="col-6" th:each="img : ${images}">
                            <div class="position-relative">
                                <img th:src="@{/uploads/pets/{name}(name='thumb_' + ${img.imageUrl})}"
                                     class="img-thumbnail" style="width:100%;height:120px;object-fit:cover;">
                                <div class="form-check position-absolute top-0 end-0 mt-1 me-1">
                                    <input type="checkbox" name="deleteImageIds" th:value="${img.id}"
                                           class="form-check-input">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="imagePreviewContainer" class="row g-2 mb-3"></div>

                <div class="mb-2">
                    <input type="file" name="images" id="imageInput" class="form-control"
                           accept="image/jpeg,image/jpg,image/png" multiple>
                </div>
                <div class="mb-2">
                    <label class="form-label small">设为封面（输入图片序号，从 1 开始）</label>
                    <input type="number" name="coverIndex" class="form-control form-control-sm"
                           min="0" placeholder="留空则默认第一张">
                </div>
            </div>
        </div>
        <hr>
        <button type="submit" class="btn btn-primary">保存</button>
        <a href="/admin/pets" class="btn btn-secondary">取消</a>
    </form>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.getElementById('imageInput').addEventListener('change', function(e) {
        const container = document.getElementById('imagePreviewContainer');
        container.innerHTML = '';
        Array.from(e.target.files).forEach((file, i) => {
            const reader = new FileReader();
            reader.onload = function(ev) {
                const col = document.createElement('div');
                col.className = 'col-6';
                col.innerHTML = '<div class="position-relative">'
                    + '<img src="' + ev.target.result + '" class="img-thumbnail" style="width:100%;height:120px;object-fit:cover;">'
                    + '<span class="position-absolute top-0 start-0 badge bg-dark m-1">' + (i + 1) + '</span>'
                    + '</div>';
                container.appendChild(col);
            };
            reader.readAsDataURL(file);
        });
    });
</script>
</body>
</html>
```

- [ ] **Step 4: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 9: Extend ApplicationService — 审核通过/拒绝

**Files:**
- Modify: `src/main/java/com/petmgt/service/ApplicationService.java`

- [ ] **Step 1: Add approve and reject methods**

Add these methods to the existing `ApplicationService` class (keep all existing code):

```java
@Transactional
public void approve(Long applicationId, Long adminId, String comment) {
    Application app = applicationMapper.selectById(applicationId);
    if (app == null) {
        throw new IllegalArgumentException("申请不存在");
    }
    if (!"pending".equals(app.getStatus())) {
        throw new IllegalArgumentException("该申请已审核过");
    }

    app.setStatus("approved");
    app.setReviewedBy(adminId);
    app.setReviewedAt(java.time.LocalDateTime.now());
    app.setReviewComment(comment);
    applicationMapper.updateById(app);

    Pet pet = petMapper.selectById(app.getPetId());
    pet.setStatus("adopted");
    petMapper.updateById(pet);

    applicationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Application>()
        .eq(Application::getPetId, app.getPetId())
        .eq(Application::getStatus, "pending")
        .ne(Application::getId, applicationId)
        .set(Application::getStatus, "rejected")
        .set(Application::getReviewComment, "该宠物已被领养，系统自动拒绝")
        .set(Application::getReviewedBy, adminId)
        .set(Application::getReviewedAt, java.time.LocalDateTime.now()));
}

@Transactional
public void reject(Long applicationId, Long adminId, String reason) {
    Application app = applicationMapper.selectById(applicationId);
    if (app == null) {
        throw new IllegalArgumentException("申请不存在");
    }
    if (!"pending".equals(app.getStatus())) {
        throw new IllegalArgumentException("该申请已审核过");
    }

    app.setStatus("rejected");
    app.setReviewedBy(adminId);
    app.setReviewedAt(java.time.LocalDateTime.now());
    app.setReviewComment(reason);
    applicationMapper.updateById(app);

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

- [ ] **Step 2: Verify compilation**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 10: Admin ApplicationController — 申请审核

**Files:**
- Create: `src/main/java/com/petmgt/controller/admin/ApplicationController.java`
- Create: `src/main/resources/templates/admin/applications.html`
- Create: `src/main/resources/templates/admin/application-detail.html`

- [ ] **Step 1: Create admin ApplicationController**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Application;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.entity.User;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.service.ApplicationService;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/applications")
public class ApplicationController {

    private final ApplicationMapper applicationMapper;
    private final ApplicationService applicationService;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;

    public ApplicationController(ApplicationMapper applicationMapper,
                                  ApplicationService applicationService,
                                  PetMapper petMapper,
                                  BreedMapper breedMapper,
                                  PetImageMapper petImageMapper,
                                  UserMapper userMapper) {
        this.applicationMapper = applicationMapper;
        this.applicationService = applicationService;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String petName,
                       @RequestParam(required = false) String applicant,
                       Model model) {
        Page<Application> appPage = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Application::getStatus, status);
        }
        if (petName != null && !petName.isEmpty()) {
            List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().like(Pet::getName, petName));
            if (pets.isEmpty()) {
                wrapper.eq(Application::getId, -1L);
            } else {
                wrapper.in(Application::getPetId,
                    pets.stream().map(Pet::getId).collect(Collectors.toList()));
            }
        }
        if (applicant != null && !applicant.isEmpty()) {
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getUsername, applicant));
            if (users.isEmpty()) {
                wrapper.eq(Application::getId, -1L);
            } else {
                wrapper.in(Application::getUserId,
                    users.stream().map(User::getId).collect(Collectors.toList()));
            }
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(appPage, wrapper);

        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                Breed breed = breedMapper.selectById(pet.getBreedId());
                if (breed != null) app.setBreedName(breed.getBreedName());
            }
            User user = userMapper.selectById(app.getUserId());
            if (user != null) app.setApplicantUsername(user.getUsername());
        }

        model.addAttribute("title", "申请审核");
        model.addAttribute("page", result);
        model.addAttribute("status", status);
        model.addAttribute("petName", petName);
        model.addAttribute("applicant", applicant);
        return "admin/applications";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            return "redirect:/admin/applications";
        }
        Pet pet = petMapper.selectById(app.getPetId());
        if (pet != null) {
            app.setPetName(pet.getName());
            Breed breed = breedMapper.selectById(pet.getBreedId());
            if (breed != null) app.setBreedName(breed.getBreedName());
            List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, pet.getId()));
            String coverUrl = images.stream().filter(img -> img.getIsCover() == 1)
                .findFirst().map(PetImage::getImageUrl).orElse(null);
            app.setCoverImageUrl(coverUrl);
        }
        User user = userMapper.selectById(app.getUserId());
        if (user != null) app.setApplicantUsername(user.getUsername());

        model.addAttribute("title", "申请详情");
        model.addAttribute("app", app);
        model.addAttribute("pet", pet);
        model.addAttribute("applicant", user);
        return "admin/application-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(defaultValue = "") String comment,
                          RedirectAttributes redirectAttributes) {
        try {
            Long adminId = SecurityUtil.getCurrentUser().getId();
            applicationService.approve(id, adminId, comment);
            redirectAttributes.addFlashAttribute("success", "申请已通过");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/applications";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            Long adminId = SecurityUtil.getCurrentUser().getId();
            applicationService.reject(id, adminId, reason);
            redirectAttributes.addFlashAttribute("success", "申请已拒绝");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/applications";
    }
}
```

- [ ] **Step 2: Create admin/applications.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <h1 class="mb-3">申请审核</h1>

    <div th:if="${success}" class="alert alert-success alert-dismissible fade show">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${error}" class="alert alert-danger alert-dismissible fade show">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <form class="row g-2 mb-3" method="get">
        <div class="col-auto">
            <select name="status" class="form-select">
                <option value="">全部状态</option>
                <option value="pending" th:selected="${status == 'pending'}">待审核</option>
                <option value="approved" th:selected="${status == 'approved'}">已通过</option>
                <option value="rejected" th:selected="${status == 'rejected'}">已拒绝</option>
                <option value="cancelled" th:selected="${status == 'cancelled'}">已取消</option>
            </select>
        </div>
        <div class="col-auto">
            <input type="text" name="petName" class="form-control" placeholder="宠物名称"
                   th:value="${petName}">
        </div>
        <div class="col-auto">
            <input type="text" name="applicant" class="form-control" placeholder="申请人"
                   th:value="${applicant}">
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-outline-primary">筛选</button>
            <a href="/admin/applications" class="btn btn-outline-secondary">重置</a>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-striped table-hover">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>宠物</th>
                    <th>申请人</th>
                    <th>电话</th>
                    <th>状态</th>
                    <th>申请时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="app : ${page.records}">
                    <td th:text="${app.id}">1</td>
                    <td>
                        <span th:text="${app.petName}">-</span>
                        <small class="text-muted d-block" th:text="${app.breedName}"></small>
                    </td>
                    <td th:text="${app.applicantUsername}">-</td>
                    <td th:text="${app.phone}">-</td>
                    <td>
                        <span th:if="${app.status == 'pending'}" class="badge bg-warning text-dark">待审核</span>
                        <span th:if="${app.status == 'approved'}" class="badge bg-success">已通过</span>
                        <span th:if="${app.status == 'rejected'}" class="badge bg-danger">已拒绝</span>
                        <span th:if="${app.status == 'cancelled'}" class="badge bg-secondary">已取消</span>
                    </td>
                    <td th:text="${#temporals.format(app.createdAt, 'yyyy-MM-dd HH:mm')}">-</td>
                    <td>
                        <a th:href="@{/admin/applications/{id}(id=${app.id})}" class="btn btn-sm btn-outline-primary">查看详情</a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div th:replace="~{fragments/pagination :: pagination(page=${page}, baseUrl='/admin/applications')}"></div>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 3: Create admin/application-detail.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title=${title})}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>

<div class="container mt-4">
    <h1 class="mb-3">申请详情</h1>

    <div class="row">
        <div class="col-md-6">
            <div class="card mb-3">
                <div class="card-header"><strong>申请人信息</strong></div>
                <div class="card-body">
                    <p><strong>用户名：</strong><span th:text="${applicant?.username}">-</span></p>
                    <p><strong>邮箱：</strong><span th:text="${applicant?.email}">-</span></p>
                    <p><strong>电话：</strong><span th:text="${app.phone}">-</span></p>
                    <p><strong>地址：</strong><span th:text="${app.address}">-</span></p>
                    <p><strong>陪伴时间：</strong><span th:text="${app.accompanyTime}">-</span></p>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card mb-3">
                <div class="card-header"><strong>宠物信息</strong></div>
                <div class="card-body">
                    <div th:if="${app.coverImageUrl}" class="mb-2 text-center">
                        <img th:src="@{/uploads/pets/{name}(name='thumb_' + ${app.coverImageUrl})}"
                             class="img-thumbnail" style="max-height:200px;">
                    </div>
                    <p><strong>名称：</strong><span th:text="${app.petName}">-</span></p>
                    <p><strong>品种：</strong><span th:text="${app.breedName}">-</span></p>
                    <p><strong>状态：</strong>
                        <span th:if="${pet?.status == 'available'}" class="badge bg-success">可领养</span>
                        <span th:if="${pet?.status == 'pending'}" class="badge bg-warning text-dark">待审核</span>
                        <span th:if="${pet?.status == 'adopted'}" class="badge bg-secondary">已领养</span>
                    </p>
                </div>
            </div>
        </div>
    </div>

    <div class="card mb-3">
        <div class="card-header"><strong>申请内容</strong></div>
        <div class="card-body">
            <p><strong>养宠经验：</strong></p>
            <p th:text="${app.experience}">-</p>
            <p><strong>申请理由：</strong></p>
            <p th:text="${app.reason}">-</p>
        </div>
    </div>

    <div class="card mb-3">
        <div class="card-header"><strong>状态</strong></div>
        <div class="card-body">
            <p>
                <span th:if="${app.status == 'pending'}" class="badge bg-warning text-dark fs-6">待审核</span>
                <span th:if="${app.status == 'approved'}" class="badge bg-success fs-6">已通过</span>
                <span th:if="${app.status == 'rejected'}" class="badge bg-danger fs-6">已拒绝</span>
                <span th:if="${app.status == 'cancelled'}" class="badge bg-secondary fs-6">已取消</span>
            </p>
            <div th:if="${app.reviewComment}">
                <p><strong>审核备注：</strong></p>
                <p th:text="${app.reviewComment}">-</p>
            </div>
        </div>
    </div>

    <div class="mb-4" th:if="${app.status == 'pending'}">
        <div class="row">
            <div class="col-md-6">
                <form th:action="@{/admin/applications/{id}/approve(id=${app.id})}" method="post">
                    <div class="mb-2">
                        <label class="form-label">审核通过备注（可选）</label>
                        <textarea name="comment" class="form-control" rows="2"
                                  placeholder="通过审核的备注信息"></textarea>
                    </div>
                    <div class="text-muted small mb-2">
                        <strong>注意：</strong>通过后将自动拒绝该宠物的其他待审核申请，并将宠物状态设为"已领养"。
                    </div>
                    <button type="submit" class="btn btn-success">通过申请</button>
                </form>
            </div>
            <div class="col-md-6">
                <form th:action="@{/admin/applications/{id}/reject(id=${app.id})}" method="post">
                    <div class="mb-2">
                        <label class="form-label">拒绝原因（必填）</label>
                        <textarea name="reason" class="form-control" rows="2" required
                                  placeholder="请填写拒绝原因"></textarea>
                    </div>
                    <div class="text-muted small mb-2">
                        <strong>注意：</strong>拒绝后如无其他待审核申请，宠物将恢复为"可领养"状态。
                    </div>
                    <button type="submit" class="btn btn-danger">拒绝申请</button>
                </form>
            </div>
        </div>
    </div>

    <a href="/admin/applications" class="btn btn-secondary">返回列表</a>
</div>

<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 4: Verify compilation and run**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

---

### Task 11: Integration Verification

- [ ] **Step 1: Start the application**

```bash
cd d:/code/pet-mgt && mvn spring-boot:run
```

- [ ] **Step 2: Verify P3 checklist**

| # | Check | Expected |
|---|-------|----------|
| 1 | Login as admin, visit `/admin` | 5 stat cards visible, 4 management links |
| 2 | Click "用户管理" → "新增用户" | Create user with roles, verify login |
| 3 | Edit user, change enabled status | Changes persist |
| 4 | Try delete own admin account | Error message shown |
| 5 | Visit breed management, create/delete | CRUD works, delete blocked if pets exist |
| 6 | Visit pet management → "发布宠物" | Upload images, set cover, verify thumbnail |
| 7 | Edit pet, delete/add images | Changes persist |
| 8 | Visit application review | List shows pending apps with filters |
| 9 | Approve an application | Pet → adopted, other pending → rejected |
| 10 | Reject an application | Pet → available (if no other pending) |

- [ ] **Step 3: Stop the application**

---

### P3 Verification Checklist (from spec)

- [ ] 后台统计数字正确
- [ ] 用户 CRUD 正常
- [ ] 品种 CRUD 正常，删除保护生效
- [ ] 宠物发布含多图上传正常
- [ ] 缩略图生成并正确展示
- [ ] 审核通过全联动正确
- [ ] 审核拒绝全联动正确
