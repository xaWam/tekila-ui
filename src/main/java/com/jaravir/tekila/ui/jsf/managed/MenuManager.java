package com.jaravir.tekila.ui.jsf.managed;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.jaravir.tekila.base.auth.Privilege;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import com.jaravir.tekila.base.auth.persistence.manager.SecurityManager;

import java.awt.MenuItem;

import org.primefaces.model.menu.MenuElement;

@ManagedBean(name = "menu")
@SessionScoped
public class MenuManager implements Serializable {

    private MenuModel menuModel;
    @EJB
    private SecurityManager securityManager;

    @PostConstruct
    public void init() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages");
        this.menuModel = new DefaultMenuModel();
        DefaultMenuItem search = new DefaultMenuItem("Search", "ui-icon-search", "/admin.xhtml");
        this.menuModel.addElement(search);

        //Subscribers
        if (securityManager.checkUIPermissionsForModule("Subscription", Privilege.READ)) {
            /*
             DefaultSubMenu subModule = new DefaultSubMenu("Customers");
             DefaultMenuItem sub = new DefaultMenuItem("Individual subscribers", null, "/pages/subscriber/ind/index.xhtml");
             sub.setRendered(securityManager.checkUIPermissions("Subscriber", Privilege.READ));
             subModule.addElement(sub);

             DefaultMenuItem subCorp = new DefaultMenuItem("Corporate subscribers", null, "/pages/subscriber/corp/index.xhtml");
             subCorp.setRendered(securityManager.checkUIPermissions("Subscriber", Privilege.READ));
             subModule.addElement(subCorp);

             DefaultMenuItem sbn = new DefaultMenuItem("Subscriptions", null, "/pages/subscription/index.xhtml");
             //subModule.addElement(sbn);
             sbn.setRendered(securityManager.checkUIPermissions("Subscription", Privilege.READ));
             */
            DefaultSubMenu subModule = new DefaultSubMenu("Customers", "ui-icon-person");
            DefaultSubMenu sub = new DefaultSubMenu("Add subscriber");
            sub.setRendered(securityManager.checkUIPermissions("Subscriber", Privilege.INSERT));

            DefaultMenuItem subInd = new DefaultMenuItem("Individual", null, "/pages/subscriber/ind/create_subscriber_ind.xhtml");
            subInd.setRendered(securityManager.checkUIPermissions("Subscriber", Privilege.INSERT));

            DefaultMenuItem subCorp = new DefaultMenuItem("Corporate", null, "/pages/subscriber/corp/create_subscriber_corp.xhtml");
            subCorp.setRendered(securityManager.checkUIPermissions("Subscriber", Privilege.INSERT));

            sub.addElement(subInd);
            sub.addElement(subCorp);

            subModule.addElement(sub);
            subModule.setRendered(securityManager.checkUIPermissions("Subscriber", Privilege.READ));

            this.menuModel.addElement(subModule);
        }
        //Services
        if (securityManager.checkUIPermissionsForModule("Service", Privilege.READ)) {

            DefaultSubMenu serviceModule = new DefaultSubMenu("Service", "ui-icon-clipboard");
            DefaultMenuItem service = new DefaultMenuItem("Services", null, "/pages/service/index.xhtml");
            serviceModule.addElement(service);
            service.setRendered(securityManager.checkUIPermissions("Service", Privilege.READ));

            DefaultMenuItem serviceSubGroup = new DefaultMenuItem("Service subgroups", null, "/pages/service/subgroup/index.xhtml");
            serviceModule.addElement(serviceSubGroup);
            service.setRendered(securityManager.checkUIPermissions("Service subgroup", Privilege.READ));


            /*
            * DefaultMenuItem rateProfile = new DefaultMenuItem("Rate profiles", null, "/pages/subscription/profile/index.xhtml");
            serviceModule.addElement(rateProfile);
            rateProfile.setRendered(securityManager.checkUIPermissions("Rate profile", Privilege.READ));

            DefaultMenuItem rate = new DefaultMenuItem("Rates", null, "/pages/subscription/rate/index.xhtml");
            serviceModule.addElement(rate);
            rate.setRendered(securityManager.checkUIPermissions("Rate", Privilege.READ));
            * */

            DefaultMenuItem res = new DefaultMenuItem("Resources", null, "/pages/resource/index.xhtml");
            serviceModule.addElement(res);
            res.setRendered(securityManager.checkUIPermissions("Resource", Privilege.READ));

            DefaultMenuItem resBucket = new DefaultMenuItem("Resource buckets", null, "/pages/bucket/index.xhtml");
            serviceModule.addElement(resBucket);
            resBucket.setRendered(securityManager.checkUIPermissions("Resource bucket", Privilege.READ));

            DefaultMenuItem vas = new DefaultMenuItem("Value Added Services", null, "/pages/service/vas/index.xhtml");
            serviceModule.addElement(vas);
            vas.setRendered(securityManager.checkUIPermissions("Value Added Services", Privilege.READ));

            DefaultMenuItem vasCode = new DefaultMenuItem("VAS codes", null, "/pages/service/vascode/index.xhtml");
            serviceModule.addElement(vasCode);
            vasCode.setRendered(securityManager.checkUIPermissions("VAS codes", Privilege.READ));

            DefaultMenuItem campaigns = new DefaultMenuItem("Campaigns", null, "/pages/campaigns/index.xhtml");
            campaigns.setRendered(securityManager.checkUIPermissions("Campaigns", Privilege.READ));
            serviceModule.addElement(campaigns);
            this.menuModel.addElement(serviceModule);
        }

