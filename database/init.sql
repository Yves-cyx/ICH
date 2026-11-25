CREATE DATABASE IF NOT EXISTS feiyi CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE feiyi;

CREATE TABLE IF NOT EXISTS member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(24) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(24) NOT NULL,
  bio VARCHAR(160) NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS heritage (
  id BIGINT PRIMARY KEY,
  title VARCHAR(48) NOT NULL,
  category VARCHAR(24) NOT NULL,
  region VARCHAR(32) NOT NULL,
  summary VARCHAR(180) NOT NULL,
  description TEXT NOT NULL,
  art_key VARCHAR(24) NOT NULL
);

-- 同一账号只能收藏同一条目一次，记录时间用于列表排序。
CREATE TABLE IF NOT EXISTS heritage_favorite (
  member_id BIGINT NOT NULL,
  heritage_id BIGINT NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (member_id, heritage_id),
  FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
  FOREIGN KEY (heritage_id) REFERENCES heritage(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS puppet_work (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  name VARCHAR(32) NOT NULL,
  shape VARCHAR(16) NOT NULL,
  palette VARCHAR(16) NOT NULL,
  x DECIMAL(6,5) NOT NULL,
  y DECIMAL(6,5) NOT NULL,
  angle DECIMAL(6,2) NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES member(id),
  INDEX ix_work_owner (owner_id, id)
);

INSERT IGNORE INTO heritage (id, title, category, region, summary, description, art_key) VALUES
(1,'中国皮影戏','表演','陕西 · 华县','一盏灯、一方幕，演出千百年的故事。','皮影戏以兽皮或纸板制成的影偶为主角。艺人隔着幕布操纵影偶，灯光把形象投在幕上，配合唱腔和乐器讲述故事。陕西华县皮影以刀工细密、线条清劲闻名。\n\n观看时不妨留意人物的关节。艺人通过几根操纵杆，就能让角色转身、拱手和跨步。2011 年，中国皮影戏列入联合国教科文组织人类非物质文化遗产代表作名录。','shadow'),
(2,'南京云锦','工艺','江苏 · 南京','织金入锦，光线掠过便显出不同颜色。','南京云锦以色彩层次丰富、纹样庄重著称。织造时，拽花工在织机上方提起经线，织手在下方按纹样配色穿梭，两人配合才能完成复杂图案。\n\n观察锦面，可以看到不同色线在同一块纹样中的交替和过渡。','brocade'),
(3,'蔚县剪纸','工艺','河北 · 蔚县','刀刻与点染相遇，纸上便多了几分生气。','蔚县剪纸常使用薄宣纸，艺人先用刻刀镂刻图样，再点染色彩。戏曲脸谱、门神和花鸟都是常见题材。\n\n与只用单色纸的剪纸相比，蔚县剪纸经常在镂空之外利用色彩区分角色和层次。','paper'),
(4,'昆曲','表演','江苏 · 昆山','慢听水磨腔，听见旋律里的细致转折。','昆曲源于昆山一带，唱腔细腻舒展，讲究字音、行腔和身段之间的配合。《牡丹亭》中的《游园》《惊梦》是常见的舞台折子。\n\n一段唱词往往会拉长一个字的发音，演员的眼神和手势也会随旋律移动。','opera'),
(5,'景德镇手工制瓷','工艺','江西 · 景德镇','从泥坯到釉色，火候决定器物的表情。','手工制瓷包括制泥、拉坯、修坯、绘画、施釉与烧成等工序。以青花瓷为例，纹样绘在坯体上，罩以透明釉后烧制。\n\n青花的浓淡和线条受到用笔、颜料浓度和烧成条件共同影响。','porcelain'),
(6,'古琴艺术','音乐','中国 · 多地','七弦之间，散音、按音与泛音各有余韵。','古琴常用散音、按音和泛音三类音色。散音直接拨弦，按音由左手按住琴弦后奏出，泛音则通过轻触特定位置取得清亮音色。\n\n古琴谱常用减字谱记录指法。听的时候可以留意左手在弦上移动形成的滑音。','guqin');
