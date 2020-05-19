package xyz.erupt.example.handler;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.AttachmentProxy;
import xyz.erupt.core.util.MimeUtil;

import java.io.InputStream;

/**
 * 七牛对象存储demo
 *
 * @author liyuepeng
 * @date 2020-05-17
 */
@Service
public class QiniuOosProxy implements AttachmentProxy {


    private static final String ACCESS_KEY = "hU85O3_YN6fRescdkbL2176ljf-R9zU_Lzbp94v5";

    private static final String SECRET_KEY = "HAxZaX3-zn5cfz4ate4NagcHT0m8v_RSKwqnj7LF";

    private static final String BUCKET = "erupt";

    @Override
    public void upLoad(InputStream inputStream, String path) {
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        String upToken = auth.uploadToken(BUCKET);
        try {
            Response response = uploadManager.put(inputStream, path, upToken, null, MimeUtil.getMimeType(path));
            if (!response.isOK()) {
                throw new RuntimeException("上传七牛云存储空间失败");
            }
        } catch (QiniuException ex) {
            Response r = ex.response;
            throw new RuntimeException(r.toString());
        }
    }

    @Override
    public boolean isLocalSave() {
        return false;
    }

    @Override
    public String fileDomain() {
        return "http://oos.erupt.xyz";
    }
}
