package com.yingjian.api.mapper;

import com.yingjian.api.model.Models.Heritage;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface FavoriteMapper {
    @Select("""
            <script>
            SELECT h.*
            FROM heritage_favorite f JOIN heritage h ON h.id = f.heritage_id
            WHERE f.member_id = #{memberId}
            ORDER BY f.created_at DESC, f.heritage_id DESC
            <if test="size != null">LIMIT #{size} OFFSET #{offset}</if>
            </script>
            """)
    List<Heritage> list(@Param("memberId") long memberId, @Param("size") Integer size, @Param("offset") long offset);

    @Select("SELECT COUNT(*) FROM heritage_favorite WHERE member_id = #{memberId}")
    long count(@Param("memberId") long memberId);

    // 重复收藏保留原来的收藏时间，列表顺序不变。
    @Insert("""
            INSERT INTO heritage_favorite (member_id, heritage_id) VALUES (#{memberId}, #{heritageId})
            ON DUPLICATE KEY UPDATE heritage_id = heritage_favorite.heritage_id
            """)
    void add(@Param("memberId") long memberId, @Param("heritageId") long heritageId);

    @Delete("DELETE FROM heritage_favorite WHERE member_id = #{memberId} AND heritage_id = #{heritageId}")
    void remove(@Param("memberId") long memberId, @Param("heritageId") long heritageId);
}
