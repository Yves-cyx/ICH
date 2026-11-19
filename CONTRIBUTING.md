# 开发协作流程

## 每个任务怎么走

1. 在 Issue 写清目标、负责人、影响文件、依赖和验收条件。先看是否有人正在做。
2. 从最新 main 创建小分支，例如 feat/001-catalog-list。
3. 实现一个可验收功能；自己的页面要能通过临时数据运行，不依赖别人电脑。
4. 本地构建、运行相关流程，提交 commit，推送分支并开 Pull Request。
5. 另一人看改动并复现验收；A 处理集成。合并后大家拉取 main。

初始化直接写入 main；后续功能按 PR 流程。该规则是团队约定，目前未配置服务器端分支保护。

## 常用命令

首次：
```bash
git clone https://github.com/Yves-cyx/ICH.git
cd ICH
```

开始任务前（工作区有未提交改动时先提交或暂存）：
```bash
git switch main
git pull --ff-only
git switch -c feat/001-catalog-list
```

完成一小步：
```bash
git add android/app/src
git commit -m "feat: add catalog list"
git push -u origin feat/001-catalog-list
```

在 GitHub 点 Compare & pull request，按模板填写。main 有更新时，在功能分支运行 git fetch origin 和 git merge origin/main；有冲突先联系文件负责人，解决后重新构建。不要强推 main。

## 防止重复工作

- 同一个任务同一时间只有一位主负责人，其他人担任审阅。
- 每 1–2 天提交可运行的小 PR；不要在个人分支积攒一整个大模块。
- 每天简短同步“完成了什么、接下来做什么、被什么阻塞”。
- 接口改动先更新 contracts，再实现；临时假数据也遵守相同格式。
- XML / Java 资源加功能前缀，例如 catalog_、stage_、drawing_；共享资源用 ich_。
- 发现别人的问题先开 Issue 或沟通，避免同时修同一段代码。

## 完成的含义

代码已合并，相关构建通过，验收流程有截图/录屏或请求响应示例，另一名成员能复现。只有写完代码但尚未接入，不算完成。

## 提交前

不提交 local.properties、密码、Token、服务端 .env、签名密钥或构建产物。数据与图片注明来源。避免大规模自动格式化无关文件。
