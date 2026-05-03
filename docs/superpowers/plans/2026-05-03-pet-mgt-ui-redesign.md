# Pet-Mgt UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform pet-mgt from raw Bootstrap 5.3 to a playful, premium pet adoption platform with "berry pop" color scheme (purple/pink/gold gradient), card hover animations, and modern design system.

**Architecture:** One custom CSS file (`pet-mgt.css`) overrides Bootstrap 5.3 defaults via CSS variables, class selectors, and utility additions. Templates get minimal class changes — CSS handles 80% of the visual upgrade. Hero section on home page is the only significant HTML restructure.

**Tech Stack:** Spring Boot + Thymeleaf + Bootstrap 5.3 CDN + custom CSS

---

### File Map

| File | Responsibility | Action |
|------|---------------|--------|
| `src/main/resources/static/css/pet-mgt.css` | Custom design system: CSS variables, Bootstrap overrides, animations, component styles | **Create** |
| `src/main/resources/templates/fragments/header.html` | Meta tags + CSS/JS imports | **Modify** — add 1 `<link>` |
| `src/main/resources/templates/fragments/nav.html` | Navigation bar | **Modify** — restyle navbar |
| `src/main/resources/templates/fragments/footer.html` | Footer | **Modify** — restyle footer |
| `src/main/resources/templates/fragments/pagination.html` | Pagination component | **Modify** — restyle pagination |
| `src/main/resources/templates/home.html` | Home page with hero, quick links, latest pets | **Modify** — new hero section, card classes |
| `src/main/resources/templates/pet/list.html` | Pet listing with filters | **Modify** — filter card, pet cards, empty state |
| `src/main/resources/templates/pet/detail.html` | Pet detail with gallery | **Modify** — gallery, info table, buttons |
| `src/main/resources/templates/auth/login.html` | Login form | **Modify** — form card styling |
| `src/main/resources/templates/auth/register.html` | Registration form | **Modify** — form card styling |
| `src/main/resources/templates/user/profile.html` | User profile | **Modify** — profile cards |
| `src/main/resources/templates/user/application-form.html` | Adoption application | **Modify** — form card styling |
| `src/main/resources/templates/user/applications.html` | User applications list | **Modify** — table, badges |
| `src/main/resources/templates/user/ai-chat.html` | AI chat page | **Modify** — chat card styling |
| `src/main/resources/templates/user/ai-match.html` | AI match form | **Modify** — form styling |
| `src/main/resources/templates/user/ai-match-result.html` | AI match results | **Modify** — result cards |
| `src/main/resources/templates/user/ai-match-history.html` | AI match history | **Modify** — table, badges |
| `src/main/resources/templates/error.html` | 500 error page | **Modify** — styling |
| `src/main/resources/templates/404.html` | 404 error page | **Modify** — styling |

---

### Task 1: Create the custom CSS file

**Files:**
- Create: `src/main/resources/static/css/pet-mgt.css`

- [ ] **Step 1: Write pet-mgt.css with full design system**

