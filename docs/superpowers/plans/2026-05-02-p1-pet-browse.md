# P1 — 宠物浏览功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现宠物列表分页浏览、多条件筛选、宠物详情展示（含图片放大）、首页推荐宠物卡片。

**Architecture:** 新增 `PetService` 封装宠物查询逻辑（MyBatis-Plus Page + 条件构造器），`PetController` 处理 `/pets` 列表和 `/pets/{id}` 详情，更新首页展示最新宠物。

**Tech Stack:** Spring Boot 3.5.6, MyBatis-Plus 3.5.9, Thymeleaf, Bootstrap 5.3 CDN

**Prerequisite:** P0 已完成 — 数据库表、实体、Mapper 均已就绪。`Pet`, `Breed`, `PetImage` 实体和对应 Mapper 已存在。

---

### Task 1: PetSearchCriteria DTO

**Files:**
- Create: `src/main/java/com/petmgt/dto/PetSearchCriteria.java`

- [ ] **Step 1: Create PetSearchCriteria DTO**

```java
package com.petmgt.dto;

import lombok.Data;

@Data
public class PetSearchCriteria {
    private String petType;
    private Long breedId;
    private String gender;
    private String status;
    private String name;
}
```

---

### Task 2: PetService

**Files:**
- Create: `src/main/java/com/petmgt/service/PetService.java`

- [ ] **Step 1: Create PetService with paginated list query**

```java
package com.petmgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.PetSearchCriteria;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;

    public PetService(PetMapper petMapper, BreedMapper breedMapper, PetImageMapper petImageMapper) {
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
    }

    public Page<Pet> findPets(Page<Pet> page, PetSearchCriteria criteria) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (criteria.getPetType() != null && !criteria.getPetType().isEmpty()) {
            // petType is on breed, handled via breedIds
            List<Breed> breeds = breedMapper.selectList(
                new LambdaQueryWrapper<Breed>().eq(Breed::getPetType, criteria.getPetType()));
            if (breeds.isEmpty()) {
                // no matching breeds, return empty page
                return page;
            }
            List<Long> breedIds = breeds.stream().map(Breed::getId).collect(Collectors.toList());
            wrapper.in(Pet::getBreedId, breedIds);
        }
        if (criteria.getBreedId() != null) {
            wrapper.eq(Pet::getBreedId, criteria.getBreedId());
        }
        if (criteria.getGender() != null && !criteria.getGender().isEmpty()) {
            wrapper.eq(Pet::getGender, criteria.getGender());
        }
        if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            wrapper.eq(Pet::getStatus, criteria.getStatus());
        }
        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            wrapper.like(Pet::getName, criteria.getName());
        }
        wrapper.orderByDesc(Pet::getCreatedAt);
        Page<Pet> result = petMapper.selectPage(page, wrapper);
        // populate transient fields
        populateBreedNames(result.getRecords());
        populateCoverImages(result.getRecords());
        return result;
    }

    public Pet findPetDetail(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return null;
        }
        // populate breed name
        Breed breed = breedMapper.selectById(pet.getBreedId());
        if (breed != null) {
            pet.setBreedName(breed.getBreedName());
        }
        // all images are loaded separately by controller
        return pet;
    }

    public List<Pet> findLatestPets(int limit) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getStatus, "available")
               .orderByDesc(Pet::getCreatedAt)
               .last("LIMIT " + limit);
        List<Pet> pets = petMapper.selectList(wrapper);
        populateBreedNames(pets);
        populateCoverImages(pets);
        return pets;
    }

    public List<PetImage> findPetImages(Long petId) {
        return petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, petId));
    }

    private void populateBreedNames(List<Pet> pets) {
        if (pets.isEmpty()) return;
        List<Long> breedIds = pets.stream().map(Pet::getBreedId).distinct().collect(Collectors.toList());
        List<Breed> breeds = breedMapper.selectBatchIds(breedIds);
        Map<Long, String> breedNameMap = breeds.stream()
            .collect(Collectors.toMap(Breed::getId, Breed::getBreedName));
        for (Pet pet : pets) {
            pet.setBreedName(breedNameMap.get(pet.getBreedId()));
        }
    }

    private void populateCoverImages(List<Pet> pets) {
        if (pets.isEmpty()) return;
        List<Long> petIds = pets.stream().map(Pet::getId).collect(Collectors.toList());
        List<PetImage> covers = petImageMapper.selectList(
            new LambdaQueryWrapper<PetImage>()
                .in(PetImage::getPetId, petIds)
                .eq(PetImage::getIsCover, 1));
        Map<Long, String> coverMap = covers.stream()
            .collect(Collectors.toMap(PetImage::getPetId, PetImage::getImageUrl, (a, b) -> a));
        for (Pet pet : pets) {
            pet.setCoverImageUrl(coverMap.get(pet.getId()));
        }
    }
}
```

