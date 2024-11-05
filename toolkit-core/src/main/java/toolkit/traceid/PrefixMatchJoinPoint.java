package toolkit.traceid;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Component
@RequiredArgsConstructor
public class PrefixMatchJoinPoint extends DynamicMethodMatcherPointcut {

    private final String prefix;

    @Override
    public ClassFilter getClassFilter() {
        return new ClassFilter() {
            @Override
            public boolean matches(Class<?> aClass) {
                return aClass.getName().startsWith(prefix);
            }
        };
    }

    @Override
    public boolean matches(Method method, Class<?> aClass, Object... objects) {
        return false;
    }
}