```css
/* ============================================
   pet-mgt — 浆果跳跳糖 Design System
   Overrides Bootstrap 5.3 for playful pet-adoption UI
   ============================================ */

/* ----- CSS Variables ----- */
:root {
  --pet-primary: #7C3AED;
  --pet-primary-light: #A855F7;
  --pet-accent: #F472B6;
  --pet-accent-light: #F9A8D4;
  --pet-gold: #FBBF24;
  --pet-dark: #1E1B4B;
  --pet-text: #334155;
  --pet-muted: #94A3B8;
  --pet-bg: #F8FAFC;
  --pet-bg-card: #FFFFFF;
  --pet-success: #10B981;
  --pet-success-bg: #ECFDF5;
  --pet-warning: #F59E0B;
  --pet-warning-bg: #FFFBEB;
  --pet-gradient: linear-gradient(135deg, #7C3AED 0%, #F472B6 50%, #FBBF24 100%);
  --pet-gradient-nav: linear-gradient(135deg, #7C3AED, #A855F7);
  --pet-gradient-hero: linear-gradient(135deg, #7C3AED 0%, #F472B6 50%, #FBBF24 100%);
  --pet-radius-sm: 8px;
  --pet-radius: 12px;
  --pet-radius-lg: 16px;
  --pet-radius-xl: 24px;
  --pet-radius-full: 9999px;
  --pet-shadow-sm: 0 2px 8px rgba(124,58,237,0.08);
  --pet-shadow: 0 4px 20px rgba(124,58,237,0.12);
  --pet-shadow-lg: 0 8px 32px rgba(124,58,237,0.16);
  --pet-transition: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ----- Global Overrides ----- */
body {
  background: var(--pet-bg);
  color: var(--pet-text);
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", "Noto Sans", sans-serif;
}

h1, h2, h3, h4, h5, h6, .h1, .h2, .h3, .h4, .h5, .h6 {
  color: var(--pet-dark);
  font-weight: 700;
}

h1, .h1, .display-4 { font-weight: 800; letter-spacing: -0.02em; }
h2, .h2 { font-weight: 700; }
h3, .h3, .card-title { font-weight: 700; }
.display-1 { font-weight: 800; }

a { color: var(--pet-primary); transition: var(--pet-transition); }
a:hover { color: var(--pet-accent); }

.text-muted { color: var(--pet-muted) !important; }

/* ----- Navbar ----- */
.navbar {
  background: var(--pet-gradient-nav) !important;
  box-shadow: 0 4px 20px rgba(124,58,237,0.25);
  padding: 0.5rem 0;
}

.navbar-brand {
  font-weight: 800;
  font-size: 1.25rem;
  letter-spacing: -0.02em;
}

.navbar-dark .nav-link {
  font-weight: 500;
  opacity: 0.85;
  transition: opacity var(--pet-transition), background var(--pet-transition);
  border-radius: var(--pet-radius-full);
  padding: 0.4rem 1rem;
  position: relative;
}

.navbar-dark .nav-link:hover {
  opacity: 1;
  background: rgba(255,255,255,0.12);
}

.navbar-dark .nav-link.active {
  opacity: 1;
  background: rgba(255,255,255,0.18);
  font-weight: 600;
}

/* Nav link underline on hover */
.navbar-nav .nav-link::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--pet-gold);
  border-radius: 2px;
  transition: width var(--pet-transition), left var(--pet-transition);
}

.navbar-nav .nav-link:hover::after,
.navbar-nav .nav-link.active::after {
  width: 60%;
  left: 20%;
}

.navbar-toggler { border: none; }

.dropdown-menu {
  border: none;
  border-radius: var(--pet-radius);
  box-shadow: var(--pet-shadow-lg);
  overflow: hidden;
}

/* ----- Footer ----- */
footer {
  background: white !important;
  border-top: 1px solid rgba(0,0,0,0.05);
  padding: 1.5rem 0 !important;
  color: var(--pet-muted) !important;
}

/* ----- Hero Section (home page) ----- */
.hero-section {
  background: var(--pet-gradient-hero);
  border-radius: var(--pet-radius-lg);
  padding: 3.5rem 2rem;
  text-align: center;
  color: white;
  position: relative;
  overflow: hidden;
  margin-bottom: 2rem;
}

.hero-section::before {
  content: '🐱';
  position: absolute;
  top: -30px;
  left: -20px;
  font-size: 100px;
  opacity: 0.12;
  animation: floatEmoji 6s ease-in-out infinite;
}

.hero-section::after {
  content: '🐶';
  position: absolute;
  bottom: -15px;
  right: -15px;
  font-size: 120px;
  opacity: 0.12;
  animation: floatEmoji 6s ease-in-out 2s infinite;
}

@keyframes floatEmoji {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(5deg); }
}

.hero-title {
  font-size: 2.25rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  position: relative;
  z-index: 1;
  letter-spacing: -0.03em;
}

.hero-subtitle {
  font-size: 1.05rem;
  opacity: 0.9;
  margin-bottom: 1.75rem;
  position: relative;
  z-index: 1;
}

.hero-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  position: relative;
  z-index: 1;
  flex-wrap: wrap;
}

/* ----- Cards ----- */
.card {
  border: none;
  border-radius: var(--pet-radius-lg);
  box-shadow: var(--pet-shadow-sm);
  transition: transform var(--pet-transition), box-shadow var(--pet-transition);
  overflow: hidden;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--pet-shadow-lg);
}

.card-title { color: var(--pet-dark); }

.card-body { padding: 1.25rem; }

/* Cards used inside forms (no hover lift needed) */
.card.shadow-sm:hover,
.card.mb-4:hover {
  transform: none;
}

/* Quick-link category cards */
.category-card {
  border-radius: var(--pet-radius);
  padding: 1.25rem 1rem;
  text-align: center;
  border: none;
  transition: transform var(--pet-transition), box-shadow var(--pet-transition);
  cursor: pointer;
  text-decoration: none;
  display: block;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--pet-shadow);
}

.category-card .emoji { font-size: 2rem; }
.category-card .label { font-weight: 700; color: var(--pet-dark); margin-top: 0.5rem; }
.category-card .count { font-size: 0.8rem; color: var(--pet-muted); }

/* ----- Buttons ----- */
.btn {
  border-radius: var(--pet-radius-full);
  font-weight: 600;
  padding: 0.5rem 1.5rem;
  transition: all var(--pet-transition);
  border: none;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn:active {
  transform: translateY(0);
}

.btn-primary {
  background: var(--pet-gradient);
  border: none;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #8B5CF6, #F472B6);
  filter: brightness(1.05);
}

.btn-success {
  background: var(--pet-success);
  border: none;
}

.btn-success:hover {
  background: #059669;
}

.btn-outline-primary {
  color: var(--pet-primary);
  border: 2px solid var(--pet-primary);
  background: transparent;
}

.btn-outline-primary:hover {
  background: var(--pet-primary);
  color: white;
  border-color: var(--pet-primary);
}

.btn-outline-secondary {
  border: 2px solid #E2E8F0;
  color: var(--pet-text);
  background: transparent;
}

.btn-outline-secondary:hover {
  background: #F1F5F9;
  border-color: #CBD5E1;
}

.btn-outline-danger {
  border: 2px solid #FCA5A5;
  color: #DC2626;
  background: transparent;
}

.btn-outline-danger:hover {
  background: #DC2626;
  color: white;
  border-color: #DC2626;
}

.btn-danger {
  background: #EF4444;
  border: none;
}

.btn-danger:hover {
  background: #DC2626;
}

.btn-lg {
  padding: 0.75rem 2rem;
  font-size: 1rem;
}

.btn-sm {
  padding: 0.3rem 1rem;
  font-size: 0.8rem;
}

/* ----- Badges ----- */
.badge {
  border-radius: var(--pet-radius-full);
  padding: 0.3em 0.75em;
  font-weight: 600;
  font-size: 0.75rem;
}

.bg-success {
  background: var(--pet-success-bg) !important;
  color: var(--pet-success) !important;
}

.bg-warning {
  background: var(--pet-warning-bg) !important;
  color: #D97706 !important;
}

.bg-secondary {
  background: #F1F5F9 !important;
  color: #64748B !important;
}

.bg-primary {
  background: var(--pet-gradient) !important;
}

.bg-danger {
  background: #FEE2E2 !important;
  color: #DC2626 !important;
}

/* Pulse animation for "available" badge */
.badge-available {
  animation: pulseBadge 2s ease-in-out infinite;
}

@keyframes pulseBadge {
  0%, 100% { box-shadow: 0 0 0 0 rgba(16,185,129,0.4); }
  50% { box-shadow: 0 0 0 6px rgba(16,185,129,0); }
}

/* ----- Forms ----- */
.form-control, .form-select {
  border-radius: var(--pet-radius-sm);
  border: 1px solid #E2E8F0;
  padding: 0.55rem 0.9rem;
  transition: border-color var(--pet-transition), box-shadow var(--pet-transition);
}

.form-control:focus, .form-select:focus {
  border-color: var(--pet-primary);
  box-shadow: 0 0 0 3px rgba(124,58,237,0.12);
}

.form-label {
  font-weight: 600;
  color: var(--pet-dark);
  font-size: 0.85rem;
  margin-bottom: 0.35rem;
}

/* ----- Tables ----- */
.table {
  border-collapse: separate;
  border-spacing: 0;
}

.table th {
  color: var(--pet-muted);
  font-weight: 600;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #E2E8F0;
  padding: 0.75rem 1rem;
}

.table td {
  padding: 0.75rem 1rem;
  vertical-align: middle;
  border-bottom: 1px solid #F1F5F9;
}

.table-hover tbody tr:hover {
  background: #F8FAFC;
}

.table-bordered {
  border: 1px solid #E2E8F0;
  border-radius: var(--pet-radius);
  overflow: hidden;
}

.table-bordered th,
.table-bordered td {
  border-color: #E2E8F0;
}

.table-striped tbody tr:nth-of-type(odd) {
  background: #FAFBFC;
}

/* ----- Breadcrumb ----- */
.breadcrumb-item a {
  color: var(--pet-muted);
  text-decoration: none;
}

.breadcrumb-item a:hover {
  color: var(--pet-primary);
}

.breadcrumb-item.active {
  color: var(--pet-dark);
  font-weight: 600;
}

/* ----- Pagination ----- */
.pagination .page-link {
  border: none;
  border-radius: var(--pet-radius-sm);
  margin: 0 2px;
  color: var(--pet-text);
  font-weight: 500;
  transition: all var(--pet-transition);
}

.pagination .page-link:hover {
  background: #F1F5F9;
  color: var(--pet-primary);
}

.pagination .active .page-link {
  background: var(--pet-gradient);
  color: white;
}

.pagination .disabled .page-link {
  color: #CBD5E1;
}

/* ----- Alerts ----- */
.alert {
  border: none;
  border-radius: var(--pet-radius);
}

.alert-success {
  background: var(--pet-success-bg);
  color: #065F46;
}

.alert-danger {
  background: #FEE2E2;
  color: #991B1B;
}

.alert-warning {
  background: var(--pet-warning-bg);
  color: #92400E;
}

.alert-info {
  background: #EFF6FF;
  color: #1E40AF;
}

/* ----- Modals ----- */
.modal-content {
  border: none;
  border-radius: var(--pet-radius-lg);
  overflow: hidden;
}

/* ----- AI Chat ----- */
#answerArea .card {
  border: 1px solid #E2E8F0;
}

#answerArea .card-header {
  background: linear-gradient(135deg, #F5F3FF, #FDF2F8);
  border-bottom: 1px solid #E2E8F0;
  color: var(--pet-dark);
  font-weight: 600;
}

/* ----- Filter Card (pet list) ----- */
.filter-card .card-body {
  padding: 1.25rem;
}

/* ----- Image Gallery (pet detail) ----- */
.img-thumbnail {
  border-radius: var(--pet-radius-sm);
  border: 2px solid transparent;
  transition: border-color var(--pet-transition);
  cursor: pointer;
}

.img-thumbnail:hover {
  border-color: var(--pet-accent);
}

/* ----- Page load fade-in ----- */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}

/* Staggered animation delays for card grids */
.fade-in-up:nth-child(1) { animation-delay: 0.05s; }
.fade-in-up:nth-child(2) { animation-delay: 0.1s; }
.fade-in-up:nth-child(3) { animation-delay: 0.15s; }
.fade-in-up:nth-child(4) { animation-delay: 0.2s; }
.fade-in-up:nth-child(5) { animation-delay: 0.25s; }
.fade-in-up:nth-child(6) { animation-delay: 0.3s; }
.fade-in-up:nth-child(7) { animation-delay: 0.35s; }
.fade-in-up:nth-child(8) { animation-delay: 0.4s; }

/* ----- Empty State ----- */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-state .icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.4;
}

/* ----- Responsive Tweaks ----- */
@media (max-width: 768px) {
  .hero-section {
    padding: 2rem 1rem;
  }
  .hero-title {
    font-size: 1.6rem;
  }
  .navbar-brand {
    font-size: 1.1rem;
  }
}
```

