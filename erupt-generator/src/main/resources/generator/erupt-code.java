/*
 * Copyright (c) 2020-2035, erupt.xyz All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: YuePeng (erupts@126.com)
 */

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.*;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import xyz.erupt.auth.model.base.BaseModel;
import xyz.erupt.auth.model.base.HyperModel;
import javax.persistence.*;
import java.util.Set;

<#assign erupt=rows[0]/>
@Erupt(name = "${erupt.name}")
@Table(name = "${erupt.className}")
@Entity
public class ${erupt.className} extends BaseModel {

    <#list erupt.generatorFields as field>
        <#assign type = GeneratorType.valueOf(field.type)/>
        @EruptField(
                views = @View(
                        title = "${field.showName}"${field.sortable?string(', sortable = true', '')}${field.isShow?string('', ', show = false')}
                ),
                edit = @Edit(
                        title = "${field.showName}",
                        type = EditType.${type.mapping.name()}${field.isShow?string('', ', show = false')}${field.notNull?string(', notNull = true', '')}
                        <#if type.code??>${','+type.code}</#if>
                )
        )
        private ${GeneratorType.replaceLinkClass(type.type, field.linkClass!)} ${field.fieldName};

    </#list>
}