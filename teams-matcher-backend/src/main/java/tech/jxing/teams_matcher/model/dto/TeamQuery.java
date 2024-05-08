package tech.jxing.teams_matcher.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.jxing.teams_matcher.common.PageRequest;

import java.util.List;

/**
 * 队伍查询封装类
 * @EqualsAndHashCode(callSuper = true) 是Lombok的一个注解，用于生成equals和hashCode方法
 * callSuper = true表示在生成的equals方法中调用父类的equals方法，在生成的hashCode方法中使用父类的hashCode值
 * 这样可以确保子类对象在进行equals比较或hashCode计算时，同时考虑到了父类属性的值
 * @author JunXing
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * 队伍id
     */
    private Long id;

    /**
     * id 列表
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 搜索关键词（同时搜索队伍名称和描述）
     */
    private String searchText;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;
}
