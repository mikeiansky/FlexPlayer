package com.winson.flexplayer;

/**
 * @date on 2019/1/9
 * @Author Winson
 */
public class TestSelection implements FlexPlayerSelection {

    private String id;
    private String url;
    private String title;
    private String cover;
    private String discription;

    public TestSelection(String id, String url, String title, String cover, String discription) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.cover = cover;
        this.discription = discription;
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

    @Override
    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }
}
