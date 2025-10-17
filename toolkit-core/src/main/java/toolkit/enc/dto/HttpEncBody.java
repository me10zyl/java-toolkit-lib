package toolkit.enc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HttpEncBody {

    private String encryptKey;
    private String encryptContent;
    private String signature;

}
