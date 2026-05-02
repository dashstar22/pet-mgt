# P5 收尾与打磨 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Polish the pet adoption platform with global exception handling, frontend interaction improvements, and data/UX finishing touches.

**Architecture:** Add `BusinessException` + `@ControllerAdvice` GlobalExceptionHandler. Refactor 6 controllers to use BusinessException instead of local try-catch. Add JS password validation and breed type cascade. Enrich test data, add nav highlighting.

**Tech Stack:** Spring Boot 4.x, Thymeleaf, Bootstrap 5, MyBatis-Plus

---

### Task 1: Create BusinessException and error pages

**Files:**
- Create: `src/main/java/com/petmgt/exception/BusinessException.java`
- Create: `src/main/resources/templates/error.html`
- Create: `src/main/resources/templates/404.html`
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Write BusinessException**

```java
package com.petmgt.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: Write error.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title='系统错误')}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>
<div class="container mt-5 text-center">
    <h1 class="display-1 text-muted">500</h1>
    <p class="lead">系统出现错误，请稍后重试。</p>
    <a href="/" class="btn btn-primary">返回首页</a>
</div>
<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 3: Write 404.html**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/header :: head(title='页面不存在')}"></head>
<body>
<nav th:replace="~{fragments/nav :: nav}"></nav>
<div class="container mt-5 text-center">
    <h1 class="display-1 text-muted">404</h1>
    <p class="lead">页面不存在。</p>
    <a href="/" class="btn btn-primary">返回首页</a>
</div>
<footer th:replace="~{fragments/footer :: footer}"></footer>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

- [ ] **Step 4: Configure 404 handling in application.properties**

Append:
```properties
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=true
```

- [ ] **Step 5: Compile and verify**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/petmgt/exception/ src/main/resources/templates/error.html src/main/resources/templates/404.html src/main/resources/application.properties
git commit -m "feat: add BusinessException, error.html, and 404.html"
```

---

### Task 2: Create GlobalExceptionHandler

**Files:**
- Create: `src/main/java/com/petmgt/handler/GlobalExceptionHandler.java`

- [ ] **Step 1: Write GlobalExceptionHandler**

```java
package com.petmgt.handler;

import com.petmgt.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404() {
        return "404";
    }

    @ExceptionHandler(BusinessException.class)
    public RedirectView handleBusinessException(BusinessException e,
                                                 HttpServletRequest request,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && !referer.isBlank()) ? referer : "/";
        return new RedirectView(redirectUrl);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e) {
        log.error("Unhandled exception", e);
        return "error";
    }
}
```

- [ ] **Step 2: Compile and verify**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/handler/
git commit -m "feat: add GlobalExceptionHandler for BusinessException and fallback errors"
```

---

### Task 3: Refactor AuthController, ProfileController, BreedController

**Files:**
- Modify: `src/main/java/com/petmgt/controller/AuthController.java`
- Modify: `src/main/java/com/petmgt/controller/user/ProfileController.java`
- Modify: `src/main/java/com/petmgt/controller/admin/BreedController.java`

- [ ] **Step 1: Refactor AuthController**

Replace the entire file — add `BusinessException` import, remove `RedirectAttributes` from `register()`, catch `IllegalArgumentException` and rethrow as `BusinessException`:

```java
package com.petmgt.controller;

import com.petmgt.dto.RegisterForm;
import com.petmgt.exception.BusinessException;
import com.petmgt.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "登录");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("title", "注册");
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(RegisterForm form) {
        try {
            userService.register(form);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return "redirect:/login?registered";
    }
}
```

- [ ] **Step 2: Refactor ProfileController**

Replace the file — convert email validation to `BusinessException`, remove `RedirectAttributes` from `updateProfile()`:

```java
package com.petmgt.controller.user;

