package xyz.erupt.flow.service;

import xyz.erupt.flow.process.listener.ExecutableNodeListener;

import java.util.List;
import java.util.Map;

public interface WithListener {

    Map<Class<ExecutableNodeListener>, List<ExecutableNodeListener>> getListenerMap();
}
