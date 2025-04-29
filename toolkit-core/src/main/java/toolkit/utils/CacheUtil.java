package toolkit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CacheUtil {
    public static <K,V> V useCacheMap(String mapKey, Supplier<K> key, Function<K, V> func, Map<String, Object> cacheMap) {
        cacheMap.putIfAbsent(mapKey, new HashMap());
        Map<K, V> existsMap = (Map<K, V>) cacheMap.get(mapKey);

        K key1 = key.get();
        if(!existsMap.containsKey(key1)){
            K t = key.get();
            V apply = func.apply(t);
            existsMap.put(t, apply);
            return apply;
        }
        return existsMap.get(key1);
    }
}