import com.petmgt.entity.User;
import com.petmgt.exception.BusinessException;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class ProfileController {

    private final UserMapper userMapper;

    public ProfileController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User user = SecurityUtil.getCurrentUser();
        model.addAttribute("title", "个人中心");
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(String email, String avatarUrl) {
        if (email != null && !email.isBlank()
                && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException("邮箱格式不正确");
        }
        User user = SecurityUtil.getCurrentUser();
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return "redirect:/user/profile";
    }
}
```

- [ ] **Step 3: Refactor BreedController**

Replace the file — add `BusinessException` import, refactor `delete()` to catch and rethrow:

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.entity.Breed;
import com.petmgt.exception.BusinessException;
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
    public String delete(@PathVariable Long id) {
        try {
            breedService.delete(id);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return "redirect:/admin/breeds";
    }
}
```

- [ ] **Step 4: Compile and verify**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/petmgt/controller/AuthController.java \
        src/main/java/com/petmgt/controller/user/ProfileController.java \
        src/main/java/com/petmgt/controller/admin/BreedController.java
git commit -m "refactor: use BusinessException in Auth, Profile, and Breed controllers"
```

---

### Task 4: Refactor both ApplicationControllers

**Files:**
- Modify: `src/main/java/com/petmgt/controller/user/ApplicationController.java`
- Modify: `src/main/java/com/petmgt/controller/admin/ApplicationController.java`

- [ ] **Step 1: Refactor user/ApplicationController**

Replace the file — add `BusinessException` import, refactor `submitApplication()` and `cancelApplication()`. Keep `RedirectAttributes` for success flash:

```java
package com.petmgt.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApplicationForm;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.exception.BusinessException;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
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

@Controller
@RequestMapping("/user")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;

    public ApplicationController(ApplicationService applicationService,
                                 ApplicationMapper applicationMapper,
                                 PetMapper petMapper,
                                 BreedMapper breedMapper,
                                 PetImageMapper petImageMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
    }

    @GetMapping("/apply/{petId}")
    public String applicationForm(@PathVariable Long petId, Model model) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return "redirect:/pets";
        }
        model.addAttribute("title", "提交领养申请");
        model.addAttribute("pet", pet);
        return "user/application-form";
    }

    @PostMapping("/apply")
    public String submitApplication(ApplicationForm form, RedirectAttributes redirectAttributes) {
        try {
            Long userId = SecurityUtil.getCurrentUser().getId();
            applicationService.submit(form, userId);
            redirectAttributes.addFlashAttribute("success", "申请已提交，请等待审核");
            return "redirect:/user/applications";
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @GetMapping("/applications")
    public String myApplications(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<Application> appPage = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
            .eq(Application::getUserId, userId)
            .orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(appPage, wrapper);

        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                app.setCoverImageUrl(
                    petImageMapper.selectList(new LambdaQueryWrapper<com.petmgt.entity.PetImage>()
                        .eq(com.petmgt.entity.PetImage::getPetId, pet.getId())
                        .eq(com.petmgt.entity.PetImage::getIsCover, 1))
                        .stream().findFirst()
                        .map(com.petmgt.entity.PetImage::getImageUrl).orElse(null));
                var breed = breedMapper.selectById(pet.getBreedId());
                if (breed != null) {
                    app.setBreedName(breed.getBreedName());
                }
            }
        }

        model.addAttribute("title", "我的申请");
        model.addAttribute("page", result);
        return "user/applications";
    }

    @PostMapping("/applications/{id}/cancel")
    public String cancelApplication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long userId = SecurityUtil.getCurrentUser().getId();
            applicationService.cancel(id, userId);
            redirectAttributes.addFlashAttribute("success", "申请已取消");
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        return "redirect:/user/applications";
    }
}
```

- [ ] **Step 2: Refactor admin/ApplicationController**

Only the `approve()` and `reject()` methods change — replace try-catch with BusinessException rethrow. The rest of the file stays the same. Add `import com.petmgt.exception.BusinessException;`:

Replace `approve()`:
```java
@PostMapping("/{id}/approve")
public String approve(@PathVariable Long id,
                      @RequestParam(defaultValue = "") String comment,
                      RedirectAttributes redirectAttributes) {
    try {
        Long adminId = SecurityUtil.getCurrentUser().getId();
        applicationService.approve(id, adminId, comment);
        redirectAttributes.addFlashAttribute("success", "申请已通过");
    } catch (IllegalArgumentException e) {
        throw new BusinessException(e.getMessage());
    }
    return "redirect:/admin/applications";
}
```

Replace `reject()`:
```java
@PostMapping("/{id}/reject")
public String reject(@PathVariable Long id,
                     @RequestParam String reason,
                     RedirectAttributes redirectAttributes) {
    try {
        Long adminId = SecurityUtil.getCurrentUser().getId();
        applicationService.reject(id, adminId, reason);
        redirectAttributes.addFlashAttribute("success", "申请已拒绝");
    } catch (IllegalArgumentException e) {
        throw new BusinessException(e.getMessage());
    }
    return "redirect:/admin/applications";
}
```

- [ ] **Step 3: Compile and verify**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/petmgt/controller/user/ApplicationController.java \
        src/main/java/com/petmgt/controller/admin/ApplicationController.java
git commit -m "refactor: use BusinessException in Application controllers"
```

