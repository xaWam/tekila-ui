package com.jaravir.tekila.ui.jsf.managed.admin;

import com.jaravir.tekila.base.auth.UserStatus;
import com.jaravir.tekila.base.auth.persistence.Group;
import com.jaravir.tekila.base.auth.persistence.User;
import com.jaravir.tekila.base.auth.persistence.manager.UserPersistenceFacade;
import com.jaravir.tekila.base.filter.MatchingOperation;
import com.jaravir.tekila.module.auth.GroupPersistenceFacade;
import com.jaravir.tekila.module.auth.security.PasswordGenerator;
import com.jaravir.tekila.module.system.SystemEvent;
import com.jaravir.tekila.module.system.log.SystemLogger;
import com.jaravir.tekila.ui.jsf.managed.LoginManager;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.model.LazyDataModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajabrayilov on 12/13/2014.
 */
@ManagedBean
@ViewScoped
public class UserManager implements Serializable {

    private transient final static Logger log = Logger.getLogger(UserManager.class);
    private final static String PATH = "/pages/admin/users/";
    @EJB
    private UserPersistenceFacade userPersistenceFacade;
    @EJB
    private GroupPersistenceFacade groupPersistenceFacade;
    @EJB
    private PasswordGenerator passwordGenerator;
    @EJB
    private SystemLogger systemLogger;

    private User user;
    private LazyDataModel<Group> groupList;
    private Long userID;
    private User selected;
    private LazyDataModel<User> userList;
    private Boolean isRegeneratePassword = false;

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    //SELECTED ITEMS
    private List<User> selectedUsers;

    //In order send  bulk email
    private String emailText;
    private String sendingOption;
    private String emailSubject;


    //search
    private String email;
    private String username;
    private String firstName;
    private String surname;
    private List<SelectItem> groupSelectList;
    private Long groupID;

    @ManagedProperty(value = "#{loginManager}")
    private LoginManager loginManager;


    public User getUser() {
        if (user == null) {
            log.info("getuser -> user is null");
            user = userID != null ? userPersistenceFacade.findForceRefresh(userID) : new User();
        }
        log.info("getuser -> "/*+user*/);
        return user;
    }

