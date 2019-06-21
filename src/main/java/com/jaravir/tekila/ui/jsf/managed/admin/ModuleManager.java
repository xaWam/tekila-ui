package com.jaravir.tekila.ui.jsf.managed.admin;

import com.jaravir.tekila.base.auth.persistence.Module;
import com.jaravir.tekila.base.auth.persistence.SubModule;
import com.jaravir.tekila.module.auth.ModulePersistenceFacade;
import com.jaravir.tekila.module.auth.SubModulePersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by sajabrayilov on 12/8/2014.
 */
@ManagedBean
@ViewScoped
public class ModuleManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(SubModuleManager.class);
    private final static String PATH = "/pages/admin/modules/";

    @EJB
    private ModulePersistenceFacade modulePersistenceFacade;

    private Module module;
    private Long moduleID;

    //index
    private LazyDataModel<Module> moduleList;
    private Module selected;
    private List<Module> filteredSubModuleList;

    public Module getModule() {
        if (module == null)
            module = moduleID != null ? modulePersistenceFacade.find(moduleID) : new Module();

        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Long getModuleID() {
        return moduleID;
    }

    public void setModuleID(Long moduleID) {
        this.moduleID = moduleID;
    }

    public LazyDataModel<Module> getModuleList() {
        if (moduleList == null)
            moduleList = new LazyTableModel<>(modulePersistenceFacade);

        return moduleList;
    }

    public void setModuleList(LazyDataModel<Module> moduleList) {
        this.moduleList = moduleList;
    }

    public Module getSelected() {
        return selected;
    }

    public void setSelected(Module selected) {
        this.selected = selected;
    }

    public List<Module> getFilteredSubModuleList() {
        return filteredSubModuleList;
    }

    public void setFilteredSubModuleList(List<Module> filteredSubModuleList) {
        this.filteredSubModuleList = filteredSubModuleList;
    }

    public String save() {
        if (moduleID != null && module == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select Submodule", "Please select Submodule"));
            return null;
        }

        if (moduleID != null)
            modulePersistenceFacade.update(module);
        else
            modulePersistenceFacade.save(module);

        return PATH + "index.xhtml";
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select module","Please select module"));
            return null;
        }

        return new StringBuilder(PATH).append("module.xhtml?module=").append(selected.getId())
                .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }
}
