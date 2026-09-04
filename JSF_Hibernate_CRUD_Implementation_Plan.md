# Implementation Plan: User & Task CRUD (JSF + Hibernate)
### Wedding Plan Management System — Course Assignment

**Chosen entities:** `User` and `Task` — with a real one-to-many relationship (`User` 1 → many `Task`, via `AssignedUserID`)
**Requirements to satisfy:** Full CRUD on both entities · 3 types of validation · CSS (internal, external, inline)

> Why this pair works well: `Task.AssignedUserID` maps directly onto `User.UserID` in the diagram, so unlike most 2-entity subsets of this schema, you get a genuine `@ManyToOne`/`@OneToMany` Hibernate relationship to show — a user is assigned many tasks, a task belongs to one user. `EventID` on `Task` stays a plain `int` column since `WeddingEvent` isn't one of your two entities.

---

## Phase 0 — Planning & Entity Simplification (0.5 day)

- [ ] Decide the exact field list you'll implement (trim anything not needed to demonstrate CRUD/validation):
  - **User:** UserID (PK), Name, Email, Role (enum: Bride/Groom/FamilyMember), Side (enum: Bride/Groom)
  - **Task:** TaskID (PK), EventID (plain int, no FK), AssignedUser (real `@ManyToOne` → `User`), Title, Deadline (date), Status (enum: Pending/InProgress/Completed), Category
- [ ] Confirm the relationship direction: model it as **unidirectional `@ManyToOne`** on `Task` (Task holds the FK) to keep things simple — you don't strictly need a `@OneToMany` list back on `User` unless you want to show "all tasks for this user" on the User detail page (optional stretch goal, see Phase 5).
- [ ] Sketch the 3 validation types you'll apply to each field now, so form design in Phase 5 matches (see Phase 6 table).
- [ ] Choose your DB: MySQL or PostgreSQL.

**Deliverable:** a one-page field/validation matrix.

---

## Phase 1 — Environment & Project Setup (0.5–1 day)

- [ ] Install: JDK 11+, Apache Maven, a Java EE app server (WildFly or GlassFish/Payara), MySQL Server + Workbench.
- [ ] Create a Maven **Web Application** project (`war` packaging).
- [ ] Add dependencies to `pom.xml`:
  - `javax.faces` (Mojarra or MyFaces) — usually `provided` scope from the app server
  - `hibernate-core`
  - `mysql-connector-java`
  - `javax.validation:validation-api` + `hibernate-validator` (Bean Validation)
- [ ] Create the database schema (`wedding_plan_db`) — let Hibernate auto-generate tables (`hbm2ddl.auto=update`) or hand-write DDL first and use `validate`.
- [ ] Confirm the server starts and deploys an empty WAR before writing business logic.

**Deliverable:** empty project deploys successfully; DB connection confirmed.

---

## Phase 2 — Data Model: Entity Classes (0.5–1 day)

- [ ] Create `User.java` — `@Entity`, `@Id @GeneratedValue`, `@Enumerated(EnumType.STRING)` for `Role` and `Side`.
- [ ] Create `Task.java` — `@Entity`, `@Id @GeneratedValue`, `@Enumerated(EnumType.STRING)` for `Status`, plain `int EventID` column, and:
  ```java
  @ManyToOne
  @JoinColumn(name = "assigned_user_id")
  private User assignedUser;
  ```
  This is the FK relationship — Hibernate will create an `assigned_user_id` column on the `task` table referencing `user.id`.
- [ ] Add **Bean Validation annotations directly on the entity fields** — this is validation type #1 (Phase 6):
  - `User`: `@NotBlank` on `Name`, `@Email` (or `@Pattern`) on `Email`, `@NotNull` on `Role`/`Side`
  - `Task`: `@NotBlank @Size(min=2,max=100)` on `Title`, `@FutureOrPresent` on `Deadline`, `@NotNull` on `Status` and `assignedUser`
- [ ] Write `hibernate.cfg.xml` (or `persistence.xml`) with connection settings and both entity mappings.
- [ ] Write a `HibernateUtil` singleton `SessionFactory`.

