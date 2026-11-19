# A 的下一步：先把框架做出来

负责人：陈翊轩。任务：T09。当前仓库已有可切换的三个占位入口；本轮是在它上面完善页面结构与导航。独立登录页仍待实现。

## 本轮交付目标

做出一个可以安装、可以演示页面走向的 App。各功能先展示标题、返回按钮与占位说明。整体样式统一之后，B/C 能按约定接入。

## 1. 先运行已有工程

在仓库根目录操作；有未提交改动时先提交或暂存：
```bash
git switch main
git pull --ff-only
git switch -c feat/a-app-framework
```

Android Studio 打开 android/，等待同步并运行 app。先确认非遗图鉴、体验、我的可以切换。环境细节见 [启动指南](启动指南.md)。

## 2. 先认识这些文件

| 文件（相对 android/app/src/main） | 本轮作用 |
| --- | --- |
| java/com/yves/ich/MainActivity.java | 现有主入口和底部切换 |
| res/layout/activity_main.xml | 外壳与底部导航 |
| res/layout/catalog_page.xml | 图鉴首页基础布局 |
| res/layout/experience_page.xml | 戏台、建模/360°、画布三个入口 |
| res/layout/profile_page.xml | 登录、收藏、作品、设置入口 |
| res/values/colors.xml、styles.xml | 全局颜色与公共样式 |
| AndroidManifest.xml | 注册新增 Activity |

本轮沿用 Java + XML 和现有 MainActivity。为二级页面创建独立 Activity 即可，暂不引入复杂多模块结构。公共跳转集中放入 navigation/AppNavigator.java（待创建）。

## 3. 定好页面与归属

下表是要创建/接通的页面约定，类名为拟定名称，尚未实现。主入口继续使用当前三个 XML；框架交接后再按需要拆分。

| 页面 | 入口 | 所属包 | 本轮 A 做什么 | 交接后负责人 |
| --- | --- | --- | --- | --- |
| 图鉴详情 CatalogDetailActivity | 图鉴示例卡片 | feature/catalog | 标题、返回、heritageId 参数 | B |
| 戏台 StageActivity | 体验 → 戏台 | feature/stage | 标题、返回、占位说明 | A |
| 建模/360° ViewerActivity | 体验 → 建模展示 | feature/viewer | 标题、返回、assetId 参数 | B |
| 画布 DrawingActivity | 体验 → 临摹画布 | feature/drawing | 标题、返回、templateId/可选 workId | B |
| 登录 LoginActivity | 我的 → 登录 | feature/login | 表单、基础校验、状态和返回 | A，C 提供认证 |
| 收藏 FavoritesActivity | 我的 → 收藏 | feature/profile | 标题、返回、占位说明 | B，C 提供数据 |
| 作品 WorksActivity | 我的 → 作品 | feature/profile | 标题、返回、占位说明 | B |
| 设置 SettingsActivity | 我的 → 设置 | feature/profile | 标题、返回、占位说明 | B |

参数通过 Intent 传稳定 ID；由 AppNavigator 维护参数名。模块内部加载内容，导航不传整份 JSON。缺失/非法参数显示说明，不闪退。

## 4. 做页面走向，再统一 UI

先完成“入口 → 二级页 → 返回原入口”的路径，再统一暖白、深墨、朱红配色，标题、按钮、卡片和间距。不要每个页面各写一套公共样式。

- 冷启动默认进入图鉴，允许访客使用。
- 点击底部入口切换主页面；旋转或重建后保留当前选中入口。
- 二级页使用系统返回键/返回按钮，不能再启动一份 MainActivity 造成重复页面。
- 登录从“我的”进入；本轮返回“我的”。后续收藏触发登录时，返回发起操作的页面。
- 二级页不显示底部三个入口；登录也是独立二级页，不增加第四个主 Tab。
- 戏台横屏和交互实现放在 T02；本轮先验证导航，避免所有页面一起强制横屏。

## 5. 登录框架的边界

本轮包含账号输入、密码输入、登录按钮、空值校验、加载/错误状态的呈现位置，以及返回继续浏览。保留注册入口的设计位置，注册业务随 T06 接入。

没有 C 的 AuthRepository 时，点击登录明确提示“账号服务尚未接入”；不把任意输入视作登录成功，不自建 Token 保存逻辑。C 提供接口后再接真实登录。

## 6. 提交与交接

```bash
git add android/app/src docs
git commit -m "feat: add app framework and login screen"
git push -u origin feat/a-app-framework
```

在 GitHub 创建 PR，关联 T09。列出新增页面、入口方法、参数、公共组件、B/C 接入位置。B 或 C 复现后合并；合并之后 B 再修改接手的页面，避免两人同时改一个 XML。

## 本轮验收

- [ ] 构建和 Lint 通过。
- [ ] 三个主入口切换正常，所有表中页面能进入和返回。
- [ ] 图鉴卡片传递的 heritageId 可在详情占位页验证。
- [ ] 返回键不会产生重复首页；重新进入页面不崩溃。
- [ ] 登录空值有提示；未接服务时不显示登录成功。
- [ ] 公共配色、标题、按钮统一，小屏可滚动，系统栏不遮挡内容。
- [ ] 提供一段完整导航录屏与文件交接清单。
- [ ] B/C 拉取后可以运行，清楚自己的修改范围。

本轮不要求真实搜索、50 条内容、画布笔画、模型制作、数据库或服务器完成。框架验收通过后，A 再进入戏台开发。
