package xyz.erupt.flow.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : willian fu
 * @version : 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgTreeVo {

    private Long id;

    private String name;

    private String type;

    private String avatar;

    private Boolean sex;

    private Boolean selected;
}
