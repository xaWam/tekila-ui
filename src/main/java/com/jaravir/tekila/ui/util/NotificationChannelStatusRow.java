package com.jaravir.tekila.ui.util;

import com.jaravir.tekila.module.event.notification.channell.NotificationChannell;

/**
 * Created by kmaharov on 12.07.2016.
 */
public class NotificationChannelStatusRow {
    private NotificationChannell channell;
    private boolean isActive;

    public NotificationChannelStatusRow(NotificationChannell channell, boolean isActive) {
        this.channell = channell;
        this.isActive = isActive;
    }

    public NotificationChannell getChannell() {
        return channell;
    }

    public void setChannell(NotificationChannell channell) {
        this.channell = channell;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
