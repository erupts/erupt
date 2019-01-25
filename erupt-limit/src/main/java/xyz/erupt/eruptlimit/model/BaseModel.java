package xyz.erupt.eruptlimit.model;

import lombok.Getter;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;

import javax.persistence.*;

/**
 * Created by liyuepeng on 10/11/18.
 */
@Getter
@MappedSuperclass
public class BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @EruptField(
            edit = @Edit(title = "ID", show = false),
            views = @View(title = "ID", show = false)
    )
    private Long id;
}