---

### Task 5: Refactor PetManageController and UserController

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/PetManageController.java`
- Modify: `src/main/java/com/petmgt/controller/admin/UserController.java`

- [ ] **Step 1: Refactor PetManageController**

Remove the try-catch blocks from `create()` and `edit()` methods. Generic exceptions will flow to the GlobalExceptionHandler fallback. Only edit the two methods:

Replace `create()`:
```java
@PostMapping("/create")
public String create(Pet pet,
                     @RequestParam(required = false) List<MultipartFile> images,
                     @RequestParam(required = false) Integer coverIndex,
                     RedirectAttributes redirectAttributes) {
    pet.setStatus("available");
    pet.setCreatedBy(SecurityUtil.getCurrentUser().getId());
    petMapper.insert(pet);
    if (images != null) {
        saveImages(pet.getId(), images, coverIndex);
    }
    redirectAttributes.addFlashAttribute("success", "宠物发布成功");
    return "redirect:/admin/pets";
}
```

Replace `edit()`:
```java
@PostMapping("/{id}/edit")
public String edit(@PathVariable Long id, Pet pet,
                   @RequestParam(required = false) List<MultipartFile> newImages,
                   @RequestParam(required = false) Integer coverIndex,
                   @RequestParam(required = false) List<Long> deleteImageIds,
                   RedirectAttributes redirectAttributes) {
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
    return "redirect:/admin/pets";
}
```

- [ ] **Step 2: Refactor UserController**

Remove the `try { ... } catch (Exception e) { ... }` wrappers from `create()` and `edit()`. Keep validation checks inline. Replace `create()`:

```java
@PostMapping("/create")
public String create(User user, @RequestParam(required = false) List<Long> roleIds,
                     RedirectAttributes redirectAttributes) {
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
    return "redirect:/admin/users";
}
```

Replace `edit()`:
```java
@PostMapping("/{id}/edit")
public String edit(@PathVariable Long id, User user,
                   @RequestParam(required = false) List<Long> roleIds,
                   RedirectAttributes redirectAttributes) {
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
    return "redirect:/admin/users";
}
```

`delete()` stays unchanged (no try-catch to remove).

- [ ] **Step 3: Compile and verify**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/PetManageController.java \
        src/main/java/com/petmgt/controller/admin/UserController.java
git commit -m "refactor: remove generic try-catch in PetManage and User controllers"
```

---

### Task 6: Add password match JS validation

**Files:**
- Modify: `src/main/resources/templates/auth/register.html`

- [ ] **Step 1: Add JS validation and improve placeholders**

Add `<script>` before `</body>` and update password placeholders to `minlength="6"`:

The complete section to add before `</body>`:
```html
<script>
document.querySelector('form').addEventListener('submit', function(e) {
    if (document.getElementById('password').value !== document.getElementById('confirmPassword').value) {
        e.preventDefault();
        alert('两次输入的密码不一致，请重新输入。');
    }
});
</script>
```

Also change the password input placeholders in the form:
- `placeholder="至少6个字符"` (was "请输入密码")
- Add `minlength="6"` to both password inputs

