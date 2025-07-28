
# Git Interview Guide

A detailed guide for mastering Git (Version Control) for interviews, including core concepts, workflow strategies, and follow-up questions.

---

## ✅ 1. Git (Version Control)

### 🔹 Core Concepts

#### 🔸 What’s the difference between merge and rebase?

- **Merge**: Combines the histories of two branches, creating a new merge commit.
- **Rebase**: Moves or reapplies commits from one branch onto another, creating a linear history.

```bash
# Merge
git checkout main
git merge feature-branch

# Rebase
git checkout feature-branch
git rebase main
```

**When to Use:**
- Use `merge` when you want to preserve the full history of both branches.
- Use `rebase` for cleaner, linear history (especially for local feature branches).

**Follow-up Interview Qs:**
- What are the risks of using rebase on shared branches?
- How do you resolve conflicts during a rebase?

---

#### 🔸 When would you use cherry-pick?

**Cherry-pick** allows you to apply specific commits from one branch to another.

```bash
git checkout main
git cherry-pick <commit-hash>
```

**Use Case:** Fixing bugs in production by applying specific changes without merging the whole branch.

**Follow-up Interview Qs:**
- What happens if a cherry-picked commit is already present on the target branch?
- How do you resolve conflicts during cherry-pick?

---

#### 🔸 How do you resolve merge conflicts?

1. Git marks conflicts in the file with `<<<<<<<`, `=======`, and `>>>>>>>`.
2. Manually edit to keep the correct code.
3. Use `git add <file>` to mark resolved.
4. Finalize with `git commit` or continue with `git rebase --continue` if rebasing.

```bash
git status  # shows conflicted files
git add <resolved-file>
git commit
```

**Follow-up Interview Qs:**
- What tools do you use to resolve merge conflicts?
- Can merge conflicts occur during rebase or cherry-pick?

---

#### 🔸 What is the difference between git reset, git revert, and git checkout?

| Command | Description | Use Case |
|--------|-------------|----------|
| `reset` | Moves HEAD and optionally changes index/working tree | Undo commits or unstage files |
| `revert` | Creates a new commit that undoes a previous commit | Safe way to undo commits in shared branches |
| `checkout` | Switches branches or restores files | Moving between branches or restoring files |

```bash
# Reset
git reset --hard <commit>
# Revert
git revert <commit>
# Checkout
git checkout main
```

**Follow-up Interview Qs:**
- When should you avoid using `git reset`?
- Can you recover a commit after reset?

---

#### 🔸 What is a detached HEAD state, and how do you recover from it?

- **Detached HEAD**: You're no longer on a branch, but on a specific commit.
- Any changes made here can be lost unless committed and referenced.

```bash
# Fix by creating a branch from the commit
git checkout -b new-branch
```

**Follow-up Interview Qs:**
- How can you recover work done in a detached HEAD?
- How to check HEAD state?

---

### 🔹 Workflow Practices

#### 🔸 What Git branching strategy do you follow?

- **GitFlow**: Separate branches for feature, release, hotfix.
- **Trunk-Based**: Work directly on `main` or short-lived branches.

**Follow-up Interview Qs:**
- Why would you choose GitFlow over trunk-based?
- How do you enforce clean merges to main?

---

#### 🔸 How do you handle hotfixes or urgent prod bugs in Git?

1. Create a branch from main (`hotfix/bug-id`).
2. Fix the bug.
3. Merge to `main`, tag a release, deploy.
4. Optionally merge back to `develop`.

```bash
git checkout -b hotfix/bug-101 main
# fix code
git commit -am "fix: urgent bug 101"
git checkout main
git merge hotfix/bug-101
git tag v1.0.1
```

**Follow-up Interview Qs:**
- How do you manage hotfixes in release-critical periods?
- Do you automate tagging and versioning?

---

#### 🔸 How do you squash commits, and when is it appropriate to do so?

- Squashing combines multiple commits into one before merging.

```bash
git rebase -i HEAD~3  # Choose squash or s
```

**Use Case:** Clean up messy commit history before merging to main.

**Follow-up Interview Qs:**
- Should you squash in team branches?
- What are the implications for code review and traceability?

---

## ✅ Bonus Interview Tips

- Know how to use Git GUI tools: SourceTree, GitKraken, or VSCode Git.
- Practice conflict resolution on real branches.
- Always push to remote branches with care during rebase/reset.

---

## ✅ Final Takeaways

| Command | Use Case |
|---------|----------|
| `git log` | Inspect commit history |
| `git diff` | View code changes |
| `git stash` | Temporarily save work |
| `git tag` | Version control releases |
| `git reflog` | Recover lost commits |

---
