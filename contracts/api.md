# 接口草案 v0.1

状态：供三人确认与假数据开发，尚未实现。C 负责维护，A/B 是调用方。路径前缀 /api/v1，JSON 使用 UTF-8。

## 基本约定

- id 为稳定字符串；页面传 id，避免把整份内容塞进导航参数。
- 分页 page 从 1 开始，size 默认 20、上限 50。
- 错误使用正确 HTTP 状态与 {"code":"VALIDATION_ERROR","message":"可显示的提示"}；不把密码/Token 写进日志。
- GET /heritages 和详情无需登录。收藏使用 Authorization: Bearer <token>，未登录返回 401。
- 真实账号上线前使用 HTTPS；仅本机调试可按需配置 debug 网络策略，不能全局开放明文。
- 时间统一 ISO 8601 UTC；页面自行转换显示。

## Heritage 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / name / category / region | string | 稳定编号、名称、分类、地区 |
| coverUrl | string 或 null | 缺失时显示占位 |
| summary / description | string | 简介与详细介绍 |
| features / background | string | 工艺或表演特点、文化背景 |
| sources | array | {title, url}；正式内容必须有来源 |
| experiences | string[] | 可用值 stage / viewer / drawing；空数组不展示体验入口 |

## 路由

| 方法与路径 | 输入 | 成功响应 |
| --- | --- | --- |
| GET /heritages | keyword、category、page、size | {items: Heritage[], page, size, total} |
| GET /heritages/{id} | id | Heritage；不存在 404 |
| POST /auth/register | {username, password, nickname} | 201 {id, nickname}；随后登录 |
| POST /auth/login | {username, password} | {accessToken, expiresAt, user:{id,nickname}} |
| POST /auth/logout | Bearer token | 204；客户端同时清除会话 |
| GET /me | Bearer token | {id, nickname} |
| GET /me/favorites | page、size | {items: Heritage[], page, size, total} |
| PUT /me/favorites/{heritageId} | Bearer token | 204；重复收藏幂等 |
| DELETE /me/favorites/{heritageId} | Bearer token | 204；重复删除幂等 |

首版 Token 到期重新登录，暂不设计 refresh token。密码仅在服务端使用成熟密码哈希实现，不明文存储；C 在实现 PR 中说明验证方案。

## Android 交接

C 提供异步 CatalogRepository（list/get）、AuthRepository（register/login/logout/session）、FavoriteRepository（list/add/remove）。A 仅依赖这些接口，不直接写 HTTP。
具体 Java 签名由 T03 同 A 确定后补到本文；约定成功/失败回调、主线程交付、取消请求和加载错误状态。假实现与远端实现遵守同一接口。

## 样例

seed.json 的三条样例用于格式验证，正文和来源留空，需在 T01/T07 补充真实审校内容。正式内容验收不得用该空样例通过。

本地作品不走本接口，格式见 works.md；作品上传不属于首轮范围。