        //Finance
        if (securityManager.checkUIPermissionsForModule("Finance", Privilege.READ)) {
            DefaultSubMenu financeModule = new DefaultSubMenu("Finance", "ui-icon-calculator");
            DefaultMenuItem banks = new DefaultMenuItem("Banks", null, "/pages/finance/banks/index.xhtml");
            banks.setRendered(securityManager.checkUIPermissions("Banks", Privilege.READ));

            financeModule.addElement(banks);

            DefaultMenuItem accCategories = new DefaultMenuItem("Accounting categories", null, "/pages/finance/categories/index.xhtml");
            accCategories.setRendered(securityManager.checkUIPermissions("Accounting categories", Privilege.READ));
            financeModule.addElement(accCategories);

            DefaultMenuItem trans = new DefaultMenuItem("Transactions", null, "/pages/finance/acc_trans/index.xhtml");
            financeModule.addElement(trans);
            trans.setRendered(securityManager.checkUIPermissions("Transactions", Privilege.READ));

            DefaultSubMenu batchPaymentSubMenu = new DefaultSubMenu("Batch Payments");
            batchPaymentSubMenu.addElement(new DefaultMenuItem("Bank", null, "/pages/finance/batch/payment.xhtml"));
            batchPaymentSubMenu.addElement(new DefaultMenuItem("VAT", null, "/pages/finance/batch/payment_vat.xhtml"));

            batchPaymentSubMenu.setRendered(securityManager.checkUIPermissions("Batch payment", Privilege.READ));

            DefaultSubMenu batchAdjSubMenu = new DefaultSubMenu("Batch Adjustment");
            batchAdjSubMenu.addElement(new DefaultMenuItem("From balance", null, "/pages/finance/batch/adj_debit.xhtml"));
            batchAdjSubMenu.addElement(new DefaultMenuItem("To balance", null, "/pages/finance/batch/adj_credit.xhtml"));
            batchAdjSubMenu.setRendered(securityManager.checkUIPermissions("Batch adjustment", Privilege.READ));
            financeModule.addElement(batchPaymentSubMenu);
            financeModule.addElement(batchAdjSubMenu);

            DefaultMenuItem paymentOptionsMenu = new DefaultMenuItem("Payment Options", null, "/pages/finance/payment/payment_options.xhtml");
            financeModule.addElement(paymentOptionsMenu);
            this.menuModel.addElement(financeModule);
        }

