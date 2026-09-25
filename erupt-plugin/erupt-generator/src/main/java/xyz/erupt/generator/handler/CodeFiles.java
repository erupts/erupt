package xyz.erupt.generator.handler;

import xyz.erupt.generator.model.GeneratorClass;

/**
 * How a generated class is named once it leaves the browser.
 *
 * @author YuePeng
 * date 2026-09-17
 */
class CodeFiles {

    static String name(GeneratorClass clazz) {
        return clazz.getClassName() + ".java";
    }

    /**
     * The package becomes a path, so an archive unzips straight over a source tree.
     */
    static String path(GeneratorClass clazz) {
        String pkg = null == clazz.getPackageName() || clazz.getPackageName().isEmpty()
                ? "" : clazz.getPackageName().replace('.', '/') + "/";
        return pkg + name(clazz);
    }

    private CodeFiles() {
    }

}
