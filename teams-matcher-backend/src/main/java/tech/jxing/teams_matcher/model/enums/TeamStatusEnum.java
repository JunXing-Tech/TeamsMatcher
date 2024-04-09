package tech.jxing.teams_matcher.model.enums;

/**
 * 队伍状态枚举
 *
 * @author JunXing
 */
public enum TeamStatusEnum {
    /**
     * PUBLIC_STATUS 队伍为公开状态
     * PRIVATE_STATUS 队伍为私有状态
     * SECRET_STATUS 队伍为加密状态
     */
    PUBLIC_STATUS(0, "公开"),
    PRIVATE_STATUS(1, "私有"),
    SECRET_STATUS(2, "加密");

    private int value;

    private String text;

    public static TeamStatusEnum getEnumByValue(Integer value) {
        if(value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if(teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
