package xyz.erupt.core.lambda;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LambdaReflect {

    private static final String GET = "get", IS = "is", WRITE_REPLACE = "writeReplace";

    private static final Map<SFunction<?, ?>, LambdaInfo> S_FUNCTION_CACHE = new HashMap<>();

    public static <T, R> String field(SFunction<T, R> func) {
        return info(func).getField();
    }

    public static <T, R> LambdaInfo info(SFunction<T, R> func) {
        try {
            if (S_FUNCTION_CACHE.containsKey(func)) {
                return S_FUNCTION_CACHE.get(func);
            } else synchronized (LambdaReflect.class) {
                if (S_FUNCTION_CACHE.containsKey(func)) return S_FUNCTION_CACHE.get(func);
            }
            Method method = func.getClass().getDeclaredMethod(WRITE_REPLACE);
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            Class<?> clazz = Class.forName(serializedLambda.getImplClass().replace("/", "."));
            LambdaInfo lambdaInfo = getserializedLambdaInfo(serializedLambda, clazz);
            S_FUNCTION_CACHE.put(func, lambdaInfo);
            return lambdaInfo;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static LambdaInfo getserializedLambdaInfo(SerializedLambda serializedLambda, Class<?> clazz) {
        String methodName = serializedLambda.getImplMethodName();
        if (clazz.isAnnotation()) {
            return new LambdaInfo(clazz, methodName, null);
        } else {
            String field = methodName;
            if (methodName.startsWith(GET) && methodName.length() != GET.length())
                field = methodName.substring(GET.length());
            if (methodName.startsWith(IS) && methodName.length() != IS.length())
                field = methodName.substring(IS.length());
            return new LambdaInfo(clazz, methodName, field.substring(0, 1).toLowerCase() + field.substring(1));
        }
    }

}
