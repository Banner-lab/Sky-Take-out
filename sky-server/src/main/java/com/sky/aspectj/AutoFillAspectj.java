package com.sky.aspectj;

import com.sky.anno.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @ClassName AutoFillAspectj
 * @Description 公共字段填充
 * @Author XMING
 * @Date 2023/5/10 9:51
 * @Version 1.0
 */
@Aspect
@Component
public class AutoFillAspectj {
    /**
     * 切点表达式
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.anno.AutoFill)")
    public void autoFill() {
    }

    /**
     * 公共字段填充
     * @param pjp
     * @throws Throwable
     */
    @Around("autoFill() && @annotation(fill)")
    public Object autoFill(ProceedingJoinPoint pjp, AutoFill fill) throws Throwable {
        //获取实体对象
        Object[] args = pjp.getArgs();
        Object pojo = args[0];
        //判断使用autoFill注解的方法是insert 还是update
        OperationType isInsert = fill.value();

        //pojo.getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(pojo, LocalDateTime.now());
        //pojo.getClass().getMethod("setUpdateUser",Long.class).invoke(pojo, BaseContext.getCurrentId());

        ////插入操作需要额外插入两个字段
        //if(isInsert == OperationType.INSERT){
        //    pojo.getClass().getMethod("setCreateTime", LocalDateTime.class).invoke(pojo, LocalDateTime.now());
        //    pojo.getClass().getMethod("setCreateUser",Long.class).invoke(pojo, BaseContext.getCurrentId());
        //}

        BeanUtils.findMethod(pojo.getClass(), "setUpdateTime", LocalDateTime.class).invoke(pojo, LocalDateTime.now());
        BeanUtils.findMethod(pojo.getClass(), "setUpdateUser",Long.class).invoke(pojo,BaseContext.getCurrentId());

        if(isInsert == OperationType.INSERT){
            BeanUtils.findMethod(pojo.getClass(), "setCreateTime", LocalDateTime.class).invoke(pojo, LocalDateTime.now());
            BeanUtils.findMethod(pojo.getClass(), "setCreateUser",Long.class).invoke(pojo,BaseContext.getCurrentId());
        }


        Object result = pjp.proceed(args);
        return result;
    }
}
