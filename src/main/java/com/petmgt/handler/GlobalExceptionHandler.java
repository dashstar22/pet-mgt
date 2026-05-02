package com.petmgt.handler;

import com.petmgt.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404() {
        return "404";
    }

    @ExceptionHandler(BusinessException.class)
    public RedirectView handleBusinessException(BusinessException e,
                                                 HttpServletRequest request,
                                                 RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && !referer.isBlank()) ? referer : "/";
        return new RedirectView(redirectUrl);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e) {
        log.error("Unhandled exception", e);
        return "error";
    }
}
