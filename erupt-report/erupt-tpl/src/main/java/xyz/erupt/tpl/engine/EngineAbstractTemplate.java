package xyz.erupt.tpl.engine;

/**
 * @author liyuepeng
 * @date 2021/1/16 20:19
 */
public abstract class EngineAbstractTemplate<E> implements EngineTemplate<E> {

    private E engine;

    public E getEngine() {
        return engine;
    }

    public void setEngine(E engine) {
        this.engine = engine;
    }
}
