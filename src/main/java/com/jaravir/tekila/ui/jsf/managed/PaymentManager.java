/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.base.auth.persistence.ExternalUser;
import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.ExternalUserPersistenceFacade;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.module.accounting.entity.Payment;
import com.jaravir.tekila.module.accounting.manager.PaymentPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import com.jaravir.tekila.ui.model.SubscriptionLazyTableModel;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class PaymentManager implements Serializable {
    @EJB
    private PaymentPersistenceFacade paymentFacade;  
    @EJB
    private SubscriptionPersistenceFacade subFacade;
    @EJB
    private UserPersistenceFacade userFacade;
    @EJB
    private ExternalUserPersistenceFacade extUserFacade;
    
    private final static String PAYMENT_PATH = "/pages/payments/";
    private LazyTableModel<Subscription> subscriptionList;
    private List<Subscription> filteredSubscriptionList;
    private Subscription selectedSubscription;
    private Payment payment;
    private Payment selected;
    private LazyTableModel<Payment> paymentList;
    private List<Payment> filteredPaymentList;
    private User user;
    private ExternalUser extUser;
    
    @PostConstruct
    public void init () {
        payment = new Payment();
    }

    public LazyTableModel<Subscription> getSubscriptionList() {
        if (subscriptionList == null)
            subscriptionList = new LazyTableModel<Subscription>(subFacade);
        
        return subscriptionList;
    }

    public void setSubscriptionList(LazyTableModel<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public Subscription getSelectedSubscription() {
        return selectedSubscription;
    }

    public void setSelectedSubscription(Subscription selectedSubscription) {
        this.selectedSubscription = selectedSubscription;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }    

    public List<Subscription> getFilteredSubscriptionList() {
        return filteredSubscriptionList;
    }

    public void setFilteredSubscriptionList(List<Subscription> filteredSubscriptionList) {
        this.filteredSubscriptionList = filteredSubscriptionList;
    }
    
    public String save() {
        paymentFacade.save(payment);
        return PAYMENT_PATH + "index";
    }
    
    public String create() {
       HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
       payment.setAccount(selectedSubscription);
       payment.setSubscriber_id(selectedSubscription.getSubscriber().getId());
       payment.setContract(selectedSubscription.getAgreement());
       payment.setServiceId(selectedSubscription.getService());
       payment.setCurrency("AZN");

       //payment.setExtUserId(null);
       payment.setUser(userFacade.find((Long)session.getAttribute("userID")));
       
       //payment.setUserId((Long)session.getAttribute("userID"));
       paymentFacade.save(payment);
       
        //GatewayClient client = new GatewayClient();
        //client.callSettlePayment(payment.getSubscriber_id(), payment.getAccount().getId(), payment.getAmount(), payment.getId());
       
       return PAYMENT_PATH + "index";
    }

    public Payment getSelected() {
        return selected;
    }

    public void setSelected(Payment selected) {
        this.selected = selected;
    }

    public LazyTableModel<Payment> getPaymentList() {
        if (paymentList == null)
            paymentList = new LazyTableModel<Payment>(paymentFacade);
        return paymentList;
    }

    public void setPaymentList(LazyTableModel<Payment> paymentList) {        
        this.paymentList = paymentList;
    }

    public List<Payment> getFilteredPaymentList() {
        return filteredPaymentList;
    }

    public void setFilteredPaymentList(List<Payment> filteredPaymentList) {
        this.filteredPaymentList = filteredPaymentList;
    }
    
    
}
