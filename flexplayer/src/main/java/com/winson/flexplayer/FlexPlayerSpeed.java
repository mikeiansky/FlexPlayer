package com.winson.flexplayer;

/**
 * @date on 2019/1/10
 * @Author Winson
 */
public enum FlexPlayerSpeed {


    DOT_EIGHT(0.8f, "0.8X"),
    ONE(1f, "1X"),
    ONE_DOT_TWO_FIVE(1.25f, "1.25X"),
    ONE_DOT_FIVE(1.5f, "1.5X"),
    TWO(2f, "2X");

    private float speed;
    private String name;

    FlexPlayerSpeed(float speed, String name) {
        this.speed = speed;
        this.name = name;
    }

    public float getSpeed() {
        return speed;
    }

    public String getName() {
        return name;
    }

}
