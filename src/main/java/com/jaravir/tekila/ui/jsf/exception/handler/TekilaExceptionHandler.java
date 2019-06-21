package com.jaravir.tekila.ui.jsf.exception.handler;

import org.apache.log4j.Logger;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import java.util.Iterator;

/**
 * Created by sajabrayilov on 25.02.2015.
 */
public class TekilaExceptionHandler extends ExceptionHandlerWrapper {
    private final static Logger log = Logger.getLogger(TekilaExceptionHandler.class);
    private ExceptionHandler handler;

    public TekilaExceptionHandler(ExceptionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator();
        ExceptionQueuedEvent ev = null;

        while (it.hasNext()) {
            ev = it.next();
            Throwable ex = ev.getContext().getException();

            if (ex instanceof ViewExpiredException) {
                FacesContext ctx = FacesContext.getCurrentInstance();
                try {
                    ctx.setViewRoot(ctx.getApplication().getViewHandler().createView(ctx, "/login.xhtml"));
                    ctx.getExternalContext().redirect("/login.xhtml");
                    ctx.getPartialViewContext().setRenderAll(true);
                    ctx.renderResponse();
                }
                catch (Exception exception) {
                    log.error("Cannot handle ViewExpiredException", exception);
                }
                finally {
                    it.remove();
                }
            }

            getWrapped().handle();
        }
    }

    @Override
    public ExceptionHandler getWrapped() {
        return handler;
    }
}
