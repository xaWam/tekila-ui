package com.jaravir.tekila.ui.jsf.managed.admin;

import com.jaravir.tekila.base.auth.Privilege;
import com.jaravir.tekila.base.auth.persistence.Module;
import com.jaravir.tekila.base.auth.persistence.SubModule;
import com.jaravir.tekila.module.auth.ModulePersistenceFacade;
import com.jaravir.tekila.module.auth.SubModulePersistenceFacade;
import com.jaravir.tekila.ui.model.FilterableLazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import spring.security.PathPrivilegeStore;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;

/**
 * Created by sajabrayilov on 12/8/2014.
 */
@ManagedBean
@ViewScoped
public class SubModuleManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(SubModuleManager.class);
    private final static String PATH = "/pages/admin/submodules/";

    @EJB private SubModulePersistenceFacade subModulePersistenceFacade;
    @EJB private ModulePersistenceFacade modulePersistenceFacade;

    private SubModule subModule;
    private Long subModuleID;
    private List<Module> moduleList;
    private Module module;
    private String path;
    private Privilege privilege;
    private List<SelectItem> privilegeSelectList;
    private Map.Entry<String, Privilege> selectedPathEntry;

    //index
    private LazyDataModel<SubModule> subModuleList;
    private SubModule selected;
    private List<SubModule> filteredSubModuleList;


    public SubModule getSubModule() {
        if (subModule == null)
            subModule = subModuleID != null ? subModulePersistenceFacade.find(subModuleID) : new SubModule();

        return subModule;
    }

    public void setSubModule(SubModule subModule) {
        this.subModule = subModule;
    }

    public Long getSubModuleID() {
        return subModuleID;
    }

    public void setSubModuleID(Long subModuleID) {
        this.subModuleID = subModuleID;
    }

    public LazyDataModel<SubModule> getSubModuleList() {
        if (subModuleList == null) {
            subModuleList = new FilterableLazyTableModel<>(
                    subModulePersistenceFacade,
                    Arrays.asList(SubModulePersistenceFacade.Filter.values()));
        }
        return subModuleList;
    }

    public void setSubModuleList(LazyDataModel<SubModule> subModuleList) {
        this.subModuleList = subModuleList;
    }

    public SubModule getSelected() {
        return selected;
    }

    public void setSelected(SubModule selected) {
        this.selected = selected;
    }

    public List<SubModule> getFilteredSubModuleList() {
        return filteredSubModuleList;
    }

    public void setFilteredSubModuleList(List<SubModule> filteredSubModuleList) {
        this.filteredSubModuleList = filteredSubModuleList;
    }

    public List<Module> getModuleList() {
        if (moduleList == null)
            moduleList = modulePersistenceFacade.findAll();

        return moduleList;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public List<SelectItem> getPrivilegeSelectList() {
        if (privilegeSelectList == null) {
            privilegeSelectList = new ArrayList<>();

            for (Privilege priv : Privilege.values())
                privilegeSelectList.add(new SelectItem(priv));
        }

        return privilegeSelectList;
    }

    public void setPrivilegeSelectList(List<SelectItem> privilegeSelectList) {
        this.privilegeSelectList = privilegeSelectList;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    public Map.Entry<String, Privilege> getSelectedPathEntry() {
        return selectedPathEntry;
    }

    public void setSelectedPathEntry(Map.Entry<String, Privilege> pathEntry) {
        this.selectedPathEntry = pathEntry;
    }

    public void addPrivilege () {
        if (subModule == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Submodule unknown", "Submodule unknown"));
            return;
        }

        if (path == null || path.isEmpty() || privilege == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all fields", "Please fill all fields"));
            return;
        }

        subModule.getPathMap().put(path, privilege);
        if (selectedPathEntry != null)
            selectedPathEntry = null;

        resetPrivilege();
        PathPrivilegeStore.getInstance().populate(subModulePersistenceFacade.findAll());
    }

    public void removePrivilege () {
        if (selectedPathEntry == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a path", "Please select a path"));
            return;
        }

        subModule.getPathMap().remove(selectedPathEntry.getKey());
        resetPrivilege();
    }

    public void resetPrivilege () {
        path = null;
        privilege = null;
        selectedPathEntry = null;
    }

    public void onRowSelect (SelectEvent event) {
        if (selectedPathEntry == null)
            return;

        path = selectedPathEntry.getKey();
        privilege = selectedPathEntry.getValue();
    }

    public List<Map.Entry<String, Privilege>> getPathMap() {
        SubModule sub = null;
        if (getSubModule() != null)
            sub = getSubModule();
        else
            return null;

        return new ArrayList<>(sub.getPathMap().entrySet());
    }

    public String save() {
        if (subModuleID != null && subModule == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select Submodule", "Please select Submodule"));
            return null;
        }

        if (subModuleID == null && module == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select Module", "Please select Module"));
            return null;
        }

        if (module != null)
            subModule.setModule(module);

        if (subModuleID != null)
            subModulePersistenceFacade.update(subModule);
        else
            subModulePersistenceFacade.save(subModule);

        PathPrivilegeStore.getInstance().populate(subModulePersistenceFacade.findAll());
        return PATH + "index.xhtml";
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select module","Please select module"));
            return null;
        }

        return new StringBuilder(PATH).append("submodule.xhtml?submodule=").append(selected.getId())
                .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }
}