- [ ] **Step 2: Verify**

Start: `cd d:/code/pet-mgt && mvn spring-boot:run`
Visit `http://localhost:8080/register`, enter mismatched passwords, submit.
Expected: Alert popup, form not submitted.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/auth/register.html
git commit -m "feat: add JS password match validation on register page"
```

---

### Task 7: Add breed type cascade to pet form

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/BreedController.java`
- Modify: `src/main/java/com/petmgt/controller/admin/PetManageController.java`
- Modify: `src/main/resources/templates/admin/pet-form.html`

- [ ] **Step 1: Add JSON API endpoint to BreedController**

Add to `BreedController.java` (imports needed: `org.springframework.web.bind.annotation.ResponseBody`, `com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper`, `java.util.List`):

```java
@GetMapping("/api")
@ResponseBody
public List<Breed> apiBreeds(@RequestParam(required = false) String petType) {
    if (petType == null || petType.isEmpty()) {
        return breedMapper.selectList(null);
    }
    return breedMapper.selectList(
        new LambdaQueryWrapper<Breed>().eq(Breed::getPetType, petType));
}
```

- [ ] **Step 2: Update PetManageController to pass petType**

In `createForm()`, add: `model.addAttribute("petType", "");`

In `editForm()`, after `Pet pet = petMapper.selectById(id);`, add:
```java
Breed breed = breedMapper.selectById(pet.getBreedId());
model.addAttribute("petType", breed != null ? breed.getPetType() : "");
```

- [ ] **Step 3: Update pet-form.html — add type dropdown and cascade JS**

In the form's first row of inputs, add before the breed select:
```html
<div class="col-md-6">
    <label class="form-label">宠物类型</label>
    <select id="petTypeSelect" class="form-select">
        <option value="">全部类型</option>
        <option value="猫" th:selected="${petType == '猫'}">猫</option>
        <option value="狗" th:selected="${petType == '狗'}">狗</option>
        <option value="兔" th:selected="${petType == '兔'}">兔</option>
    </select>
</div>
```

Give the breed select an id and data attribute:
```html
<select name="breedId" id="breedSelect" class="form-select" required
        th:attr="data-selected=${pet.breedId}">
```

