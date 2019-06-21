package com.jaravir.tekila.ui.jsf.managed;


import com.jaravir.tekila.module.event.notification.channell.NotificationChannelStatus;
import com.jaravir.tekila.module.event.notification.channell.NotificationChannell;
import com.jaravir.tekila.module.notification.NotificationChannelPersistenceFacade;
import com.jaravir.tekila.ui.util.NotificationChannelStatusRow;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kmaharov on 12.07.2016.
 */
@ManagedBean
@ViewScoped
public class NotificationChannelManager {
    @EJB
    NotificationChannelPersistenceFacade notifChannelFacade;

    List<NotificationChannelStatusRow> statusList;

    public NotificationChannelManager() {

    }

    public List<NotificationChannelStatusRow> getStatusList() {
        if (statusList == null) {
            List<NotificationChannelStatus> channelStatuses = notifChannelFacade.findAll();

            statusList = new ArrayList<>();
            for (NotificationChannell channel : NotificationChannell.values()) {
                boolean active = false;
                if (channelStatuses != null) {
                    for (NotificationChannelStatus status : channelStatuses) {
                        if (status.getChannell().equals(channel)) {
                            active = status.isActive();
                            break;
                        }
                    }
                }
                statusList.add(new NotificationChannelStatusRow(channel, active));
            }
        }
        return statusList;
    }

    public void setStatusList(List<NotificationChannelStatusRow> statusList) {
        this.statusList = statusList;
    }

    public void save() {
        for (NotificationChannelStatusRow row : statusList) {
            NotificationChannell channell = row.getChannell();
            NotificationChannelStatus channelStatus = notifChannelFacade.getChannelStatus(channell);
            if (channelStatus == null) {
                NotificationChannelStatus status = new NotificationChannelStatus();
                status.setChannell(channell);
                status.setActive(row.isActive());
                notifChannelFacade.save(status);
            } else if (channelStatus.isActive() != row.isActive()) {
                channelStatus.setActive(row.isActive());
                notifChannelFacade.update(channelStatus);
            }
        }
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}