**Deliverable:** two mapped entities with a real FK between them; Hibernate creates `user` and `task` tables (task with `assigned_user_id` FK column) on startup.

---

## Phase 3 — Data Access Layer (DAO) (1 day)

- [ ] `UserDAO` / `TaskDAO` with full CRUD: `create()`, `findById()`, `findAll()`, `update()`, `delete()`.
- [ ] `UserDAO` needs one extra read method: `findAll()` is reused by `TaskBean` to populate the "Assign to" dropdown when creating/editing a Task.
- [ ] `TaskDAO.delete()` — decide the FK behavior: since `Task.assignedUser` is required (`@NotNull`), deleting a `User` who has assigned tasks will fail on a DB constraint unless you handle it. **Recommendation for this assignment:** just prevent deleting a `User` that still has tasks assigned (check `TaskDAO.findByUserId()` first and show a friendly error) — that's a nice, easy edge case to demonstrate you understand referential integrity.
- [ ] Use `Session`/`Transaction` (or `EntityManager`) with proper commit/rollback/finally handling.
- [ ] Sanity-test each DAO method with a throwaway `main()`/unit test before wiring up JSF.

**Deliverable:** DAOs tested independently of the web layer, including the user-deletion-with-tasks edge case.

---

## Phase 4 — JSF Managed Beans (1 day)

- [ ] `UserBean` and `TaskBean` as `@Named @ViewScoped` (CDI).
- [ ] Each exposes: a list property, a "current" object bound to the create/edit form, and `list()`, `prepareCreate()`, `save()`, `prepareEdit(id)`, `update()`, `delete(id)`.
- [ ] `TaskBean` additionally exposes a `List<User> allUsers` (loaded via `UserDAO.findAll()`) to back the "Assign to" `<h:selectOneMenu>` in the Task form.
- [ ] Inject DAOs rather than calling Hibernate directly from the bean.
- [ ] Add `FacesMessage` success/error feedback for every operation, including the blocked-delete case from Phase 3.

**Deliverable:** beans compile and are exercisable from a barebones test page.

---

## Phase 5 — JSF Views: CRUD Pages (1.5–2 days)

- [ ] `userList.xhtml` — `<h:dataTable>` of all users, Edit/Delete actions, "Add New" button.
- [ ] `userForm.xhtml` — `<h:form>` with `<h:inputText>` for Name/Email, `<h:selectOneMenu>` for Role and Side (backed by the enums).
- [ ] `taskList.xhtml` — `<h:dataTable>` of all tasks, showing the assigned user's name (not just an ID) in one column — this is the payoff of the real relationship. Edit/Delete actions, "Add New" button.
- [ ] `taskForm.xhtml` — `<h:form>` with `<h:inputText>` for Title/EventID/Category, a date input for Deadline, `<h:selectOneMenu>` for Status, and a `<h:selectOneMenu>` bound to `assignedUser` and populated from `TaskBean.allUsers` (use a converter, e.g. `<f:converter converterId="javax.faces.Entity">` via Hibernate, or a simple custom `Converter` class keyed on `UserID`).
- [ ] *(Optional stretch)* On the User detail/view page, list that user's assigned tasks by calling `TaskDAO.findByUserId()` — nice demonstration of the relationship paying off in the UI, not required for the core assignment.
- [ ] Wire navigation: list → edit form → back to list. Add `<h:messages>` on every page.

**Deliverable:** full click-through Create → Read → Update → Delete for both entities, including selecting/reassigning a Task's user from a real dropdown.

---

## Phase 6 — Apply the Three Validation Types (1 day)

