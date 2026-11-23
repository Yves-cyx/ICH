# Android 框架交接

框架任务为 T09（#5）。用 Android Studio 打开仓库里的 `android/`，运行 `app` 即可用访客身份测试。环境配置见[启动指南](启动指南.md)。

## 页面与负责人

Java 文件位于 `android/app/src/main/java/com/yves/ich/`，布局位于 `android/app/src/main/res/layout/`。表中每个类名对应同名的 Java 文件。二级页使用系统返回手势或页面上的“返回”按钮，都回到打开它的入口。

| 从哪里进入 | 页面类和包 | 页面布局 | AppNavigator 方法 | 接手人 |
| --- | --- | --- | --- | --- |
| 图鉴 → 皮影戏 | feature/catalog/CatalogDetailActivity | activity_catalog_detail.xml | openCatalogDetail(from, heritageId) | B 王浩铭 |
| 体验 → 皮影戏台 | feature/stage/StageActivity | activity_stage.xml | openStage(from) | A 陈翊轩 |
| 体验 → 360° 展示 | feature/viewer/ViewerActivity | activity_viewer.xml | openViewer(from, assetId) | B |
| 体验 → 临摹画布 | feature/drawing/DrawingActivity | activity_drawing.xml | openDrawing(from, templateId) | B |
| 我的 → 登录 | feature/login/LoginActivity | activity_login.xml | openLogin(from) | A 做表单，C 季院升提供认证 |
| 我的 → 我的收藏 | feature/profile/FavoritesActivity | activity_favorites.xml | openFavorites(from) | B 做页面，C 提供数据 |
| 我的 → 我的作品 | feature/profile/WorksActivity | activity_works.xml | openWorks(from) | B |
| 我的 → 设置 | feature/profile/SettingsActivity | activity_settings.xml | openSettings(from) | B |

- A 维护 `MainActivity.java`、`activity_main.xml`、`experience_page.xml`、`navigation/AppNavigator.java`、`AndroidManifest.xml` 和公共样式。公共返回栏和标题在 `ui/FeatureActivity.java` 和 `activity_feature.xml`。
- B 接手 `catalog_page.xml` 和 `profile_page.xml`。图鉴列表接入时，点击某行调用 `AppNavigator.openCatalogDetail(this, itemId)`。详情页读取 `HERITAGE_ID`，再向 C 的图鉴接口查询。样例皮影戏使用 `contracts/seed.json` 中的 `shadow-puppetry`。
- 体验页传入的 `shadow-figure`、`paper-cutting` 是开发用的素材和底图编号。B 接入作品列表后，用 `AppNavigator.openWork(this, workId)` 打开已有草稿，在画布读取 `WORK_ID`。数据格式见[作品契约](../contracts/works.md)。
- C 可直接在 `server/` 和 `data/` 开发图鉴与认证接口，与 A/B 对齐[接口草案](../contracts/api.md)。登录表单通过空值校验后会提示“账号服务尚未接入”。认证接入点在 `LoginActivity.submit()`，布局预留了 `login_progress` 和 `login_status`。

## 验收清单

1. 冷启动进入图鉴。点“皮影戏 · 查看详情”，应看到编号 `shadow-puppetry`；点“返回”回到图鉴。
2. 切到“体验”。依次打开戏台、360° 展示、临摹画布。后两页分别显示素材编号、底图编号。每次返回都应回到“体验”。
3. 切到“我的”。进入登录，先空着提交，再只填账号提交，最后填写任意测试账号和测试密码提交。应先后看到账号必填、密码必填、服务尚未接入。返回后仍为访客。
4. 在“我的”依次打开收藏、作品、设置，用系统返回键回到“我的”。旋转设备后仍应选中“我的”。
5. A 或另一位组员在模拟器录下上述路径，附到 PR。组员复现后合并；各自从更新后的 main 新建功能分支。

## 合并后的第一项任务

| 人员 | 可以开始做 | 首次交付的验收方式 |
| --- | --- | --- |
| A 陈翊轩 | 在 feature/stage 实现第一个能拖动的影人 | 从“体验”进入戏台，影人跟手移动，返回仍在“体验” |
| B 王浩铭 | 在 feature/catalog 和 catalog_page.xml 接入样例列表与详情 | 点击不同条目后看到相应名称和内容 |
| C 季院升 | 用 contracts/seed.json 跑通图鉴列表与按 ID 查详情 | B 用同一组编号获取列表和详情；后续与 A 联调登录 |
