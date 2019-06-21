package com.jaravir.tekila.ui.jsf.managed.admin;

import com.jaravir.tekila.base.auth.PermissionRow;
import com.jaravir.tekila.base.auth.Privilege;
import com.jaravir.tekila.base.auth.persistence.*;
import com.jaravir.tekila.module.auth.GroupPersistenceFacade;
import com.jaravir.tekila.module.auth.ModulePersistenceFacade;
import com.jaravir.tekila.module.auth.RolePersistenceFacade;
import com.jaravir.tekila.module.service.entity.ServiceProvider;
import com.jaravir.tekila.module.service.persistence.manager.ServiceProviderPersistenceFacade;
import com.jaravir.tekila.ui.model.FilterableLazyTableModel;
import com.jaravir.tekila.ui.model.LazyTableModel;
import com.jaravir.tekila.ui.util.PermissionNode;
import org.apache.log4j.Logger;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sajabrayilov on 12/9/2014.
 */
@ManagedBean
@ViewScoped
public class RoleManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(RoleManager.class);
    private final static String PATH = "/pages/admin/roles/";

    @EJB private RolePersistenceFacade rolePersistenceFacade;
    @EJB private ServiceProviderPersistenceFacade providerPersistenceFacade;
    @EJB private ModulePersistenceFacade modulePersistenceFacade;
    @EJB private GroupPersistenceFacade groupPersistenceFacade;

    private Role role;
    private Long roleID;
    private LazyDataModel<Role> roleList;
    private Role selected;
    private List<Role> filteredRoleList;
    private LazyDataModel<ServiceProvider> providerList;
    private ServiceProvider selectedProvider;
    private TreeNode privTree;
    private LazyDataModel<Module> moduleList;
    private Module selectedModule;
    private List<SubModule> selectedSubModules;
    private List<PermissionRow> permissionRowList;
    private PermissionRow selectedPermissionRow;
    private List<Privilege> selectedPrivileges;
    private List<Module> eagerModuleList;
    private List<PermissionRow> selectedPermissionRows;

    public Role getRole() {
        if (role == null)
            role = roleID != null ? rolePersistenceFacade.find(roleID) : new Role();

        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getRoleID() {
        return roleID;
    }

    public void setRoleID(Long roleID) {
        this.roleID = roleID;
    }

    public LazyDataModel<Role> getRoleList() {
        if (roleList == null) {
            roleList = new FilterableLazyTableModel<>(
                    rolePersistenceFacade,
                    Arrays.asList(RolePersistenceFacade.Filter.values()));
        }
        return roleList;
    }

    public void setRoleList(LazyDataModel<Role> roleList) {
        this.roleList = roleList;
    }

    public Role getSelected() {
        return selected;
    }

    public void setSelected(Role selected) {
        this.selected = selected;
    }

    public List<Role> getFilteredRoleList() {
        return filteredRoleList;
    }

    public void setFilteredRoleList(List<Role> filteredRoleList) {
        this.filteredRoleList = filteredRoleList;
    }

    public ServiceProvider getSelectedProvider() {
        if (roleID != null && selectedProvider == null)
            selectedProvider = getRole().getProvider();
        return selectedProvider;
    }

    public void setSelectedProvider(ServiceProvider selectedProvider) {
        this.selectedProvider = selectedProvider;
    }

    public LazyDataModel<ServiceProvider> getProviderList() {
        if (providerList == null)
            providerList = new LazyTableModel<>(providerPersistenceFacade);

        return providerList;
    }

    public void setProviderList(LazyDataModel<ServiceProvider> providerList) {
        this.providerList = providerList;
    }

    public LazyDataModel<Module> getModuleList() {
        if (moduleList == null)
            moduleList = new LazyTableModel<>(modulePersistenceFacade);

        return moduleList;
    }

    public void setModuleList(LazyDataModel<Module> moduleList) {
        this.moduleList = moduleList;
    }

    public Module getSelectedModule() {
        if (roleID != null && selectedModule == null)
            selectedModule = getRole().getModule();
        return selectedModule;
    }

    public void setSelectedModule(Module selectedModule) {
        this.selectedModule = selectedModule;
    }

    public List<SubModule> getSelectedSubModules() {
        return selectedSubModules;
    }

    public void setSelectedSubModules(List<SubModule> selectedSubModules) {
        this.selectedSubModules = selectedSubModules;
    }

    public List<PermissionRow> getPermissionRowList() {
        if (roleID != null && permissionRowList == null) {
            Module mod = getSelectedModule();
            if (mod != null) {
                populateSubModulesTable();
            }
        }
        return permissionRowList;
    }

    public void setPermissionRowList(List<PermissionRow> permissionRowList) {
        this.permissionRowList = permissionRowList;
    }

    public PermissionRow getSelectedPermissionRow() {
        return selectedPermissionRow;
    }

    public void setSelectedPermissionRow(PermissionRow selectedPermissionRow) {
        this.selectedPermissionRow = selectedPermissionRow;
    }

    public List<Privilege> getSelectedPrivileges() {
        return selectedPrivileges;
    }

    public void setSelectedPrivileges(List<Privilege> selectedPrivileges) {
        this.selectedPrivileges = selectedPrivileges;
    }

    public List<Module> getEagerModuleList() {
        if (eagerModuleList == null)
            eagerModuleList = modulePersistenceFacade.findAll();

        return eagerModuleList;
    }

    public void setEagerModuleList(List<Module> eagerModuleList) {
        this.eagerModuleList = eagerModuleList;
    }

    public List<PermissionRow> getSelectedPermissionRows() {
        return selectedPermissionRows;
    }

    public void setSelectedPermissionRows(List<PermissionRow> selectedPermissionRows) {
        this.selectedPermissionRows = selectedPermissionRows;
    }

    public TreeNode getPrivTree() {
        if (privTree == null)
            createPermTree();
        return privTree;
    }

    private void createPermTree() {
        privTree = new DefaultTreeNode(new PermissionNode("name 0", "firstID", new SelectBooleanCheckbox()), null);
        TreeNode first = new DefaultTreeNode(new PermissionNode("name 1", "secondID", new SelectBooleanCheckbox()), privTree);
    }


    public void onRowSelect (SelectEvent event) {
        populateSubModulesTable();
    }

    private void populateSubModulesTable() {
        permissionRowList = new ArrayList<>();
        PermissionRow permissionRow = null;

        for (SubModule subModule : selectedModule.getSubModules()) {
            permissionRow = new PermissionRow();
            permissionRow.setSubModule(subModule);
            permissionRow.addPrivilege(Privilege.READ);
            permissionRow.addPrivilege(Privilege.INSERT);
            permissionRow.addPrivilege(Privilege.UPDATE);
            permissionRow.addPrivilege(Privilege.DELETE);
            log.debug("Susbmodule iterated: " + subModule.getName());
            if (roleID != null && getRole().getPermissions() != null && !getRole().getPermissions().isEmpty()) {
                for (Permission perm : getRole().getPermissions()) {
                    log.debug("Permission tested: " + perm);
                    log.debug("Permission row tested: " + permissionRow);
                    log.debug("Is Permisssion's submodule equal to row's one? " + perm.getSubModule().equals(subModule));

                    if (!perm.getSubModule().equals(subModule))
                        continue;

                    if (perm.getPrivilege() != null && !perm.getPrivilege().isEmpty()) {
                        log.debug("Privilege list: " + perm.getPrivilege());
                        permissionRow.setSelectedPrivilegeList(new ArrayList<String>());
                        for (Privilege priv : perm.getPrivilege()) {
                            permissionRow.getSelectedPrivilegeList().add(priv.getCode());
                        }
                    }
                    break;
                }
            }

            log.debug("Resulting Permission row: " + permissionRow);
            permissionRowList.add(permissionRow);
        }
    }

    public String save () {
        if (selectedProvider == null || selectedModule == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select group, module and provider","Please select group, module and provider"));
            return null;
        }

        int count = 0;

        for (PermissionRow row : permissionRowList) {
            count += row.getSelectedPrivilegeList().size();
        }

        if (count == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please set a privilege","Please set a privilege"));
            return null;
        }
       /* if (roleID != null )
            rolePersistenceFacade.update(role);
        else
            rolePersistenceFacade.save(role);
        */
        try {
            if (roleID != null)
                rolePersistenceFacade.edit(role, selectedModule, selectedProvider, permissionRowList);
            else
                rolePersistenceFacade.create(role, selectedModule, selectedProvider, permissionRowList);
        }
        catch (Exception ex) {
            String message = roleID != null ? "Cannot edit the role " + role.getName() : "Cannot create the role";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
            return null;
        }
        log.debug("PermissionRows: " + permissionRowList);
        return PATH + "index.xhtml";
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a role", "Please select a role"));
            return null;
        }

        return new StringBuilder(PATH).append("role.xhtml?role=").append(selected.getId())
                .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }
}
