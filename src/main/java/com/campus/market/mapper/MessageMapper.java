package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询每个会话的最新一条消息。
     * 会话由 (较小方id, 较大方id, 商品id) 唯一确定，取各会话中 id 最大（即最新）的一条。
     */
    @Select("SELECT m.* FROM message m INNER JOIN (" +
            "SELECT MAX(id) AS max_id FROM message " +
            "WHERE deleted = 0 AND (from_id = #{uid} OR to_id = #{uid}) " +
            "GROUP BY LEAST(from_id, to_id), GREATEST(from_id, to_id), product_id" +
            ") latest ON m.id = latest.max_id " +
            "WHERE m.deleted = 0 ORDER BY m.id DESC")
    List<Message> selectLatestConversations(@Param("uid") Long uid);
}
