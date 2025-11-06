/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import jakarta.persistence.*;
import xyz.erupt.annotation.*;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.upms.model.base.HyperModel;
import xyz.erupt.jpa.model.BaseModel;
import java.util.Set;
import java.util.Date;

<#assign erupt=rows[0]/>
@Erupt(name = "${erupt.name}")
@Table(name = "${erupt.tableName}")
@Entity
public class ${erupt.className} extends BaseModel {

    <#list erupt.fields?sort_by('sort') as field>
        <#assign type = GeneratorType.valueOf(field.type) />
        <#assign annotation = type.annotation(erupt.className, field.linkClass!)!'' />
        @EruptField(
                views = @View(
                        title = "${field.showName}"${field.sortable?string(', sortable = true', '')}${field.isShow?string('', ', show = false')}
                ),
                edit = @Edit(
                        title = "${field.showName}",
                        type = EditType.${type.mapping.name()}${field.query?string(', search = @Search', '')}${field.isShow?string('', ', show = false')}${field.notNull?string(', notNull = true', '')}<#if type.code??>${',
                        ' + type.code}</#if>
                )
        )<#if annotation!=''>${'\n        '+annotation}</#if>
        private ${type.fieldType(erupt.className, field.linkClass!)!} ${field.fieldName};

    </#list>
}