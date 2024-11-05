package toolkit.utils;


import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 简简单单的使用stream
 */
public class StreamUtil {

    public static <V,E> Map<V, E> toMap(Collection<E> collection, Function<E, V> keyMapper){
        return collection.stream().collect(Collectors.toMap(keyMapper, e->e, (k1,k2)->k1));
    }

    public static <K,E> Map<K, List<E>> groupBy(Collection<E> collection,Function<E, K> keyMapper){
        return collection.stream().collect(Collectors.groupingBy(keyMapper));
    }

    public static <E, K> List<K> distinct(Collection<E> collection, Function<E, K> keyMapper){
        return mapId(collection, keyMapper).stream().distinct().collect(Collectors.toList());
    }

    public static <E> BigDecimal sum(Collection<E> collection, Function<E,BigDecimal> vMapper){
        if(collection == null){
            return BigDecimal.ZERO;
        }
        return collection.stream().map(vMapper).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }


    public static <L,E> List<L> mapId(Collection<E> collection, Function<E, L> idMapper){
        if(collection == null){
            return new ArrayList<>();
        }
        return collection.stream().map(idMapper).collect(Collectors.toList());
    }

    public static <L,E> List<L> flatMapId(Collection<E> collection, Function<E, Stream<L>> idMapper){
        if(collection == null){
            return new ArrayList<>();
        }
        return collection.stream().flatMap(idMapper).collect(Collectors.toList());
    }

    public static <E> List<E> filterNonNull(Collection<E> collection){
        return filter(collection, Objects::nonNull);
    }
    public static <E> List<E> filter(Collection<E> collection, Predicate<? super E> predicate){
        if(collection == null){
            return null;
        }
        return collection.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <E> Optional<E> findFirst(Collection<E> collection, Predicate<? super E> predicate){
        if(collection == null){
            return null;
        }
        return collection.stream().filter(predicate).findFirst();
    }

    public static <E> boolean anyMatch(Collection<E> collection,Predicate<? super E> predicate){
        return collection.stream().anyMatch(predicate);
    }

    public static <E> boolean noneMatch(Collection<E> collection,Predicate<? super E> predicate){
        return collection.stream().noneMatch(predicate);
    }
}