---

### Task 3: PetController

**Files:**
- Create: `src/main/java/com/petmgt/controller/PetController.java`

- [ ] **Step 1: Create PetController with list and detail endpoints**

```java
package com.petmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.PetSearchCriteria;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PetController {

    private final PetService petService;
    private final BreedMapper breedMapper;

    public PetController(PetService petService, BreedMapper breedMapper) {
        this.petService = petService;
        this.breedMapper = breedMapper;
    }

    @GetMapping("/pets")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "12") int size,
                       PetSearchCriteria criteria,
                       Model model) {
        Page<Pet> petPage = petService.findPets(new Page<>(page, size), criteria);
        List<Breed> breeds = breedMapper.selectList(null);

        model.addAttribute("title", "宠物列表");
        model.addAttribute("petPage", petPage);
        model.addAttribute("breeds", breeds);
        model.addAttribute("criteria", criteria);
        return "pet/list";
    }

    @GetMapping("/pets/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Pet pet = petService.findPetDetail(id);
        if (pet == null) {
            return "redirect:/pets";
        }
        List<PetImage> images = petService.findPetImages(id);

        model.addAttribute("title", pet.getName() + " - 宠物详情");
        model.addAttribute("pet", pet);
        model.addAttribute("images", images);
        return "pet/detail";
    }
}
```

---

### Task 4: Pagination Fragment

**Files:**
- Create: `src/main/resources/templates/fragments/pagination.html`

- [ ] **Step 1: Create reusable pagination fragment**

```html
<div th:fragment="pagination(page, baseUrl)" xmlns:th="http://www.thymeleaf.org">
    <th:block th:if="${page.pages > 1}">
        <nav aria-label="Page navigation">
            <ul class="pagination justify-content-center">
                <li class="page-item" th:classappend="${page.current <= 1} ? 'disabled' : ''">
                    <a class="page-link" th:href="@{${baseUrl}(page=${page.current - 1})}"
                       th:if="${page.current > 1}">&laquo; 上一页</a>
                    <span class="page-link" th:unless="${page.current > 1}">&laquo; 上一页</span>
                </li>
                <th:block th:each="i : ${#numbers.sequence(1, page.pages)}">
                    <th:block th:if="${i >= page.current - 2 && i <= page.current + 2}">
                        <li class="page-item" th:classappend="${i == page.current} ? 'active' : ''">
                            <a class="page-link" th:href="@{${baseUrl}(page=${i})}"
                               th:text="${i}">1</a>
                        </li>
                    </th:block>
                </th:block>
                <li class="page-item" th:classappend="${page.current >= page.pages} ? 'disabled' : ''">
                    <a class="page-link" th:href="@{${baseUrl}(page=${page.current + 1})}"
                       th:if="${page.current < page.pages}">下一页 &raquo;</a>
                    <span class="page-link" th:unless="${page.current < page.pages}">下一页 &raquo;</span>
                </li>
            </ul>
        </nav>
    </th:block>
</div>
```

---

### Task 5: Pet List Page

**Files:**
- Create: `src/main/resources/templates/pet/list.html`