- [ ] **Step 2: Verify the CSS file was created**

```bash
ls -la src/main/resources/static/css/pet-mgt.css
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/css/pet-mgt.css
git commit -m "feat: add custom pet-mgt.css with berry pop design system"
```

---

### Task 2: Include CSS in the header fragment

**Files:**
- Modify: `src/main/resources/templates/fragments/header.html`

- [ ] **Step 1: Add the CSS link after the Bootstrap CDN**

Replace:
```html
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
```

With:
```html
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="/css/pet-mgt.css" rel="stylesheet">
```

- [ ] **Step 2: Verify the change**

Read `src/main/resources/templates/fragments/header.html` and confirm the new line is present after the Bootstrap CDN link.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/fragments/header.html
git commit -m "feat: include pet-mgt.css in header fragment"
```

---

### Task 3: Restyle navigation bar

**Files:**
- Modify: `src/main/resources/templates/fragments/nav.html`

- [ ] **Step 1: Update navbar classes for gradient styling**

Replace:
```html
<nav th:fragment="nav" class="navbar navbar-expand-lg navbar-dark bg-primary"
```

With:
```html
<nav th:fragment="nav" class="navbar navbar-expand-lg navbar-dark"
```

Note: Removing `bg-primary` — the CSS `.navbar` rule already applies the gradient background.

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/templates/fragments/nav.html
git commit -m "feat: update navbar to use gradient background"
```

