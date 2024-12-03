package toolkit.utils;

import java.util.List;
import java.util.function.Function;

public class DiffUtil {

    public static <T, R> Patch<T> diff(List<T> requestObjs, List<T> dbObjects, Function<T, R> idGetter) {
        Patch<T> patch = new Patch<>();
        for (T requestObj : requestObjs) {
            if (dbObjects.stream().noneMatch(e -> idGetter.apply(e).equals(idGetter.apply(requestObj)))) {
                patch.getInsertList().add(requestObj);
            }
        }
        for (T dbObj : dbObjects) {
            if (requestObjs.stream().noneMatch(e -> idGetter.apply(dbObj).equals(idGetter.apply(e)))) {
                patch.getDeleteList().add(dbObj);
            }
        }
        for (T requestObj : requestObjs) {
            if (dbObjects.stream().anyMatch(e -> idGetter.apply(e).equals(idGetter.apply(requestObj)))) {
                patch.getUpdateList().add(requestObj);
            }
        }
        return patch;
    }
}
