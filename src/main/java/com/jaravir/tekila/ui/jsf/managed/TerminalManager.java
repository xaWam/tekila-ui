package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.engines.EngineFactory;
import com.jaravir.tekila.module.accounting.periodic.BillingManager;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.engines.CitynetProvisioner;
import com.jaravir.tekila.provision.exception.ProvisionerNotFoundException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.*;

@ManagedBean
@ViewScoped
public class TerminalManager implements Serializable {
    private static Logger log = Logger.getLogger(TerminalManager.class);
    Map<String, Command> commands;

    @EJB
    private BillingManager billingManager;
    @EJB
    private EngineFactory provisioningFactory;
    @EJB
    private SubscriptionPersistenceFacade subscriptionFacade;

    public TerminalManager() {
        commands = new HashMap<>();
        commands.put("activate", new Activation());
        commands.put("bill", new Billing());
    }


    public String run(String command, String[] params) {
        try {

            log.debug(seeInput(command, params));

            if (command.equals("help")) {
                return getHelp();
            }

            if (command.equals("date")) {
                return new Date().toString();
            }

            Iterator it = commands.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (command.equals(pair.getKey())) {
                    log.debug("pair.getKey(): " + pair.getKey() + " pair.getValue(): " + pair.getValue());
                    if (params.length > 0) {
                        return ((Command) pair.getValue()).run(params);
                    } else {
                        return ((Command) pair.getValue()).run();
                    }
                }
            }

            return command + " not found";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ERROR: " + ex;
        }
    }

    public String getHelp() {
        Iterator it = commands.entrySet().iterator();
        String result = "Commands: ";
        if (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            result += pair.getKey();
        }
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            result += " / " + pair.getKey();
        }
        return result;
    }

    public String seeInput(String command, String[] params) {
        String result = "Command: " + command + " ; ";

        if (params.length > 0) {
            result += params[0];
            for (int i = 1; i < params.length; i++) {
                result += " / " + params[i];
            }
        }
        return result;
    }

    interface Command {

        public String run();

        public String run(String[] params);
    }

    class Activation implements Command {
        public Activation() {
            super();
        }


        public String run() {
            try {
//                billingManager.manageActivationForCitynet();
                return "Ok";
            } catch (Exception ex) {
                ex.printStackTrace();
                return "Error : " + ex;
            }

        }


        public String run(String[] params) {

            try {
                Subscription subscription = null;
                try {
                    log.debug("subscriptionFacade: " + subscriptionFacade);
                    subscription = subscriptionFacade.findByAgreementOrdinary(params[0]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "Subscription not found : " + ex;
                }

                if (subscription.getActivationDate() != null) {
                    return "Subscription already activated ! Activation Date: " + subscription.getActivationDate();
                }

                CitynetProvisioner provisioner;
                try {
                    provisioner = (CitynetProvisioner) provisioningFactory.getProvisioningEngine(subscription);
                } catch (ProvisionerNotFoundException ex) {
                    ex.printStackTrace();
                    return "ProvisionerNotFoundException";
                }
                DateTime radiusFD = provisioner.getActivationDate(subscription);
                subscription.setActivationDate(radiusFD);
                subscriptionFacade.update(subscription);
                return "Subscription " + subscription.getAgreement() + " has been Activated";
            } catch (Exception ex) {
                ex.printStackTrace();
                return "Error : " + ex;
            }
        }
    }

    class Billing implements Command {

        @Override
        public String run() {
            return "Please specify an agreement";
        }

        @Override
        public String run(String[] params) {

            try {
                Subscription subscription = null;
                try {
                    subscription = subscriptionFacade.findByAgreementOrdinary(params[0]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "Subscription not found : " + ex;
                }

//                citynetBilling.billing(subscription);
                return "Ok";

            } catch (Exception ex) {
                ex.printStackTrace();
                return "Error : " + ex;
            }
        }
    }


}