---

### Task 4: Restyle footer

**Files:**
- Modify: `src/main/resources/templates/fragments/footer.html`

- [ ] **Step 1: Update footer classes**

Replace:
```html
<footer th:fragment="footer" class="bg-light mt-5 py-3 text-center text-muted">
```

With:
```html
<footer th:fragment="footer" class="mt-5 py-3 text-center text-muted">
```

Note: Removing `bg-light` — the CSS `footer` rule handles background.

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/templates/fragments/footer.html
git commit -m "feat: restyle footer to white background"
```

---

### Task 5: Redesign home page (hero + quick links + latest pets)

**Files:**
- Modify: `src/main/resources/templates/home.html`

- [ ] **Step 1: Replace the hero section and latest pets section**

Replace the content inside `<div class="container mt-4">`:

```html
<div class="container mt-4">
    <!-- Hero Section -->
    <div class="hero-section">
        <div class="hero-title">🐾 找到你一生的伙伴</div>
        <div class="hero-subtitle">智能匹配 · 一键申请 · 给流浪动物一个温暖的家</div>
        <div class="hero-buttons">
            <a href="/pets" class="btn btn-light btn-lg" style="color: var(--pet-primary); font-weight: 700; border-radius: var(--pet-radius-full);">
                🐾 浏览宠物
            </a>
            <a href="/user/ai-match" class="btn btn-lg" style="background: rgba(255,255,255,0.2); color: white; border-radius: var(--pet-radius-full); font-weight: 600;" sec:authorize="isAuthenticated()">
                ✨ AI 匹配
            </a>
        </div>
    </div>

    <!-- Latest Pets -->
    <div th:if="${!latestPets.isEmpty()}">
        <h3 class="mb-3">最新可领养宠物</h3>
        <div class="row">
            <div class="col-12 col-md-6 col-lg-3 mb-3 fade-in-up"
                 th:each="pet : ${latestPets}">
                <div class="card h-100">
                    <div class="position-relative">
                        <img th:src="${pet.coverImageUrl != null ? pet.coverImageUrl : 'https://placehold.co/400x300/e9ecef/6c757d?text=No+Image'}"
                             class="card-img-top" alt="封面"
                             style="height: 200px; object-fit: cover;">
                        <span class="badge position-absolute top-0 end-0 m-2"
                              th:classappend="${pet.status == 'available' ? 'badge-available' : ''}"
                              th:with="statusClass=${pet.status == 'available' ? 'bg-success' : (pet.status == 'pending' ? 'bg-warning' : 'bg-secondary')}"
                              th:class="${statusClass}"
                              th:text="${pet.status == 'available' ? '可领养' : (pet.status == 'pending' ? '待审核' : '已领养')}">可领养</span>
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
    </div>

    <!-- Quick Links (fallback when no pets) -->
    <div th:unless="${!latestPets.isEmpty()}">
        <h3>快速入口</h3>
        <div class="row mt-3">
            <div class="col-md-4 mb-3">
                <a href="/pets" class="card category-card" style="background: #F5F3FF;">
                    <div class="emoji">🐾</div>
                    <div class="label">浏览宠物</div>
                    <div class="count">查看所有待领养宠物</div>
                </a>
            </div>
            <div class="col-md-4 mb-3">
                <a href="/user/ai-match" class="card category-card" style="background: #FDF2F8;">
                    <div class="emoji">✨</div>
                    <div class="label">AI 匹配</div>
                    <div class="count">智能推荐最适合你的宠物</div>
                </a>
            </div>
            <div class="col-md-4 mb-3">
                <a href="/user/ai-chat" class="card category-card" style="background: #FEFCE8;">
                    <div class="emoji">💬</div>
                    <div class="label">养宠问答</div>
                    <div class="count">AI 助手解答你的养宠疑问</div>
                </a>
            </div>
        </div>
    </div>
