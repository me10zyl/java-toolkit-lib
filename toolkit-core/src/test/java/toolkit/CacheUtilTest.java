package toolkit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static toolkit.utils.CacheUtil.useCacheMap;

public class CacheUtilTest {

    @Test
    public void test1(){
        HashMap<String, Object> cacheMap = new HashMap<>();
        ArrayList<Object> objects1 = getAa(cacheMap);
        ArrayList<Object> objects2 = getAa(cacheMap);
        ArrayList<Object> objects3 = getAa(cacheMap);
        System.out.println(objects1);
        assertTrue(objects1 == objects2 && objects1 == objects3);
    }

    private static ArrayList<Object> getAa(HashMap<String, Object> cacheMap) {
        return useCacheMap("aa", () -> 1, (k) -> {
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(2);
            objects.add(3);
            objects.add(4);
            return objects;
        }, cacheMap);
    }
}
