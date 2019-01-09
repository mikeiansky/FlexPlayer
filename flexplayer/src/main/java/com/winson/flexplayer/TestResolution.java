package com.winson.flexplayer;

/**
 * @date on 2019/1/9
 * @Author Winson
 */
public class TestResolution implements FlexPlayerResolution {

    private String id;
    private String url;
    private String title;

    public TestResolution(String id, String url, String title) {
        this.id = id;
        this.url = url;
        this.title = title;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
