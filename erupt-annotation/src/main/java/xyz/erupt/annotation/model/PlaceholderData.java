package xyz.erupt.annotation.model;

/**
 * Created by liyuepeng on 11/5/18.
 */
public class PlaceholderData {
    private String placeholder;

    private String data;

    public PlaceholderData(String placeholder, String data) {
        this.placeholder = placeholder;
        this.data = data;
    }

    public PlaceholderData() {
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
