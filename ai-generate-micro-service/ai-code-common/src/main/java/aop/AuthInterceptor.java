package aop;

import annotation.AuthCheck;
import enums.UserRoleEnum;
import exception.BusinessException;
import exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class AuthInterceptor {

    
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String userRole = (String) request.getAttribute("userRole");
        UserRoleEnum mstRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if(mstRoleEnum == null){
            return joinPoint.proceed();
        }
        // 校验权限
        UserRoleEnum user = UserRoleEnum.getEnumByValue(userRole);
        if(user==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if(!UserRoleEnum.ADMIN.equals(user)&& UserRoleEnum.ADMIN.equals(mstRoleEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
