package com.jaravir.tekila.ui.jsf.managed.admin;

import com.jaravir.tekila.module.admin.AdminSettingPersistenceFacade;
import com.jaravir.tekila.module.store.ScratchCardSettingFacade;
import com.jaravir.tekila.module.store.ats.AtsPersistenceFacade;
import com.jaravir.tekila.module.subscription.persistence.entity.Ats;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;

import com.jaravir.tekila.module.admin.setting.AdminSetting;
import com.jaravir.tekila.module.store.scratchcard.Settings.ScratchCardSettingType;
import com.jaravir.tekila.module.store.scratchcard.persistence.entity.ScratchCardBlockingSetting;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shnovruzov on 5/24/2016.
 * =======
 * import java.io.Serializable;
 * <p>
 * /**
 * Created by shnovruzov on 5/5/2016.
 * >>>>>>> origin/stretchCard
 */
@ManagedBean
@ViewScoped
public class AdminSettingManager implements Serializable {

    private final static Logger log = Logger.getLogger(AdminSettingManager.class);

    //region
    @EJB
    private AtsPersistenceFacade atsFacade;
    private List<SelectItem> atsSelectList;
    private List<String> selectedAtsList;
    private List<Integer> dayOfMonth;
    private Integer selectedDay;
    private LazyDataModel<Ats> atsList;


    private Integer maxAttempCount;
    private Integer blockingHours;

    @EJB
    AdminSettingPersistenceFacade adminSettingPersistenceFacade;
    @EJB
    ScratchCardSettingFacade scratchCardSettingFacade;

    public Integer getMaxAttempCount() {
        return maxAttempCount;
    }

    public void setMaxAttempCount(Integer maxAttempCount) {
        this.maxAttempCount = maxAttempCount;
    }

    public Integer getBlockingHours() {
        return blockingHours;
    }

    public void setBlockingHours(Integer blockingHours) {
        this.blockingHours = blockingHours;
    }


    public LazyDataModel<Ats> getAtsList() {
        return atsList;
    }

    public void setAtsList(LazyDataModel<Ats> atsGroupList) {
        this.atsList = atsGroupList;
    }

    public List<String> getSelectedAtsList() {
        return selectedAtsList;
    }

    public void setSelectedAtsList(List<String> selectedAtsList) {
        this.selectedAtsList = selectedAtsList;
    }

    public List<SelectItem> getAtsSelectList() {
        return atsSelectList;
    }

    public void setAtsSelectList(List<SelectItem> atsSelectList) {
        this.atsSelectList = atsSelectList;
    }

    public List<Integer> getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(List<Integer> dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public Integer getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(Integer selectedDay) {
        this.selectedDay = selectedDay;
    }

    public void search() {
        atsList = new LazyTableModel<>(atsFacade);
        log.debug("ats count: " + atsFacade.count());
    }

    public void save() {

        if (selectedAtsList == null || selectedAtsList.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select any ats", "Select any ats"));
            return;
        }
        if (selectedDay == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Select any day", "Select any day"));
            return;
        }

        search();
    }


    public void saveScSettings() throws IOException {

        if (maxAttempCount == null || blockingHours == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill in the fields", "Fill in the fields"));
        }

        ScratchCardBlockingSetting blockingSetting = adminSettingPersistenceFacade.getBlockingSetting();
        if (blockingSetting == null) {
            blockingSetting = new ScratchCardBlockingSetting();
            blockingSetting.setBlockingHours(blockingHours);
            blockingSetting.setMaxAttemptCount(maxAttempCount);
            scratchCardSettingFacade.save(blockingSetting);
        } else {
            blockingSetting.setBlockingHours(blockingHours);
            blockingSetting.setMaxAttemptCount(maxAttempCount);
            scratchCardSettingFacade.update(blockingSetting);
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Updated successfully", "Updated successfully"));
        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/scratchcard/index.xhtml");

    }

    public void reset() {
        maxAttempCount = null;
        blockingHours = null;
    }
}

