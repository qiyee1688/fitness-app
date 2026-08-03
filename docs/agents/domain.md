# Domain Docs / 领域文档

This repo uses a single-context domain documentation layout.

本仓库使用单上下文领域文档布局。

## Before engineering work / 工程工作前

Before doing engineering work, read:

开始工程工作前，先读取：

- `CONTEXT.md` at the repo root / 仓库根目录的 `CONTEXT.md`
- Relevant ADRs under `docs/adr/`, if present / 如存在，读取 `docs/adr/` 下与当前工作相关的 ADR

If those files do not exist, proceed silently.

如果相关文件不存在，静默继续，不需要主动报错。

## Vocabulary / 词汇

Use the glossary vocabulary from `CONTEXT.md` in issue titles, PRDs, test names, and implementation notes.

在 issue 标题、PRD、测试命名、实现说明中，使用 `CONTEXT.md` 中定义的领域词汇。

If a concept is missing from the glossary, note it for `/domain-modeling`.

如果需要的概念不在词汇表中，记录为后续 `/domain-modeling` 的候选事项。

## ADR conflicts / ADR 冲突

If output contradicts an ADR, call out the conflict explicitly.

如果输出内容与已有 ADR 冲突，必须明确指出冲突点。
