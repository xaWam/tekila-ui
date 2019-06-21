package com.jaravir.tekila.ui.jsf.managed.payment;

import com.jaravir.tekila.module.accounting.entity.PaymentOption;
import com.jaravir.tekila.module.payment.PaymentOptionsPersistenceFacade;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Created by kmaharov on 01.07.2016.
 */

@ManagedBean(name = "paymentOptionsManager")
public class PaymentOptionsManager {
    private transient final static Logger log = Logger.getLogger(PaymentOptionsManager.class);
    @EJB
    PaymentOptionsPersistenceFacade paymentOptionsPersistenceFacade;

    private PaymentOption paymentOption;
    private List<PaymentOption> paymentOptionList;

    public PaymentOption getPaymentOption() {
        if (paymentOption == null) {
            paymentOption = new PaymentOption();
        }
        return paymentOption;
    }

    public void setPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
    }

    public String save() {
        if (paymentOption == null || paymentOption.getName() == null
                || paymentOption.getName().isEmpty()) {
            FacesMessage errMessage = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Please enter payment method name",
                    "(It should be non-empty)");
            FacesContext.getCurrentInstance().addMessage("errMsg", errMessage);
            return null;
        }
        try {
            paymentOptionsPersistenceFacade.save(paymentOption);
        } catch (Exception ex) {
            FacesMessage errMessage = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Could not create payment method",
                    ex.getMessage());
            FacesContext.getCurrentInstance().addMessage("errMsg", errMessage);
            return null;
        }
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
        } catch (IOException ioex) {
            log.error("Could not redirect after payment option creation", ioex);
        }
        return null;
    }

    public List<PaymentOption> getPaymentOptionsList() {
        if (paymentOptionList == null) {
            paymentOptionList = paymentOptionsPersistenceFacade.findAll();
        }
        return paymentOptionList;
    }

    public void setPaymentOptionsList(List<PaymentOption> paymentOptionList) {
        this.paymentOptionList = paymentOptionList;
    }
}
