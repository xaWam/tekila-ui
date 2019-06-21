package com.jaravir.tekila.ui.jsf.exception.handler;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * Created by sajabrayilov on 25.02.2015.
 */
public class TekilaExceptionFactory extends ExceptionHandlerFactory{
    private ExceptionHandlerFactory factory;

    public TekilaExceptionFactory (ExceptionHandlerFactory factory) {
        this.factory = factory;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler exceptionHandler = factory.getExceptionHandler();
        exceptionHandler = new TekilaExceptionHandler(exceptionHandler);
        return exceptionHandler;
    }
}