</div>
```

- [ ] **Step 2: Verify the file structure**

Read `src/main/resources/templates/home.html` and confirm the hero section and new card structures are present.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/home.html
git commit -m "feat: redesign home page with gradient hero and category cards"
```

---

### Task 6: Restyle pet list page

**Files:**
- Modify: `src/main/resources/templates/pet/list.html`

- [ ] **Step 1: Update filter card and pet cards**

Apply these edits in sequence:

1. Add `class="filter-card"` to the filter `<div class="card mb-4">`:
```html
<div class="card mb-4 filter-card">
```

2. Replace the pet card div wrapping each pet (the `shadow-sm` card) with the `fade-in-up` variant and add `badge-available` to the status badge:
```html
<div class="col-12 col-md-6 col-lg-4 col-xl-3 mb-3 fade-in-up"
     th:each="pet : ${petPage.records}">
    <div class="card h-100">
        <div class="position-relative">
            <img th:src="${pet.coverImageUrl != null ? pet.coverImageUrl : 'https://placehold.co/400x300/e9ecef/6c757d?text=No+Image'}"
                 class="card-img-top" alt="封面"
                 style="height: 200px; object-fit: cover;">
            <span class="badge position-absolute top-0 end-0 m-2"
                  th:classappend="${pet.status == 'available' ? 'badge-available' : ''}"
                  th:with="statusClass=${pet.status == 'available' ? 'bg-success' : (pet.status == 'pending' ? 'bg-warning' : 'bg-secondary')}"
                  th:class="${statusClass}"
                  th:text="${pet.status == 'available' ? '可领养' : (pet.status == 'pending' ? '待审核' : '已领养')}">可领养</span>
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
```

