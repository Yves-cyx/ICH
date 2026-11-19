# 本地作品契约 v0.1

B 负责实现与维护；A 的“我的作品”只调用 B 的作品仓库，不直接拼文件路径。

## DrawingDocument

- schemaVersion：首版为 1；未知版本提示无法编辑，不静默覆盖。
- id：UUID 字符串；title：用户可编辑名称；kind：drawing。
- createdAt / updatedAt：ISO 8601 UTC。
- referenceAssetId：底图稳定编号；referenceOpacity：0..1。
- canvasAspectRatio：逻辑画布宽/高；重开时按此比例呈现。
- strokes：按绘制顺序的笔画数组。
- 每笔包含 tool（pen/eraser）、color（#AARRGGBB）、width（相对画布宽度的比例）、points（[{x,y}]，坐标归一化为 0..1）。
- 擦除只作用于绘画层，不擦除临摹底图。重放笔画时按相同顺序和工具语义恢复。

## 文件与操作

每个作品保存 JSON 与缩略图；二者归属于同一 id。采用临时文件写入后替换，损坏时保留原文件并提示。
B 提供异步 list/read/save/delete/exportImage 操作和成功/失败结果。列表返回 id、title、kind、updatedAt、thumbnailUri。
A 打开作品传 id；导出图片与可编辑草稿分开，不把 PNG 当成可编辑文档。

文件保存在应用私有目录；卸载可能丢失，UI 明确提示。用户主动导出时才复制到共享位置。具体 Java 签名由 T05 实现前与 A 确认。

舞台演出记录不复用 drawing 格式；后续录制需求另写版本化契约。
