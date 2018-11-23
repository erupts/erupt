package com.erupt.core.model;

import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.Edit;
import com.erupt.annotation.sub_field.View;

import javax.persistence.*;

/**
 * Created by liyuepeng on 10/11/18.
 */
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