3. Update the empty state to use the `.empty-state` class:
```html
<div class="empty-state" th:unless="${!petPage.records.isEmpty()}">
    <div class="icon">🐾</div>
    <p class="text-muted fs-5">暂无宠物信息</p>
    <a th:href="@{/pets}" class="btn btn-outline-primary">清除筛选</a>
</div>
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/templates/pet/list.html
git commit -m "feat: restyle pet list page with hover cards and animations"
```

---

### Task 7: Restyle pet detail page

**Files:**
- Modify: `src/main/resources/templates/pet/detail.html`

- [ ] **Step 1: Update image gallery card and info card**

1. Add `shadow-sm` class removal from image gallery card (CSS handles card styling globally):

Replace:
```html
<div class="card">
    <div class="card-body text-center">
```

With (no change needed — CSS handles it). Keep as-is.

2. Update the status badge:
```html
<span class="badge fs-6 mb-3"
      th:classappend="${pet.status == 'available' ? 'badge-available' : ''}"
      th:with="statusClass=${pet.status == 'available' ? 'bg-success' : (pet.status == 'pending' ? 'bg-warning' : 'bg-secondary')}"
      th:class="${statusClass}"
      th:text="${pet.status == 'available' ? '可领养' : (pet.status == 'pending' ? '待审核' : '已领养')}">可领养</span>
```

The existing badge code is already similar. Replace the old badge span lines 45-48 with the above.

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/templates/pet/detail.html
git commit -m "feat: restyle pet detail page badges"
```

---

### Task 8: Restyle login and register pages

**Files:**
- Modify: `src/main/resources/templates/auth/login.html`
- Modify: `src/main/resources/templates/auth/register.html`

- [ ] **Step 1: Update login page form card**

Replace `class="card shadow"` with just `class="card"`:

In login.html:
```html
<div class="card">
```

And replace the button class from `btn-primary` to keep as-is (CSS handles it).

- [ ] **Step 2: Update register page form card**

Same change in register.html:
```html
<div class="card">
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/auth/login.html src/main/resources/templates/auth/register.html
git commit -m "feat: restyle login and register page cards"
```

---

### Task 9: Restyle user pages (profile, applications, application form)

**Files:**
- Modify: `src/main/resources/templates/user/profile.html`
- Modify: `src/main/resources/templates/user/application-form.html`
- Modify: `src/main/resources/templates/user/applications.html`

- [ ] **Step 1: Update profile page**

Replace `class="card shadow"` with `class="card"` in both profile cards (lines 22 and 47).

- [ ] **Step 2: Update application form page**

Replace `class="card shadow"` with `class="card"` in the application form card (line 25).

- [ ] **Step 3: Update applications list**

Replace the badge logic on line 48 to use new badge colors:

The existing inline `th:class` with `bg-warning text-dark`, `bg-success`, `bg-danger` etc is fine — the CSS overrides for `.bg-success`, `.bg-warning`, `.bg-danger` handle the new styling. No HTML change needed for badges.

Update empty state:
```html
<div th:if="${page.records == null || page.records.isEmpty()}" class="empty-state">
    <div class="icon">📋</div>
    <p class="text-muted fs-4">暂无申请记录</p>
    <a href="/pets" class="btn btn-outline-primary">去看看待领养宠物</a>
