package tech.jxing.teams_matcher.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 * @author JunXing
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 2584309478944927048L;

    /**
     * 当前页数
     */
    protected int pageNum = 1;

    /**
     * 页面大小
     */
    protected int pageSize = 10;
}