        //Network
        if (securityManager.checkUIPermissionsForModule("Stock", Privilege.READ)) {
            DefaultSubMenu networkModule = new DefaultSubMenu("Stock", "ui-icon-cart");
            DefaultMenuItem equipMenu = new DefaultMenuItem("Equipment", null, "/pages/store/equip/index.xhtml");
            networkModule.addElement(equipMenu);
            equipMenu.setRendered(securityManager.checkUIPermissions("Equipment", Privilege.READ));

            if (securityManager.checkUIPermissions("Distributor", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Distributors", null, "/pages/store/distrib/index.xhtml"));
            }

            if (securityManager.checkUIPermissions("Distributor stores", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Distributor stores", null, "/pages/store/distrib/store/index.xhtml"));
            }

            if (securityManager.checkUIPermissions("Dealer", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Dealer", null, "/pages/store/dealer/index.xhtml"));
            }

            if (securityManager.checkUIPermissions("Dealer stores", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Dealer stores", null, "/pages/store/dealer/store/index.xhtml"));
            }

            if (securityManager.checkUIPermissions("Scratch card", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Scratch card", null, "/pages/store/scratchcard/index.xhtml"));
            }

            if (securityManager.checkUIPermissions("Ats", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Ats", null, "/pages/store/ats/index.xhtml"));
                networkModule.addElement(new DefaultMenuItem("Ats Govs", null, "/pages/store/ats/govs/govs.xhtml"));
            }

            if (securityManager.checkUIPermissions("Streets", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Streets", null, "/pages/store/street/index.xhtml"));
            }

            DefaultMenuItem midipops = new DefaultMenuItem("Midipops", null, "/pages/store/midipop/index.xhtml");
            midipops.setRendered(securityManager.checkUIPermissions("Midipop", Privilege.READ));
            networkModule.addElement(midipops);


            DefaultMenuItem minipops = new DefaultMenuItem("Minipops", null, "/pages/minipops/index.xhtml");
            minipops.setRendered(securityManager.checkUIPermissions("Minipops", Privilege.READ));
            networkModule.addElement(minipops);

            DefaultSubMenu radiusMenu = new DefaultSubMenu("Radius");
            boolean isNasRead = securityManager.checkUIPermissions("Nas", Privilege.READ);

            if (isNasRead) {
                DefaultMenuItem nasSubMenu = new DefaultMenuItem("Nas", null, "/pages/store/radius/nas/index.xhtml");
                radiusMenu.addElement(nasSubMenu);
            }

            boolean isAttRead = securityManager.checkUIPermissions("Attribute", Privilege.READ);

            if (isAttRead) {
                DefaultMenuItem attSubMenu = new DefaultMenuItem("Attribute", null, "/pages/store/radius/attribute/index.xhtml");
                radiusMenu.addElement(attSubMenu);
            }

            if (isNasRead || isAttRead)
                networkModule.addElement(radiusMenu);

            DefaultMenuItem ipRange = new DefaultMenuItem("IP address range", null, "/pages/store/range/index.xhtml");
            ipRange.setRendered(securityManager.checkUIPermissions("IP Address", Privilege.READ));
            networkModule.addElement(ipRange);

            DefaultSubMenu mobileMenu = new DefaultSubMenu("Mobile");

            boolean isImsiReadd = securityManager.checkUIPermissions("IMSI", Privilege.READ);

            if (isImsiReadd) {
                DefaultMenuItem imsiSubMenu = new DefaultMenuItem("IMSI", null, "/pages/store/mobile/imsi/index.xhtml");
                mobileMenu.addElement(imsiSubMenu);
            }

            boolean isMsdnRead = securityManager.checkUIPermissions("MSISDN", Privilege.READ);

            if (isMsdnRead) {
                DefaultMenuItem msisdnMenu = new DefaultMenuItem("MSISDN", null, "/pages/store/mobile/msisdn/index.xhtml");
                mobileMenu.addElement(msisdnMenu);
            }

            if (isImsiReadd || isMsdnRead) {
                networkModule.addElement(mobileMenu);
            }

            if (securityManager.checkUIPermissions("CommercialCategory", Privilege.READ)) {
                networkModule.addElement(new DefaultMenuItem("Commercial category", null, "/pages/store/comcat/index.xhtml"));
            }

            this.menuModel.addElement(networkModule);
        }
        //Statistics
        if (securityManager.checkUIPermissionsForModule("Report", Privilege.READ)) {
            DefaultSubMenu statsModule = new DefaultSubMenu("Reports", "ui-icon-pencil");
            DefaultMenuItem paymentReport = new DefaultMenuItem("Payments", null, "/pages/stats/payments.xhtml");
            statsModule.addElement(paymentReport);
            paymentReport.setRendered(
                    securityManager.checkUIPermissions("PaymentReport", Privilege.READ)
                            || securityManager.checkUIPermissions("OwnPaymentReport", Privilege.READ)
            );

            DefaultMenuItem onlineUsers = new DefaultMenuItem("Online users", null, "/pages/stats/online.xhtml");
            statsModule.addElement(onlineUsers);
            onlineUsers.setRendered(securityManager.checkUIPermissions("Online users", Privilege.READ));

            DefaultMenuItem offlineSessions = new DefaultMenuItem("Offline sessions", null, "/pages/stats/offline.xhtml");
            statsModule.addElement(offlineSessions);
            offlineSessions.setRendered(securityManager.checkUIPermissions("Offline sessions", Privilege.READ));
            this.menuModel.addElement(statsModule);
        }
        //Notifications
        if (securityManager.checkUIPermissionsForModule("Notification", Privilege.READ)) {
            DefaultSubMenu notificationsMenu = new DefaultSubMenu("Notifications", "ui-icon-mail-closed");
            DefaultMenuItem notif = new DefaultMenuItem("Notifications", null, "/pages/notifications/index.xhtml");
            notificationsMenu.addElement(notif);
            notif.setRendered(securityManager.checkUIPermissions("Notification", Privilege.READ));
            DefaultMenuItem channel = new DefaultMenuItem("NotificationStatus", null, "/pages/notifications/channelstatus.xhtml");
            channel.setRendered(securityManager.checkUIPermissions("NotificationChannelStatus", Privilege.UPDATE));
            notificationsMenu.addElement(channel);
            this.menuModel.addElement(notificationsMenu);
        }
        //Tools
        if (securityManager.checkUIPermissionsForModule("Tools", Privilege.READ)) {
            DefaultSubMenu toolsMenu = new DefaultSubMenu("Tools", "ui-icon-gear");
            DefaultSubMenu batchOperations = new DefaultSubMenu("Batch Operations");
            toolsMenu.addElement(batchOperations);

            DefaultMenuItem btfs = new DefaultMenuItem("Status change", null, "/pages/tools/status_change.xhtml");
            batchOperations.addElement(btfs);

            DefaultMenuItem agreementChangeSubMenu = new DefaultMenuItem("Agreement change", null, "/pages/tools/agreement_change.xhtml");
            batchOperations.addElement(agreementChangeSubMenu);

            btfs.setRendered(securityManager.checkUIPermissions("Batch Agreement changes", Privilege.READ));

            this.menuModel.addElement(toolsMenu);
        }
        //Administration
        if (securityManager.checkUIPermissionsForModule("Administration", Privilege.READ)) {
            DefaultSubMenu adminMenu = new DefaultSubMenu("Administration", "ui-icon-wrench");
            adminMenu.addElement(new DefaultMenuItem("Modules", null, "/pages/admin/modules/index.xhtml"));
            adminMenu.addElement(new DefaultMenuItem("Submodules", null, "/pages/admin/submodules/index.xhtml"));
            adminMenu.addElement(new DefaultMenuItem("Users", null, "/pages/admin/users/index.xhtml"));
            adminMenu.addElement(new DefaultMenuItem("Groups", null, "/pages/admin/groups/index.xhtml"));
            adminMenu.addElement(new DefaultMenuItem("Roles", null, "/pages/admin/roles/index.xhtml"));

            this.menuModel.addElement(adminMenu);
        }
        //payments

        DefaultSubMenu payments = new DefaultSubMenu("Payments");
        DefaultMenuItem paymentsCreate = new DefaultMenuItem("Create");
        paymentsCreate.setUrl("/pages/payments/create.xhtml");
        DefaultMenuItem paymentsIndex = new DefaultMenuItem("View");
        paymentsIndex.setUrl("/pages/payments/index.xhtml");
        payments.addElement(paymentsCreate);
        payments.addElement(paymentsIndex);

        payments.setRendered(true);


    }

    public MenuModel getMenuModel() {
        return this.menuModel;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}
