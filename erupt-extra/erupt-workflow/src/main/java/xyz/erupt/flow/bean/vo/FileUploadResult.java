package xyz.erupt.flow.bean.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadResult {
    private String name;
    private String url;

    public FileUploadResult(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