- [ ] **Step 1: Create pet list page with filters, cards, and pagination**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/header :: head(${title})}"></head>
<body>
    <div th:replace="~{fragments/nav :: nav}"></div>

    <div class="container mt-4">
        <h2 class="mb-3">宠物列表</h2>

        <!-- Filter Form -->
        <div class="card mb-4">
            <div class="card-body">
                <form th:action="@{/pets}" method="get" class="row g-2">
                    <div class="col-6 col-md-3">
                        <label class="form-label">宠物类型</label>
                        <select name="petType" class="form-select">
                            <option value="">全部类型</option>
                            <option value="猫" th:selected="${criteria.petType == '猫'}">猫</option>
                            <option value="狗" th:selected="${criteria.petType == '狗'}">狗</option>
                            <option value="兔" th:selected="${criteria.petType == '兔'}">兔</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-3">
                        <label class="form-label">品种</label>
                        <select name="breedId" class="form-select">
                            <option value="">全部品种</option>
                            <option th:each="breed : ${breeds}"
                                    th:value="${breed.id}"
                                    th:text="${breed.breedName}"
                                    th:selected="${criteria.breedId == breed.id}"></option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label">性别</label>
                        <select name="gender" class="form-select">
                            <option value="">全部</option>
                            <option value="公" th:selected="${criteria.gender == '公'}">公</option>
                            <option value="母" th:selected="${criteria.gender == '母'}">母</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label">状态</label>
                        <select name="status" class="form-select">
                            <option value="">全部</option>
                            <option value="available" th:selected="${criteria.status == 'available'}">可领养</option>
                            <option value="adopted" th:selected="${criteria.status == 'adopted'}">已领养</option>
                            <option value="pending" th:selected="${criteria.status == 'pending'}">待审核</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label class="form-label">名称</label>
                        <input type="text" name="name" class="form-control"
                               th:value="${criteria.name}" placeholder="搜索名称">
                    </div>
                    <div class="col-12 d-flex justify-content-end">
                        <button type="submit" class="btn btn-primary">筛选</button>
                        <a th:href="@{/pets}" class="btn btn-outline-secondary ms-2">重置</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Pet Cards -->
        <div class="row" th:if="${!petPage.records.isEmpty()}">
            <div class="col-12 col-md-6 col-lg-4 col-xl-3 mb-3"
                 th:each="pet : ${petPage.records}">
                <div class="card h-100 shadow-sm">
                    <div class="position-relative">
                        <img th:src="${pet.coverImageUrl != null ? pet.coverImageUrl : 'https://placehold.co/400x300/e9ecef/6c757d?text=No+Image'}"
                             class="card-img-top" alt="封面"
                             style="height: 200px; object-fit: cover;">
                        <span class="badge position-absolute top-0 end-0 m-2"
                              th:classappend="${pet.status == 'available'} ? 'bg-success' : (${pet.status == 'pending'} ? 'bg-warning' : 'bg-secondary')"
                              th:text="${pet.status == 'available'} ? '可领养' : (${pet.status == 'pending'} ? '待审核' : '已领养')">可领养</span>
                    </div>
                    <div class="card-body">
                        <h6 class="card-title" th:text="${pet.name}">宠物名</h6>
                        <p class="card-text small text-muted mb-1">
                            <span th:text="${pet.breedName}">品种</span>
                            <span class="mx-1">|</span>
                            <span th:text="${pet.gender}">性别</span>
                            <span class="mx-1">|</span>
                            <span th:text="${pet.age} + '岁'">年龄</span>
                        </p>
                        <a th:href="@{/pets/{id}(id=${pet.id})}" class="btn btn-outline-primary btn-sm w-100">查看详情</a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Empty State -->
        <div class="text-center py-5" th:unless="${!petPage.records.isEmpty()}">
            <p class="text-muted fs-5">暂无宠物信息</p>
            <a th:href="@{/pets}" class="btn btn-outline-primary">清除筛选</a>
        </div>

        <!-- Pagination -->
        <div th:replace="~{fragments/pagination :: pagination(${petPage}, '/pets')}"></div>
    </div>

    <div th:replace="~{fragments/footer :: footer}"></div>
