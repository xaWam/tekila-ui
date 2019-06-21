package com.jaravir.tekila.ui.jsf.managed.util;

import com.jaravir.tekila.module.service.entity.Service;

/**
 * Created by kmaharov on 03.08.2016.
 */
public class ServiceWithBonus {
    private Service service;
    private Long bonus;

    public ServiceWithBonus() {
    }

    public ServiceWithBonus(Service service, Long bonus) {
        this.service = service;
        this.bonus = bonus;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setBonus(Long bonus) {
        this.bonus = bonus;
    }

    public Long getBonus() {
        return bonus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceWithBonus that = (ServiceWithBonus) o;

        if (service != null ? !service.equals(that.service) : that.service != null) return false;
        return bonus != null ? bonus.equals(that.bonus) : that.bonus == null;

    }

    @Override
    public int hashCode() {
        int result = service != null ? service.hashCode() : 0;
        result = 31 * result + (bonus != null ? bonus.hashCode() : 0);
        return result;
    }
}