    public void sendMailNotification() {
        log.info("Subject : " + emailSubject);
        log.info("Message : " + emailText);
        log.info("TO : " + selectedUsers);
        log.info("options : " + sendingOption);
        log.info("WHO : " + loginManager.getUsername());
        try {
            if (sendingOption != null && sendingOption.equalsIgnoreCase("selected"))
                userPersistenceFacade.sendBulkEmailNotification(selectedUsers, emailText, emailSubject);
            else if (sendingOption != null && sendingOption.equalsIgnoreCase("all")) {
                userPersistenceFacade.sendBulkEmailNotification(userPersistenceFacade.getUsers(), emailText, emailSubject);
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Your request successfully accepted , all notifications are being processing", null));
            log.debug("EMAIL NOTIFICATION CALLED on " + selectedUsers);
        } catch (Exception e) {
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Something went wrong", null));
        }
    }

    public void block() {
        try {
            for (User u : selectedUsers) {
                userPersistenceFacade.forceBlock(u.getUserName());
                systemLogger.success(SystemEvent.USER_UNBLOCKED, null, "User :" + u.getUserName() + "is blocked manually by " + loginManager.getUsername());
                log.info(u.getUserName() + " is blocked by" + loginManager.getUsername());
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void unblock() {
        try {
            for (User u : selectedUsers) {
                userPersistenceFacade.forceUnblock(u.getUserName());
                systemLogger.success(SystemEvent.USER_UNBLOCKED, null, "User :" + u.getUserName() + "is unblocked manually by " + loginManager.getUsername());
                log.info(u.getUserName() + " is unblocked by " + loginManager.getUsername());
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    public void resetPassword() {


    }

    public void resetPasswordInSilentMode() {

    }

    public void setUser(User user) {
        this.user = user;
        log.info("setUser -> "/*+this.user*/);
    }

    public LazyDataModel<Group> getGroupList() {
        if (groupList == null) {
            groupList = new LazyTableModel<>(groupPersistenceFacade);
        }
        return groupList;
    }

    public void setGroupList(LazyDataModel<Group> groupList) {
        this.groupList = groupList;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public User getSelected() {
        return selected;
    }

    public void setSelected(User selected) {
        this.selected = selected;
    }

    public LazyDataModel<User> getUserList() {
        if (userList == null) {
            userList = new LazyTableModel<>(userPersistenceFacade);
        }
        return userList;
    }

    public void setUserList(LazyDataModel<User> userList) {
        this.userList = userList;
    }

    public Boolean getIsRegeneratePassword() {
        return isRegeneratePassword;
    }

    public void setIsRegeneratePassword(Boolean isRegeneratePassword) {
        this.isRegeneratePassword = isRegeneratePassword;
    }

    public String save() {
        log.info("This User: " + this.user.toString());
        log.info("User surname : " + user.getSurname());
        String message = null;

        if (getUser().getGroup() == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Group is required", "Group is required"));
            return null;
        }

        char[] rawPass = null;

        if (userID == null) { // adding user
            rawPass = (newPassword != null && !newPassword.isEmpty()) ? newPassword.toCharArray() : passwordGenerator.generatePassword();
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedDate(DateTime.now());
        } else if (userID != null) { // editing user
            if (isRegeneratePassword)
                rawPass = passwordGenerator.generatePassword();
            else if (newPassword != null && !newPassword.isEmpty()) {
                rawPass = newPassword.toCharArray();
            }
        }

        // VALIDATING PASSWORD
        try {
            if(rawPass != null){  //if password is new
                PasswordValidator.validate(String.valueOf(rawPass));
                user.setPassword(passwordGenerator.encodePassword(rawPass));
                user.setLastPasswordChanged(DateTime.now());
            }
        } catch (Exception e) {
            log.error("Error occurs while validate password", e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
            return null;
        }

        log.info("User desc : " + user.getDsc());

        try {

            if (userID != null) {

                if (isRegeneratePassword || !newPassword.isEmpty()) {
                    userPersistenceFacade.update(user, String.valueOf(rawPass));
                } else {
                    userPersistenceFacade.update(user);
                }

                message = "User " + user.getUserName() + " updated successfully";
            } else {
                userPersistenceFacade.save(user, String.valueOf(rawPass), false);
                message = "User " + user.getUserName() + " created successfully? Password is: " + String.valueOf(rawPass);

            }

            if (rawPass != null) {
                message = "Success! Password is: " + String.valueOf(rawPass);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
                return null;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
                return null;
                //return String.format("%suser.xhtml?user=%d&amp;faces-redirect=true&amp;includeViewParams=true", PATH, user.getId());
            }
        } catch (Exception ex) {
            log.error("Error occurs while save/edit user", ex);
            message = userID != null ? "Cannot edit user " + getUser().getUserName() : "Cannot create user " + getUser().getUserName();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
            return null;
        }
    }

    public String show() {
        if (selectedUsers == null || selectedUsers.size() != 1) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select one User", "Please select a User"));
            return null;
        }

        return new StringBuilder(PATH).append("user.xhtml?user=").append(selectedUsers.get(0).getId())
                .append("&amp;includeViewParams=true&amp;faces-redirect=true").toString();
    }

    public void reset() {
        userPersistenceFacade.clearFilters();

        username = null;
        firstName = null;
        surname = null;
        groupID = null;
        email = null;
        userList = null;
    }

    public void changePassword() {
        if (oldPassword == null || oldPassword.isEmpty()
                || newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please fill all fields", "Please fill all fields"));
            return;
        }

        String username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();
        User user = userPersistenceFacade.check(username, passwordGenerator.encodePassword(oldPassword.toCharArray()));

        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Old password is incorrect", "Old password is incorrect"));
            return;
        }

        if (newPassword.length() < 8) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password should be minimum 8 charecter long", "Password should be minimum 8 charecter long"));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match", "Passwords do not match"));
            return;
        }

        if (!passwordGenerator.validate(newPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password should be contain minimum one special character and a digit", "Password should be contain minimum one special character and a digit"));
            return;
        }


        if (user.getUsedPasswords() != null
                && user.getUsedPasswords().contains(passwordGenerator.encodePassword(newPassword.toCharArray()))) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "You cannot use already used passwords in the past", ""));
            return;
        }

        // VALIDATING PASSWORD
        try {
            PasswordValidator.validate(newPassword);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
            return;
        }

        try {

            String encodedPassword = passwordGenerator.encodePassword(newPassword.toCharArray());
            user.setPassword(encodedPassword);
            user.setUsedPasswords(user.getUsedPasswords() + "," + encodedPassword);
            user.setLastPasswordChanged(DateTime.now());
            userPersistenceFacade.update(user, newPassword);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Password successfully changed", "Password successfully changed"));
            systemLogger.success(SystemEvent.PASSWORD_CHANGED, null, loginManager.getUsername() + " changed his password successfully");
            loginManager.logout();
        } catch (IOException ex) {
            log.error("Cannot redirect", ex);
            FacesContext.getCurrentInstance().addMessage("loginForm", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match", "Passwords do not match"));
        } catch (Exception ex) {
            log.error("Cannot change password", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot change password", "Cannot change password"));
        }

    }

    public LoginManager getLoginManager() {
        return loginManager;
    }

    public void setLoginManager(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public List<SelectItem> getGroupSelectList() {
        if (groupSelectList == null) {
            groupSelectList = new ArrayList<>();

            List<Group> groups = groupPersistenceFacade.findAll();

            for (Group gr : groups) {
                groupSelectList.add(new SelectItem(gr.getId(), gr.getGroupName()));
            }
        }
        return groupSelectList;
    }

    public void setGroupSelectList(List<SelectItem> groupSelectList) {
        this.groupSelectList = groupSelectList;
    }

    public Long getGroupID() {
        return groupID;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    public void search() {
        userPersistenceFacade.clearFilters();

        if (username != null && !username.isEmpty()) {
            UserPersistenceFacade.Filter userNameFilter = UserPersistenceFacade.Filter.USERNAME;
            userNameFilter.setOperation(MatchingOperation.LIKE);
            userPersistenceFacade.addFilter(userNameFilter, username);
        }

        if (firstName != null && !firstName.isEmpty()) {
            UserPersistenceFacade.Filter firstnameFilter = UserPersistenceFacade.Filter.FIRSTNAME;
            firstnameFilter.setOperation(MatchingOperation.LIKE);
            userPersistenceFacade.addFilter(firstnameFilter, firstName);
        }

        if (surname != null && !surname.isEmpty()) {
            UserPersistenceFacade.Filter surnameFilter = UserPersistenceFacade.Filter.SURNAME;
            surnameFilter.setOperation(MatchingOperation.LIKE);
            userPersistenceFacade.addFilter(surnameFilter, surname);
        }

        if (groupID != null) {
            userPersistenceFacade.addFilter(UserPersistenceFacade.Filter.GROUP_ID, groupID);
        }

        if (email != null && !email.isEmpty()) {
            UserPersistenceFacade.Filter emailFilter = UserPersistenceFacade.Filter.EMAIL;
            emailFilter.setOperation(MatchingOperation.LIKE);
            userPersistenceFacade.addFilter(emailFilter, email);
        }

        userList = new LazyTableModel<>(userPersistenceFacade);
    }


    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<User> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
    }

    public String getSendingOption() {
        return sendingOption;
    }

    public void setSendingOption(String sendingOption) {
        this.sendingOption = sendingOption;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
}