</body>
</html>
```

---

### Task 6: Pet Detail Page

**Files:**
- Create: `src/main/resources/templates/pet/detail.html`

- [ ] **Step 1: Create pet detail page with info and image gallery**

```html
<!DOCTYPE html>
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head th:replace="~{fragments/header :: head(${title})}"></head>
<body>
    <div th:replace="~{fragments/nav :: nav}"></div>

    <div class="container mt-4">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="/">首页</a></li>
                <li class="breadcrumb-item"><a href="/pets">宠物列表</a></li>
                <li class="breadcrumb-item active" th:text="${pet.name}">宠物名</li>
            </ol>
        </nav>

        <div class="row">
            <!-- Image Gallery -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-body text-center">
                        <img id="mainImage"
                             th:src="${images != null && !images.isEmpty() ? images[0].imageUrl : 'https://placehold.co/600x400/e9ecef/6c757d?text=No+Image'}"
                             class="img-fluid rounded mb-3" alt="宠物图片"
                             style="max-height: 400px; object-fit: contain; cursor: pointer;"
                             data-bs-toggle="modal" data-bs-target="#imageModal">
                    </div>
                </div>
                <!-- Thumbnails -->
                <div class="d-flex flex-wrap gap-2 mt-2" th:if="${images != null && images.size() > 1}">
                    <img th:each="image, iter : ${images}"
                         th:src="${image.imageUrl}"
                         class="img-thumbnail" alt="缩略图"
                         style="width: 80px; height: 80px; object-fit: cover; cursor: pointer;"
                         th:classappend="${iter.index == 0} ? 'border-primary' : ''"
                         onclick="document.getElementById('mainImage').src = this.src">
                </div>
            </div>

            <!-- Pet Info -->
            <div class="col-lg-6">
                <div class="card">
                    <div class="card-body">
                        <h3 class="card-title" th:text="${pet.name}">宠物名</h3>
                        <span class="badge fs-6 mb-3"
                              th:classappend="${pet.status == 'available'} ? 'bg-success' : (${pet.status == 'pending'} ? 'bg-warning' : 'bg-secondary')"
                              th:text="${pet.status == 'available'} ? '可领养' : (${pet.status == 'pending'} ? '待审核' : '已领养')">可领养</span>

                        <table class="table table-bordered mt-3">
                            <tr><th class="w-25">品种</th><td th:text="${pet.breedName}">-</td></tr>
                            <tr><th>性别</th><td th:text="${pet.gender}">-</td></tr>
                            <tr><th>年龄</th><td th:text="${pet.age} + ' 岁'">-</td></tr>
                            <tr><th>体重</th><td th:text="${pet.weight != null ? pet.weight + ' kg' : '-'}">-</td></tr>
                            <tr><th>健康状况</th><td th:text="${pet.healthStatus}">-</td></tr>
                            <tr><th>疫苗情况</th><td th:text="${pet.vaccineStatus ?: '-'}">-</td></tr>
                            <tr><th>绝育情况</th><td th:text="${pet.sterilizationStatus ?: '-'}">-</td></tr>
                            <tr><th>性格特点</th><td th:text="${pet.personality}">-</td></tr>
                            <tr><th>领养要求</th><td th:text="${pet.adoptionRequirement ?: '-'}">-</td></tr>
                        </table>

                        <!-- Action Buttons -->
                        <div class="d-grid gap-2 mt-3">
                            <button class="btn btn-success btn-lg"
                                    th:if="${pet.status == 'available'}"
                                    th:onclick="'location.href=\'/user/applications/apply?petId=' + ${pet.id} + '\''"
                                    sec:authorize="isAuthenticated()">
                                申请领养
                            </button>
                            <a th:href="@{/login}"
                               class="btn btn-success btn-lg"
                               th:if="${pet.status == 'available'}"
                               sec:authorize="!isAuthenticated()">
                                登录后申请领养
                            </a>
                            <button class="btn btn-secondary btn-lg" disabled
                                    th:if="${pet.status != 'available'}">
                                <span th:text="${pet.status == 'pending'} ? '该宠物审核中' : '该宠物已被领养'"></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Image Modal -->
    <div class="modal fade" id="imageModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" th:text="${pet.name}">宠物名</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body text-center">
                    <img id="modalImage" src="" class="img-fluid" alt="宠物图片">
                </div>
            </div>
        </div>
    </div>

    <div th:replace="~{fragments/footer :: footer}"></div>

    <script>
        // sync modal image with main image on open
        document.getElementById('imageModal').addEventListener('show.bs.modal', function() {
            document.getElementById('modalImage').src = document.getElementById('mainImage').src;
        });
    </script>
</body>
</html>
```

---

### Task 7: Update HomeController — add latest pets

**Files:**
- Modify: `src/main/java/com/petmgt/controller/HomeController.java`

- [ ] **Step 1: Inject PetService and pass latest pets to view**

Replace the entire file:

```java
package com.petmgt.controller;