</div>
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/templates/user/profile.html src/main/resources/templates/user/application-form.html src/main/resources/templates/user/applications.html
git commit -m "feat: restyle user profile, application form, and applications pages"
```

---

### Task 10: Restyle AI pages

**Files:**
- Modify: `src/main/resources/templates/user/ai-chat.html`
- Modify: `src/main/resources/templates/user/ai-match.html`
- Modify: `src/main/resources/templates/user/ai-match-result.html`
- Modify: `src/main/resources/templates/user/ai-match-history.html`

- [ ] **Step 1: Update AI chat page**

No HTML changes needed — the CSS rules for `#answerArea .card` and `#answerArea .card-header` handle the styling. The buttons and form elements are already covered by global CSS.

- [ ] **Step 2: Update AI match form page**

No HTML changes needed. Form elements and buttons are styled by global CSS overrides.

- [ ] **Step 3: Update AI match result page**

No structural changes needed. The cards and badges get styled by global CSS overrides.

- [ ] **Step 4: Update AI match history page**

No HTML changes needed. Tables and badges are covered by global CSS.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/user/ai-chat.html src/main/resources/templates/user/ai-match.html src/main/resources/templates/user/ai-match-result.html src/main/resources/templates/user/ai-match-history.html
git commit -m "feat: confirm AI pages styled by global CSS overrides"
```

---

### Task 11: Restyle pagination fragment

**Files:**
- Modify: `src/main/resources/templates/fragments/pagination.html`

- [ ] **Step 1: No changes needed**

The `pagination` component gets styled entirely by the CSS rules for `.pagination .page-link`, `.pagination .active .page-link`, etc. No HTML changes required.

- [ ] **Step 2: Commit** (skip if no changes)

---

### Task 12: Restyle error pages (404 and 500)

**Files:**
- Modify: `src/main/resources/templates/404.html`
- Modify: `src/main/resources/templates/error.html`

- [ ] **Step 1: Update error page styling**

In both files, replace:
```html
<h1 class="display-1 text-muted">404</h1>
```
and
```html
<h1 class="display-1 text-muted">500</h1>
```

With:
```html
<h1 class="display-1" style="color: var(--pet-primary); opacity: 0.3;">404</h1>
<h1 class="display-1" style="color: var(--pet-primary); opacity: 0.3;">500</h1>
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/templates/404.html src/main/resources/templates/error.html
git commit -m "feat: restyle error pages with brand color"
```

---

### Task 13: Build and verify

**Files:** None (verification only)

- [ ] **Step 1: Build the project**

```bash
cd d:/code/pet-mgt && mvn compile -q
```

Expected: BUILD SUCCESS with no errors.

- [ ] **Step 2: Start the application**

```bash
cd d:/code/pet-mgt && mvn spring-boot:run
```

- [ ] **Step 3: Verify visually**

Open http://localhost:8080 in a browser and check:
- [ ] Home page: gradient hero with floating emoji, category cards, latest pet cards with hover effect
- [ ] Pet list: filter card, pet cards with status badges, pagination
- [ ] Login page: centered form card with brand styling
- [ ] Nav bar: purple gradient, underline hover effect on links
- [ ] Footer: clean white with subtle border

- [ ] **Step 4: Stop the server** (Ctrl+C)

- [ ] **Step 5: Final commit if any fixes were needed**

---

### Post-Implementation Notes

- If the build fails with a compilation error, check that no unintended changes were made to Java files
- If the CSS doesn't seem to take effect, verify the `href="/css/pet-mgt.css"` path is correct
- Admin templates (`admin/*.html`) get global styling from the CSS but were not individually reworked (per spec: lower priority)
- To add real pet photos later, use the existing admin upload feature — no UI changes needed
