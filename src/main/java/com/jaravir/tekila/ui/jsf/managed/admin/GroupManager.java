package com.jaravir.tekila.ui.jsf.managed.admin;

import com.jaravir.tekila.base.auth.persistence.Group;
import com.jaravir.tekila.base.auth.persistence.Role;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.module.auth.GroupPersistenceFacade;
import com.jaravir.tekila.module.auth.RolePersistenceFacade;
import com.jaravir.tekila.ui.model.FilterableLazyTableModel;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by sajabrayilov on 12/9/2014.
 */
@ManagedBean
@ViewScoped
public class GroupManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(GroupManager.class);
    private final static String PATH = "/pages/admin/groups/";

    @EJB private GroupPersistenceFacade groupPersistenceFacade;
    @EJB private UserPersistenceFacade userPersistenceFacade;
    @EJB private RolePersistenceFacade rolePersistenceFacade;

    private Group group;
    private LazyDataModel<Role> roleList;
    private Role selectedRole;
    private Long groupID;
    private LazyDataModel<Group> groupList;
    private Group selected;

    public Group getGroup() {
        if (group == null)
            group = groupID != null ? groupPersistenceFacade.find(groupID) : new Group();
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public LazyDataModel<Role> getRoleList() {
        if (roleList == null)
            roleList = new LazyTableModel<>(rolePersistenceFacade);
        return roleList;
    }

    public void setRoleList(LazyDataModel<Role> roleList) {
        this.roleList = roleList;
    }

    public Role getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Role selectedRole) {
        this.selectedRole = selectedRole;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public LazyDataModel<Group> getGroupList() {
        if (groupList == null) {
            groupList = new FilterableLazyTableModel<>(
                    groupPersistenceFacade,
                    Arrays.asList(GroupPersistenceFacade.Filter.values()));
        }

        return groupList;
    }

    public void setGroupList(LazyDataModel<Group> groupList) {
        this.groupList = groupList;
    }

    public Group getSelected() {
        return selected;
    }

    public void setSelected(Group selected) {
        this.selected = selected;
    }

    public String save() {
        try {
            if (groupID != null)
                groupPersistenceFacade.update(group);
            else
                groupPersistenceFacade.save(group);
        }
        catch (Exception ex) {
            String message = groupID != null ? "Cannot edit Group " + getGroup().getGroupName()
                    : "Cannot save group " + getGroup().getGroupName();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
            return null;
        }
        return PATH + "index.xhtml";
    }

    public String show() {
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a group", "Please select a group"));
            return null;
        }

        return new StringBuilder(PATH).append("group.xhtml?group=").append(selected.getId())
            .append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }
}
