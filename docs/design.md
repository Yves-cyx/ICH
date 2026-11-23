# 影间非遗 · 实现设计

1. 登录与注册通过 REST API 完成。Spring Security 验证 JWT，MySQL 保存 BCrypt 密码哈希、个人资料和作品。
2. 登录后进入推荐页。双列 RecyclerView 支持类别筛选，卡片可进入详情。Room 缓存公共图文目录，断网时仍可浏览已加载的内容。
3. 体验页以戏台为默认入口，另提供展馆和创作。创作支持选择角色、配色、位置和角度，可将作品保存到账户并从「我的」再次编辑。
4. Android 按界面、ViewModel、Repository 组织。服务端按 Controller、Service、Mapper 组织。公共目录可读取，资料和作品按令牌所属账号隔离。
5. 验收覆盖 MySQL 初始化、真实 API、跨账号作品隔离、Android 安装启动及前后端联调。