For the initial options, keep them as-is (they'll be replaced by JS when type changes, or left as all breeds).

Add cascade JS at the bottom of the existing `<script>` block:
```javascript
document.getElementById('petTypeSelect').addEventListener('change', async function() {
    const type = this.value;
    const breedSelect = document.getElementById('breedSelect');
    const selectedBreedId = breedSelect.getAttribute('data-selected');
    let url = '/admin/breeds/api';
    if (type) url += '?petType=' + encodeURIComponent(type);
    try {
        const resp = await fetch(url);
        const breeds = await resp.json();
        breedSelect.innerHTML = '<option value="">请选择品种</option>';
        breeds.forEach(function(b) {
            const sel = selectedBreedId && b.id == selectedBreedId ? ' selected' : '';
            breedSelect.innerHTML += '<option value="' + b.id + '"' + sel + '>' + b.breedName + '</option>';
        });
    } catch (e) {
        breedSelect.innerHTML = '<option value="">加载失败</option>';
    }
});

// On page load, if editing with a pre-selected type, trigger breed load
(function() {
    var typeSelect = document.getElementById('petTypeSelect');
    if (typeSelect.value) {
        typeSelect.dispatchEvent(new Event('change'));
    }
})();
```

- [ ] **Step 4: Verify**

Start: `cd d:/code/pet-mgt && mvn spring-boot:run`
Visit `/admin/pets/create` → select 猫 → breed dropdown should filter to only cat breeds.
Visit `/admin/pets/1/edit` → should pre-load breeds for the pet's type with its breed selected.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/BreedController.java \
        src/main/java/com/petmgt/controller/admin/PetManageController.java \
        src/main/resources/templates/admin/pet-form.html
git commit -m "feat: add breed type cascade to pet form"
```

---

### Task 8: Enrich test data in data.sql

**Files:**
- Modify: `src/main/resources/data.sql`

- [ ] **Step 1: Add more breeds and pets**

Replace the current pet_breed and pet INSERT sections with richer data:

```sql
INSERT IGNORE INTO pet_breed (breed_name, pet_type) VALUES
('British Shorthair', '猫'),
('Persian', '猫'),
('Siamese', '猫'),
('Maine Coon', '猫'),
('Ragdoll', '猫'),
('Golden Retriever', '狗'),
('Corgi', '狗'),
('Husky', '狗'),
('Labrador', '狗'),
('Poodle', '狗'),
('Holland Lop', '兔'),
('Netherland Dwarf', '兔'),
('Mini Rex', '兔');

INSERT IGNORE INTO pet (name, breed_id, gender, age, weight, health_status, vaccine_status, sterilization_status, personality, adoption_requirement, status, created_by)
VALUES
('橘橘', 1, '公', 12, 4.5, '健康', '已接种', '已绝育', '温顺亲人，喜欢晒太阳，已会用猫砂', '有稳定住所，同意定期回访', 'available', 1),
('雪球', 2, '母', 8, 3.2, '健康', '已接种', '未绝育', '安静优雅，喜欢被抚摸，适合安静家庭', '室内饲养，不笼养', 'available', 1),
('小暹', 3, '公', 6, 3.0, '健康', '已接种', '已绝育', '活泼好动，叫声洪亮，非常粘人', '有时间陪伴，能接受猫咪话多', 'available', 1),
('大毛', 4, '公', 24, 7.5, '健康', '已接种', '已绝育', '体型大但性格温柔，喜欢玩水', '空间足够大，有养猫经验优先', 'available', 1),
('布布', 5, '母', 10, 4.0, '轻微疾病', '部分接种', '已绝育', '布偶猫典型性格，安静粘人，需要定期梳毛', '有耐心打理长毛猫', 'available', 1),
('大黄', 6, '公', 18, 30.0, '健康', '已接种', '已绝育', '金毛典型暖男性格，喜欢捡球，对小孩友善', '每天至少遛两次，有足够活动空间', 'available', 1),
('短腿', 7, '公', 14, 12.0, '健康', '已接种', '未绝育', '精力充沛，喜欢跑跳，柯基标志性微笑', '注意控制体重，避免爬楼梯伤脊椎', 'available', 1),
('二哈', 8, '母', 16, 22.0, '健康', '已接种', '已绝育', '活泼好动，表情丰富，需要大量运动', '有耐心，能接受拆家风险，最好有院子', 'available', 1),
('拉布', 9, '公', 20, 28.0, '健康', '已接种', '已绝育', '温顺忠诚，智商高，适合做导盲犬或家庭犬', '能保证每天充足运动量', 'adopted', 1),
('卷卷', 10, '母', 9, 6.0, '健康', '已接种', '未绝育', '聪明机灵，不易掉毛，适合过敏体质', '定期美容护理，有养贵宾经验优先', 'available', 1),
('团子', 11, '公', 5, 1.5, '健康', '未接种', '未绝育', '可爱温顺，喜欢被抱着，会用兔厕所', '提供足够干草和活动空间', 'available', 1),
('小黑', 12, '母', 4, 1.0, '健康', '未接种', '未绝育', '体型小巧，活泼好动，适合小空间饲养', '注意保暖，笼子不能太小', 'available', 1),
('绒绒', 13, '母', 7, 2.0, '轻微疾病', '未接种', '未绝育', '毛发柔软如丝绒，性格温顺安静', '有养兔经验，注意饮食管理', 'available', 1);
```

- [ ] **Step 2: Verify**

Start: `cd d:/code/pet-mgt && mvn spring-boot:run`
Visit `/pets` → should see 12 pets (1 adopted, rest available) across 13 breeds.
Expected: Rich variety of pets visible in list.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/data.sql
git commit -m "feat: enrich test data with more breeds and pets"
```

---

### Task 9: Add nav highlighting and UX polish

**Files:**
- Modify: `src/main/resources/templates/fragments/nav.html`
- Modify: `src/main/resources/templates/auth/login.html`

- [ ] **Step 1: Add nav active class based on current path**

In `nav.html`, add `th:classappend` to each nav link to highlight the current page:

```html
<nav th:fragment="nav" class="navbar navbar-expand-lg navbar-dark bg-primary"
     xmlns:th="http://www.thymeleaf.org"
     xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
    <div class="container">
        <a class="navbar-brand" href="/">pet-mgt</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="/pets"
                       th:classappend="${#request.getRequestURI() == '/pets' || #request.getRequestURI().startsWith('/pets/') ? 'active' : ''}">宠物列表</a>
                </li>
                <li class="nav-item" sec:authorize="isAuthenticated()">
                    <a class="nav-link" href="/user/ai-match"
                       th:classappend="${#request.getRequestURI().startsWith('/user/ai-match') ? 'active' : ''}">AI 匹配</a>
                </li>
                <li class="nav-item" sec:authorize="isAuthenticated()">
                    <a class="nav-link" href="/user/applications"
                       th:classappend="${#request.getRequestURI().startsWith('/user/applications') ? 'active' : ''}">我的申请</a>
                </li>
                <li class="nav-item" sec:authorize="isAuthenticated()">
                    <a class="nav-link" href="/user/ai-chat"
                       th:classappend="${#request.getRequestURI() == '/user/ai-chat' ? 'active' : ''}">养宠问答</a>
                </li>
                <li class="nav-item" sec:authorize="hasRole('ADMIN')">
                    <a class="nav-link" href="/admin"
                       th:classappend="${#request.getRequestURI().startsWith('/admin') ? 'active' : ''}">后台管理</a>
                </li>
            </ul>
            <ul class="navbar-nav">
                <li class="nav-item" sec:authorize="!isAuthenticated()">
                    <a class="nav-link" href="/login"
                       th:classappend="${#request.getRequestURI() == '/login' ? 'active' : ''}">登录</a>
                </li>
                <li class="nav-item" sec:authorize="!isAuthenticated()">
                    <a class="nav-link" href="/register"
                       th:classappend="${#request.getRequestURI() == '/register' ? 'active' : ''}">注册</a>
                </li>
                <li class="nav-item dropdown" sec:authorize="isAuthenticated()">
                    <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button"
                       data-bs-toggle="dropdown" aria-expanded="false"
                       th:text="${#authentication.name}">用户名</a>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
                        <li><a class="dropdown-item" href="/user/profile">个人中心</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <form th:action="@{/logout}" method="post" class="m-0">
                                <button type="submit" class="dropdown-item">退出登录</button>
                            </form>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
</nav>
```

- [ ] **Step 2: Add login page placeholder hints**

In `login.html`, update password placeholder to `placeholder="请输入密码"` if not already set.

- [ ] **Step 3: Verify**

Start: `cd d:/code/pet-mgt && mvn spring-boot:run`
Navigate to different pages → the nav link for the current section should be highlighted.
Expected: Active nav link has `.active` class (darker background).

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/fragments/nav.html
git commit -m "feat: add nav highlighting for current page"
```

---

### Task 10: Final verification — build and smoke test

- [ ] **Step 1: Full build**

Run: `cd d:/code/pet-mgt && mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: Start application**

Run: `cd d:/code/pet-mgt && mvn spring-boot:run`

- [ ] **Step 3: Smoke test checklist**

| Test | Expected |
|------|----------|
| Visit `/` | Home page loads, pets displayed |
| Visit `/register` | Register form with password hints |
| Register with mismatched passwords | JS alert, form blocked |
| Register with taken username | Flash error from BusinessException |
| Login as admin/123456 | Dashboard nav visible |
| Visit `/admin/pets/create` | Type dropdown + breed cascade works |
| Select 猫 → breeds filter | Only cat breeds shown |
| Visit `/nonexistent` | Custom 404 page |
| Trigger a server error | Custom error page (500) |
| All nav links highlight on active page | Active class present |

- [ ] **Step 4: Commit any remaining changes**

```bash
git status
git diff
# If any remaining changes, commit them
```
