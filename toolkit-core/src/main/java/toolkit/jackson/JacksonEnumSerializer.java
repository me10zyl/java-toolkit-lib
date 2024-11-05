package toolkit.jackson;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;


public class JacksonEnumSerializer extends JsonSerializer<Enum> {


    @Override
    public void serialize(Enum enumValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (enumValue instanceof IEnum) {
            jsonGenerator.writeObject(((IEnum<?>) enumValue).getValue());
        } else {
            jsonGenerator.writeObject(getEnumMap(enumValue));
        }
    }

    public static <E extends Enum<E>> Map<String, Object> getEnumMap(E e) {
        Map<String, Object> result = BeanUtil.beanToMap(e);
        result.put("enumName", e.name());
        return result;
    }

}