| # | Validation type | Where it lives | Example in this project |
|---|---|---|---|
| 1 | **Bean Validation (JSR-380) annotations** | On the entity class fields (Phase 2) | `@NotBlank`, `@Email`, `@NotNull` on `User`; `@Size`, `@FutureOrPresent`, `@NotNull` on `Task` — enforced automatically by JSF's Bean Validation integration on submit |
| 2 | **Standard JSF validators (tag-based)** | In the `.xhtml` form, on `<h:inputText>` components | `<f:validateLength minimum="2" maximum="50"/>` on `User.Name`, `<f:validateRegex pattern="..."/>` on `Email`, `required="true"` on `assignedUser` selection |
| 3 | **Custom validator** | A Java class implementing `javax.faces.validator.Validator`, registered with `@FacesValidator` | e.g. a `TaskDeadlineValidator` that rejects a `Deadline` more than 2 years out, or a validator ensuring the selected `assignedUser`'s `Side` is consistent with something on the task — something a built-in validator can't express |

- [ ] Implement one custom validator class, register it via `@FacesValidator("...")`, reference with `<f:validator validatorId="...">` (or as a bean method `validator="#{bean.validateX}"`).
- [ ] Make sure at least one field per entity is covered by all three types combined (stacked on one field or spread across fields — either is fine, just document which is which).
- [ ] Test each validation path by submitting bad data (empty name, malformed email, no user assigned, invalid date) and confirm the right message appears.

**Deliverable:** a table like the one above mapping fields to validation type(s), plus screenshots of triggered errors.

---

## Phase 7 — CSS Styling: All Three Types (0.5–1 day)

- [ ] **External CSS** — `resources/css/style.css`, linked via `<h:outputStylesheet library="css" name="style.css"/>` in every page's `<h:head>`. Use for layout, table design, form spacing, buttons, color scheme.
- [ ] **Internal CSS** — a `<style>` block inside `<h:head>` on at least one page (e.g. `taskList.xhtml`) for a page-specific rule not worth sharing.
- [ ] **Inline CSS** — `style="..."` directly on elements, e.g. highlighting overdue `Task` rows in red when `Deadline` has passed and `Status != Completed`, or coloring `Role`/`Side` badges differently per value.
- [ ] Keep styling consistent across all pages; use a CSS class toggled on invalid fields (red border) alongside `<h:message>` text.

**Deliverable:** all pages styled and consistent; one concrete example of each CSS type identifiable in the code.

---

## Phase 8 — Integration Testing & Polish (0.5–1 day)

- [ ] Full manual pass: Create → list → Read → Update → Delete for both entities.
- [ ] Specifically test the relationship: create a User, assign several Tasks to them, confirm the Task list shows the user's name correctly; then test the blocked-delete behavior from Phase 3 by trying to delete a User who still has tasks.
- [ ] Test edge cases: empty lists, long input, boundary dates, reassigning a task to a different user.
- [ ] Clean up debug code, unused imports.
- [ ] Confirm every page has a way back to the list.

**Deliverable:** a stable, demoable build.

---

## Phase 9 — Documentation & Packaging (0.5 day)

- [ ] README: entity descriptions (including the FK relationship), setup steps, screenshots of each CRUD screen.
- [ ] Explicitly document where each of the 3 validation types and 3 CSS types live (file + line reference).
- [ ] Package as `.war`, do a clean deploy test from scratch.

**Deliverable:** submission-ready package + documentation.

---

## Suggested timeline (rough)

| Phase | Focus | Est. time |
|---|---|---|
| 0 | Planning | 0.5 day |
| 1 | Setup | 0.5–1 day |
| 2 | Entities + FK relationship | 0.5–1 day |
| 3 | DAO layer | 1 day |
| 4 | Managed beans | 1 day |
| 5 | JSF views/CRUD | 1.5–2 days |
| 6 | Validation (3 types) | 1 day |
| 7 | CSS (3 types) | 0.5–1 day |
| 8 | Testing/polish | 0.5–1 day |
| 9 | Docs/packaging | 0.5 day |
| **Total** | | **~7.5–10 days** part-time |

Build `User` fully through Phase 5 first (it's the simpler entity, no relationship to manage), then build `Task` — that's where the FK dropdown and the blocked-delete edge case come in, and it'll go faster since the CRUD scaffolding pattern already exists.
