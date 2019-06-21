/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.accounting.entity.Payment;
import com.jaravir.tekila.module.accounting.manager.PaymentPersistenceFacade;
import com.jaravir.tekila.module.admin.AdminSettingPersistenceFacade;
import com.jaravir.tekila.module.auth.security.EncryptorAndDecryptor;
import com.jaravir.tekila.module.auth.security.PasswordGenerator;
import com.jaravir.tekila.module.campaign.CampaignJoinerBean;
import com.jaravir.tekila.module.campaign.CampaignPersistenceFacade;
import com.jaravir.tekila.module.campaign.CampaignRegisterPersistenceFacade;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.store.BatchIDPersistenceFacade;
import com.jaravir.tekila.module.store.ScratchCardPersistenceFacade;
import com.jaravir.tekila.module.store.ScratchCardSessionPersistenceFacade;
import com.jaravir.tekila.module.store.SerialPersistenceFacade;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.BatchID;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.ScratchCard;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.ScratchCardSession;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.Serial;
import com.jaravir.tekila.module.subscription.persistence.entity.Subscription;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriptionPersistenceFacade;
import com.jaravir.tekila.module.web.service.provider.BillingServiceProvider;
import com.jaravir.tekila.ui.model.LazyTableModel;
import com.jaravir.tekila.ui.model.LazyTableModelDynamic;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

/**
 * @author shnovruzov
 */
@ManagedBean
@ViewScoped
public class ScratchCardManager implements Serializable {

    private transient final static Logger log = Logger.getLogger(ScratchCard.class);
    private final static String PATH = "/pages/store/scratchcard";

    @EJB
    ScratchCardPersistenceFacade scratchCardFacade;
    @EJB
    BatchIDPersistenceFacade batchPersistenceFacade;
    @EJB
    EncryptorAndDecryptor encryptorAndDecryptor;
    @EJB
    SerialPersistenceFacade serialFacade;
    @EJB
    PaymentPersistenceFacade paymentPersistenceFacade;
    @EJB
    SubscriptionPersistenceFacade subscriptionPersistenceFacade;
    @EJB
    private BillingServiceProvider serviceProvider;
    @EJB
    private CampaignPersistenceFacade campaignFacade;
    @EJB
    private CampaignJoinerBean campaignJoinerBean;
    @EJB
    private CampaignRegisterPersistenceFacade campaignRegisterFacade;
    @EJB
    private AdminSettingPersistenceFacade adminSettingPersistenceFacade;
    @EJB
    private ScratchCardSessionPersistenceFacade sessionPersistenceFacade;

    static final long LOWER_RANGE = 10000000L;
    static final long UPPER_RANGE = 90000000L;

    private Long amount;
    private Date validFrom;
    private Date validTo;
    private Long count;
    private Long status;
    private Long serial;
    private Long batchID;
    private Long subscriber;
    private Long subscription;

    private HashMap<Long, Boolean> generated;
    private List<String> pins;
    private Random random;
    private List<ScratchCard> scratchCards;
    private List<SelectItem> amountSelectList;
    private LazyDataModel<ScratchCard> cardList;

    private ScratchCard selectedCard;
    private long selectedCardId;
    private String sqlQuery;
    private ScratchCardSession cardSession;

    Object lock;

    private Object getLockObject() {
        if (lock == null) {
            lock = new Object();
        }
        return lock;
    }

