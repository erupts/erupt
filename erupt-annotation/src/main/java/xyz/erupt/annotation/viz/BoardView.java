package xyz.erupt.annotation.viz;

import org.intellij.lang.annotations.Language;

/**
 * @author YuePeng
 * date 2025/10/30 23:52
 */
public @interface BoardView {

    @Language(value = "hql", prefix = "select * from t where ")
    String groupField();

}
