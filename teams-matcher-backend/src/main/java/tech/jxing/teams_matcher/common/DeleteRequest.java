package tech.jxing.teams_matcher.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 *
 * @author JunXing
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 5721398126590742159L;

    private long id;
}