    public Long getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Long subscriber) {
        this.subscriber = subscriber;
    }

    public Long getSubscription() {
        return subscription;
    }

    public void setSubscription(Long subscription) {
        this.subscription = subscription;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<SelectItem> getAmountSelectList() {

        if (amountSelectList != null) {
            return amountSelectList;
        }

        amountSelectList = new ArrayList();
        amountSelectList.add(new SelectItem(1, "1"));
        amountSelectList.add(new SelectItem(3, "3"));
        amountSelectList.add(new SelectItem(5, "5"));
        amountSelectList.add(new SelectItem(10, "10"));
        amountSelectList.add(new SelectItem(20, "20"));

        return amountSelectList;

    }

    public void setAmountSelectList(List<SelectItem> amountSelectList) {
        this.amountSelectList = amountSelectList;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getSerial() {
        return serial;
    }

    public void setSerial(Long serial) {
        this.serial = serial;
    }

    public Long getBatchID() {
        return batchID;
    }

    public void setBatchID(Long batchID) {
        this.batchID = batchID;
    }

    public void init() {
        scratchCardFacade.clearFilters();
    }

    public LazyDataModel<ScratchCard> getCardList() {
        if (cardList == null)
            cardList = new LazyTableModel<>(scratchCardFacade);
        return cardList;
    }

    public void setCardList(LazyDataModel<ScratchCard> cardList) {
        this.cardList = cardList;
    }

    public ScratchCard getSelectedCard() {
        return selectedCard;
    }

    public void setSelectedCard(ScratchCard selectedCard) {
        this.selectedCard = selectedCard;
    }

    public long getSelectedCardId() {
        return selectedCardId;
    }

    public void setSelectedCardId(long selectedCardId) {
        this.selectedCardId = selectedCardId;
    }

    public void reset() {
        log.debug("reset");
        amount = status = serial = batchID = count = null;
        validFrom = validTo = null;
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Resetting successfully completed", "Resetting successfully completed"));
    }

    public void generatePin(long batchID) {

        generated = new HashMap<>();
        pins = new ArrayList<>();
        random = new Random();

        for (int i = 0; i < count; i++) {
            while (true) {
                double nextDouble = 0.0;
                while ((nextDouble == 0.0 || nextDouble == 1.0)) {
                    nextDouble = random.nextDouble();
                }
                long randomValue = LOWER_RANGE + (long) (nextDouble * (UPPER_RANGE - LOWER_RANGE));
                if (generated.get(randomValue) == null) {
                    String pin = String.valueOf(randomValue);
                    pin = pin.substring(0, 5) + batchID + pin.substring(5, pin.length());
                    pins.add(pin);
                    generated.put(randomValue, true);
                    break;
                }
            }
        }
    }

    private boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void createScratchCard() throws Exception {

        int totalCount = 0;
        long batchID;

        if (amount == null || !isNumeric(String.valueOf(amount)) || amount < 1 || amount > 100) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select a valid amount", "Select a valid amount"));
            return;
        }

        if (!((validFrom != null && validTo != null) && validFrom.compareTo(validTo) < 0)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select a valid date interval", "Select a valid date interval"));
            return;
        }
        //TODO more cards create at one time
        if (count == 0 || count > 10000) {
            if (count == 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.valueOf(totalCount) + " scratch cards created", String.valueOf(totalCount) + " scratch cards created"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Count must be less than 10000 at a time", "Count must be less than 10000 at a time"));
            }
            return;
        }

        lock = getLockObject();
        synchronized (lock) {
            if (batchPersistenceFacade.checkCount() == 0) {
                batchID = 10094872;
                BatchID bt = new BatchID();
                bt.setBatchID(batchID);
                batchPersistenceFacade.save(bt);
            } else {
                batchID = batchPersistenceFacade.getBatchID();
            }
            log.debug("batchID: " + batchID);

            generatePin(batchID);
            log.debug(pins.size());

            ScratchCard scratchCard;
            Serial serial;
            Long serialNum = serialFacade.findLast();
            if (serialNum == null) serialNum = 1223654562L;
            scratchCards = new ArrayList();
            for (String pin : pins) {
                scratchCard = new ScratchCard();
                serial = new Serial();
                serial.setId(++serialNum);
                serialFacade.save(serial);
                scratchCard.setSerial(serial);
                scratchCard.setAmount(amount);
                scratchCard.setStatus(0);
                scratchCard.setBatchID(batchID);
                scratchCard.setPin(encryptorAndDecryptor.encrypt(pin));
                scratchCard.setValidFrom(new DateTime(validFrom));
                scratchCard.setValidTo(new DateTime(validTo));
                scratchCards.add(scratchCard);
            }

            totalCount = scratchCardFacade.create(scratchCards);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.valueOf(totalCount) + " scratch cards created", String.valueOf(totalCount) + " scratch cards created"));
            FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/scratchcard/index.xhtml");
            RequestContext.getCurrentInstance().update("scratchCardForm");

        }
    }

    public void search() {

        cardList = null;
        scratchCardFacade.clearFilters();

        if (amount != null) {
            scratchCardFacade.addFilter(ScratchCardPersistenceFacade.Filter.AMOUNT, amount);
        }

        if (status != null) {
            scratchCardFacade.addFilter(ScratchCardPersistenceFacade.Filter.STATUS, status);
        }

        if (batchID != null) {
            scratchCardFacade.addFilter(ScratchCardPersistenceFacade.Filter.BATCH_ID, batchID);
        }

        if (serial != null) {
            scratchCardFacade.addFilter(ScratchCardPersistenceFacade.Filter.SERIAL, serial);
        }

        if (validFrom != null) {
            scratchCardFacade.addFilter(ScratchCardPersistenceFacade.Filter.VALID_FROM, validFrom);
        }

        if (validTo != null) {
            scratchCardFacade.addFilter(ScratchCardPersistenceFacade.Filter.VALID_TO, validTo);
        }

        sqlQuery = scratchCardFacade.getSqlSearchQuery(scratchCardFacade.getFilters());
        cardList = new LazyTableModelDynamic<>(scratchCardFacade, ScratchCard.class, sqlQuery);
        sqlQuery = sqlQuery.replaceFirst("distinct sc", "count(distinct sc.id)");
        long count = scratchCardFacade.countDynamic(sqlQuery);
        cardList.setRowCount((int) count);
        log.debug(String.format("Finished search. Found %d scratch cards", count));

    }

    public ScratchCardSession getSession() {
        if (cardSession == null) {
            ScratchCard card = scratchCardFacade.find(selectedCardId);
            return scratchCardFacade.findSessionByCard(card);
        }
        return cardSession;
    }

    public void settings() throws Exception{
        FacesContext.getCurrentInstance().getExternalContext().redirect(PATH + "/settings.xhtml");
    }

    public void view() throws IOException {

        if (selectedCard.getStatus() == 0) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "The card is not used", "The card is not used"));
            return;
        }

        cardSession = scratchCardFacade.findSessionByCard(selectedCard);
        if(cardSession == null){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Session not found", "Session not found"));
            return;
        }
        FacesContext.getCurrentInstance().getExternalContext().redirect(PATH + "/view.xhtml?selectedCardId=" + selectedCard.getId());
    }

    public String decrypt(String encrypted) {
        return encryptorAndDecryptor.decrypt(encrypted);
    }

    public void payment() {

        Subscription sbn = subscriptionPersistenceFacade.find(getSubscription());
        String res = "";

        if (sbn == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "The subscription is not exists", "The subscription is not exists"));
            return;
        }

        ScratchCardSession scratchCardSession;
        List<ScratchCardSession> sessions = scratchCardFacade.getSessionList(sbn);

        if (sessions != null && sessions.size() > 0) {
            scratchCardSession = sessions.get(0);
            if (scratchCardSession.getIsBlocked()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "The subscription is blocked", "The subscription is blocked"));
                return;
            }
        }

        if (!scratchCardFacade.checkSerial(selectedCard.getSerial())) {
            scratchCardSession = new ScratchCardSession();
            if (sessions != null && sessions.size() + 1 == adminSettingPersistenceFacade.getBlockingSetting().getMaxAttemptCount()) {
                scratchCardSession.setIsBlocked(true);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "3 wrong attempts, The subscription has been blocked for 1 hour", "3 wrong attempts, The subscription has been blocked for 1 hour"));
            } else {
                scratchCardSession.setIsBlocked(false);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Wrong serial", "Wrong serial"));
            }

            sessionPersistenceFacade.create(scratchCardSession, null, null, sbn, null, selectedCard.getSerial().getId(), 1, sbn.getAgreement(), null);
            return;
        }

        ScratchCard scratchCard;
        scratchCard = scratchCardFacade.findScratchCardByPin(selectedCard.getPin());
        if (scratchCard == null) {

            scratchCardSession = new ScratchCardSession();
            if (sessions != null && sessions.size() + 1 >= adminSettingPersistenceFacade.getBlockingSetting().getMaxAttemptCount()) {
                scratchCardSession.setIsBlocked(true);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "3 wrong attempts, The subscription has been blocked for 1 hour", "3 wrong attempts, The subscription has been blocked for 1 hour"));
            } else {
                scratchCardSession.setIsBlocked(false);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Wrong pin", "Wrong pin"));
            }

            sessionPersistenceFacade.create(scratchCardSession, null, null, sbn, selectedCard.getPin(), 0, 1, sbn.getAgreement(), null);
            return;
        }

        //res = scratchCardFacade.testPayment(sbn, 200, selectedCard.getPin());

        amount = scratchCard.getAmount();
        scratchCard = scratchCardFacade.find(scratchCard.getId());
        scratchCard.setStatus(1);

        log.debug("scratchCard: " + scratchCard.getId());

        scratchCardFacade.update(scratchCard);

        log.debug("pin: " + encryptorAndDecryptor.decrypt(scratchCard.getPin()));

        Payment payment = null;
        double amount = scratchCard.getAmount();
        payment = paymentPersistenceFacade.create(sbn, amount, 200);

        log.debug("after payment facade: " + payment);

        if (payment == null) {
            log.debug("cannot make payment external user");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "cannot make payment external user", "cannot make payment external user"));
            return;
        }

        scratchCardSession = new ScratchCardSession();
        scratchCardSession.setIsBlocked(false);
        sessionPersistenceFacade.create(scratchCardSession, payment, scratchCard, sbn, selectedCard.getPin(), 0, 0, sbn.getAgreement(), scratchCard.getSerial().getId());

        amount = payment.getAmount();

        log.debug("make payment successful: " + amount);

        boolean result = false;
        try {
            result = serviceProvider.settlePayment(sbn.getSubscriber().getId(), sbn.getId(), amount, payment.getId());
        } catch (Exception ex) {
            log.debug("cannot settle payment: " + payment);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "cannot settle payment", "cannot settle payment"));
            return;
        }
        if (!result) {
            return;
        } else {
            try {
                campaignJoinerBean.tryAddToCampaign(sbn.getId(), payment.getId(), false);
            } catch (Exception ex) {
                log.error(String.format("settlePayment: error while searching for campaign, subscription id = %d, payment id=%d",
                        sbn.getId(), payment.getId()), ex);
            }

            try {
                campaignRegisterFacade.tryActivateCampaign(sbn.getId(), payment.getId());
            } catch (Exception ex) {
                log.error(String.format("settlePayment: error while searching for campaigns awaiting activation, subscription id = %d, payment id=%d", sbn.getId(), payment.getId()), ex);
            }
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "success", "success"));
    }

    public void preView() {
        log.debug("preView: " + selectedCardId);
    }
}
