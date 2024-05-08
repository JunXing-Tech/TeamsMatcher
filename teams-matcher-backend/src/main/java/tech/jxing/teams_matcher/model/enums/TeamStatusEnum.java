package tech.jxing.teams_matcher.model.enums;

/**
 * 队伍状态枚举
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

    /**
     * 根据传入的整数值获取对应的TeamStatusEnum枚举实例。
     *
     * @param value 整数值，对应枚举中的值。可以为null。
     * @return 对应的TeamStatusEnum枚举实例。如果找不到匹配的枚举值或输入为null，则返回null。
     */
    public static TeamStatusEnum getEnumByValue(Integer value) {
        if(value == null) {
            return null;
        }
        // 获取TeamStatusEnum枚举的所有实例
        TeamStatusEnum[] values = TeamStatusEnum.values();
        // 遍历枚举实例，查找值匹配的实例
        for (TeamStatusEnum teamStatusEnum : values) {
            if(teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        // 如果没有找到匹配的枚举实例，返回null
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
