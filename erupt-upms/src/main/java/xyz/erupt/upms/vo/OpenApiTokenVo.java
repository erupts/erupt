package xyz.erupt.upms.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author YuePeng
 * date 2024/8/17 16:20
 */
@Getter
@Setter
@NoArgsConstructor
public class OpenApiTokenVo {

    //token
    private String token;

    //token 过期时间
    private LocalDateTime expireTime;

    public OpenApiTokenVo(String token, LocalDateTime expireTime) {
        this.token = token;
        this.expireTime = expireTime;
    }
}
