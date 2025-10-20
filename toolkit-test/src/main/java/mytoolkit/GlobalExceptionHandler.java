//package mytoolkit;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import toolkit.enc.exception.EncException;
//
//@ControllerAdvice
//@Slf4j
//@Component
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(EncException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public String processException(EncException e) {
//        log.error(e.getMessage(), e);
//        return e.getMessage();
//    }
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public String processException2(Exception e) {
//        log.error(e.getMessage(), e);
//        return e.getMessage();
//    }
//}
