package xyz.erupt.generator.base;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Parent class of the generated entity, decides which columns are inherited
 * and therefore skipped while importing a table.
 *
 * @author YuePeng
 * date 2026-09-17
 */
@Getter
public enum SuperModel {

    NONE(null, null),

    BASE_MODEL("BaseModel", "xyz.erupt.jpa.model.BaseModel", Columns.ID),

    //audit trail kept by the framework, the Vo variants additionally show it in the ui
    META_MODEL("MetaModel", "xyz.erupt.jpa.model.MetaModel", Columns.META),

    META_MODEL_VO("MetaModelVo", "xyz.erupt.jpa.model.MetaModelVo", Columns.META),

    META_MODEL_CREATE_VO("MetaModelCreateVo", "xyz.erupt.jpa.model.MetaModelCreateVo", Columns.META),

    META_MODEL_UPDATE_VO("MetaModelUpdateVo", "xyz.erupt.jpa.model.MetaModelUpdateVo", Columns.META),

    META_MODEL_CREATE_ONLY("MetaModelCreateOnly", "xyz.erupt.jpa.model.MetaModelCreateOnly", Columns.META_CREATE),

    META_MODEL_CREATE_ONLY_VO("MetaModelCreateOnlyVo", "xyz.erupt.jpa.model.MetaModelCreateOnlyVo", Columns.META_CREATE),

    //the same trail recorded against the user table instead of the user name
    HYPER_MODEL("HyperModel", "xyz.erupt.upms.model.base.HyperModel", Columns.HYPER),

    HYPER_MODEL_VO("HyperModelVo", "xyz.erupt.upms.helper.HyperModelVo", Columns.HYPER),

    HYPER_MODEL_CREATOR_VO("HyperModelCreatorVo", "xyz.erupt.upms.helper.HyperModelCreatorVo", Columns.HYPER),

    HYPER_MODEL_UPDATE_VO("HyperModelUpdateVo", "xyz.erupt.upms.helper.HyperModelUpdateVo", Columns.HYPER),

    HYPER_MODEL_CREATOR_ONLY_VO("HyperModelCreatorOnlyVo", "xyz.erupt.upms.helper.HyperModelCreatorOnlyVo", Columns.HYPER_CREATE);

    private final String className;

    private final String packageName;

    private final Set<String> inheritColumns;

    SuperModel(String className, String packageName, String... inheritColumns) {
        this.className = className;
        this.packageName = packageName;
        this.inheritColumns = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(inheritColumns)));
    }

    public boolean inherit(String column) {
        return this.inheritColumns.contains(Naming.humpToLine(column));
    }

    public String label() {
        return null == this.className ? "None" : this.className;
    }

    //an enum constant may not read a static field of its own class, a holder may hold it
    private static class Columns {

        private static final String[] ID = {"id"};

        private static final String[] META = {"id", "create_by", "create_time", "update_by", "update_time"};

        private static final String[] META_CREATE = {"id", "create_by", "create_time"};

        private static final String[] HYPER = {"id", "create_time", "create_user_id", "update_time", "update_user_id"};

        private static final String[] HYPER_CREATE = {"id", "create_time", "create_user_id"};

    }

}
