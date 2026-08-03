# Issue tracker: GitHub / Issue 跟踪器：GitHub

Issues and PRDs for this repo live as GitHub issues in `qiyee1688/fitness-app`.

本仓库的 Issue 与 PRD 都发布到 `qiyee1688/fitness-app` 的 GitHub Issues。

Use the `gh` CLI for all operations.

所有相关操作优先使用 `gh` CLI。

## Conventions / 约定

- Create an issue / 创建 issue: `gh issue create --title "..." --body "..."`
- Read an issue / 读取 issue: `gh issue view <number> --comments`
- List issues / 列出 issue: `gh issue list --state open --json number,title,body,labels,comments`
- Comment on an issue / 评论 issue: `gh issue comment <number> --body "..."`
- Apply or remove labels / 添加或移除标签: `gh issue edit <number> --add-label "..."` / `--remove-label "..."`
- Close an issue / 关闭 issue: `gh issue close <number> --comment "..."`

Infer the repo from `git remote -v`; `gh` does this automatically inside this clone.

仓库从 `git remote -v` 推断；在当前 clone 内运行 `gh` 时会自动识别。

## When a skill says "publish to the issue tracker"

Create a GitHub issue.

当 skill 要求“发布到 issue tracker”时，创建 GitHub Issue。

## When a skill says "fetch the relevant ticket"

Run `gh issue view <number> --comments`.

当 skill 要求“获取相关 ticket”时，运行 `gh issue view <number> --comments`。