import com.petmgt.entity.Pet;
import com.petmgt.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PetService petService;

    public HomeController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Pet> latestPets = petService.findLatestPets(8);
        model.addAttribute("title", "首页");
        model.addAttribute("latestPets", latestPets);
        return "home";
    }
}
```

---

### Task 8: Update home.html — show pet cards

**Files:**
- Modify: `src/main/resources/templates/home.html`

- [ ] **Step 1: Replace the static card section with dynamic pet cards, keep hero**

Replace lines 17–45 (the `<div class="row mt-5">` block) with pet cards:

```html
        <div class="row mt-5" th:if="${!latestPets.isEmpty()}">
            <div class="col-12">
                <h3 class="mb-3">最新可领养宠物</h3>
            </div>
            <div class="col-12 col-md-6 col-lg-3 mb-3"
                 th:each="pet : ${latestPets}">
                <div class="card h-100 shadow-sm">
                    <img th:src="${pet.coverImageUrl != null ? pet.coverImageUrl : 'https://placehold.co/400x300/e9ecef/6c757d?text=No+Image'}"
                         class="card-img-top" alt="封面"
                         style="height: 200px; object-fit: cover;">
                    <div class="card-body">
                        <h6 class="card-title" th:text="${pet.name}">宠物名</h6>
                        <p class="card-text small text-muted mb-1">
                            <span th:text="${pet.breedName}">品种</span>
                            <span class="mx-1">|</span>
                            <span th:text="${pet.gender}">性别</span>
                            <span class="mx-1">|</span>
                            <span th:text="${pet.age} + '岁'">年龄</span>
                        </p>
                        <a th:href="@{/pets/{id}(id=${pet.id})}" class="btn btn-outline-primary btn-sm w-100">查看详情</a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Fallback when no pets -->
        <div class="row mt-5" th:unless="${!latestPets.isEmpty()}">
            <div class="col-12">
                <h3>快速入口</h3>
            </div>
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body text-center">
                        <h5 class="card-title">浏览宠物</h5>
                        <p class="card-text">查看所有待领养宠物信息</p>
                        <a href="/pets" class="btn btn-outline-primary">去看看</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body text-center">
                        <h5 class="card-title">AI 匹配</h5>
                        <p class="card-text">智能推荐最适合你的宠物</p>
                        <a href="/user/ai-match" class="btn btn-outline-primary">去匹配</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card">
                    <div class="card-body text-center">
                        <h5 class="card-title">养宠问答</h5>
                        <p class="card-text">AI 助手解答你的养宠疑问</p>
                        <a href="/user/ai-chat" class="btn btn-outline-primary">去提问</a>
                    </div>
                </div>
            </div>
        </div>
```

Note: The hero section (lines 8–15 of the original) remains unchanged. Only the `<div class="row mt-5">` block (lines 17–45) is replaced.

---

### Task 9: Build verification

- [ ] **Step 1: Build the project**

```bash
cd d:\code\pet-mgt && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Verify all produced files exist**

```bash
ls src/main/java/com/petmgt/dto/PetSearchCriteria.java
ls src/main/java/com/petmgt/service/PetService.java
ls src/main/java/com/petmgt/controller/PetController.java
ls src/main/resources/templates/pet/list.html
ls src/main/resources/templates/pet/detail.html
ls src/main/resources/templates/fragments/pagination.html
```

---

## P1 验证清单

- [ ] `/pets` 宠物列表正常分页（有宠物数据时）
- [ ] 筛选条件全部生效（类型、品种、性别、状态、名称）
- [ ] 手机端单列、平板双列、桌面多列（Bootstrap grid）
- [ ] 宠物详情页 `/pets/{id}` 信息完整
- [ ] 图片点击可放大（Modal）
- [ ] 未登录点"申请领养"跳转登录
- [ ] 首页有最新宠物卡片展示（有数据时）
- [ ] 首页无宠物时显示快速入口兜底
- [ ] 项目编译无错误

## 依赖关系

```
Task 1 (PetSearchCriteria) ──→ Task 2 (PetService) ──→ Task 3 (PetController)
                                                           │
                              Task 4 (pagination) ←────────┤
                              Task 5 (list.html) ←─────────┤
                              Task 6 (detail.html) ←───────┤
                                                        Task 3
                              Task 2 ──→ Task 7 (HomeController)
                              Task 2 ──→ Task 8 (home.html)
```

Tasks 4, 5, 6 can be created in parallel after Task 3. Tasks 7 and 8 can be done in parallel after Task 2.
