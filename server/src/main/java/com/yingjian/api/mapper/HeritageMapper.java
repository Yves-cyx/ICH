package com.yingjian.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yingjian.api.model.Models.Heritage;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface HeritageMapper extends BaseMapper<Heritage> {
    String SEARCH_FILTERS = """
            <where>
                <if test="pattern != null">
                    (title LIKE #{pattern} ESCAPE '!'
                    OR region LIKE #{pattern} ESCAPE '!'
                    OR summary LIKE #{pattern} ESCAPE '!')
                </if>
                <if test="category != null">AND category = #{category}</if>
            </where>
            """;

    @Select("<script>SELECT * FROM heritage " + SEARCH_FILTERS + """
            ORDER BY id
            <if test="size != null">LIMIT #{size} OFFSET #{offset}</if>
            </script>
            """)
    List<Heritage> search(@Param("pattern") String pattern, @Param("category") String category,
                          @Param("size") Integer size, @Param("offset") long offset);

    @Select("<script>SELECT COUNT(*) FROM heritage " + SEARCH_FILTERS + "</script>")
    long countMatches(@Param("pattern") String pattern, @Param("category") String category);
}
