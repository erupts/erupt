package com.erupt;

import com.erupt.annotation.Erupt;
import com.erupt.annotation.EruptField;
import com.erupt.annotation.sub_field.*;
import com.erupt.annotation.sub_field.sub_edit.ChoiceType;
import com.erupt.annotation.sub_field.EditType;

import javax.persistence.*;

/**
 * Created by liyuepeng on 9/28/18.
 */
@Erupt
@Entity
@Table(name = "ERUPT_TEST")
public class mmo {


    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "NAME")
    @EruptField(
            view = @View(title = "名称"),
            edit = @Edit(
                    title = "名称",
                    notNull = true,
                    type = EditType.CHOICE,
                    choiceValue = @ChoiceType(values = {"张三", "李四"})),
            search = @Search(isSearch = true)
    )
    public String name;


}
