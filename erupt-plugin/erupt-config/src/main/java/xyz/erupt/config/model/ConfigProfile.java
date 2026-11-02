package xyz.erupt.config.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.Readonly;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.config.proxy.ConfigProfileDataProxy;
import xyz.erupt.jpa.model.MetaModel;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A Spring profile's items, layered the way {@code application-{profile}.yml} layers over
 * {@code application.yml}: every instance reads {@value #DEFAULT}, and an instance whose
 * {@code spring.profiles.active} names a profile reads that profile's items on top.
 *
 * @author YuePeng
 */
@Getter
@Setter
@Entity
@Table(name = ConfigProfile.TABLE, uniqueConstraints = @UniqueConstraint(columnNames = ConfigProfile.CODE_COLUMN))
@Erupt(name = "Profiles", orderBy = "id", tree = @Tree(label = "code"), dataProxy = ConfigProfileDataProxy.class)
public class ConfigProfile extends MetaModel {

    public static final String TABLE = "e_config_profile";

    public static final String CODE_COLUMN = "code";

    /** Spring's reserved default profile: the layer every instance reads first. */
    public static final String DEFAULT = "default";

    @Column(name = CODE_COLUMN)
    @EruptField(views = @View(title = "Code"), edit = @Edit(title = "Code", notNull = true, search = @Search(operator = QueryExpression.LIKE),
            readonly = @Readonly(add = false), inputType = @InputType(regex = "^[A-Za-z0-9][A-Za-z0-9_.\\-]*$"),
            desc = "A Spring profile name as spring.profiles.active lists it: dev, prod"))
    private String code;

    @EruptField(views = @View(title = "Remark"), edit = @Edit(title = "Remark", type = EditType.TEXTAREA))
    private String remark;

    /**
     * The profiles an instance reads, in override order: {@value #DEFAULT} first, then the
     * active profiles as Spring resolves them (the default profiles when none is active).
     */
    public static List<String> layers(Environment environment) {
        Set<String> layers = new LinkedHashSet<>();
        layers.add(DEFAULT);
        String[] active = environment.getActiveProfiles();
        layers.addAll(Arrays.asList(active.length > 0 ? active : environment.getDefaultProfiles()));
        return List.copyOf(layers);
    }

}
