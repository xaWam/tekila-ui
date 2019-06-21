package com.jaravir.tekila.ui.jsf.managed.auth;

import com.jaravir.tekila.base.auth.Privilege;
import com.jaravir.tekila.base.auth.persistence.SubModule;
import com.jaravir.tekila.base.auth.persistence.exception.NoPrivilegeFoundException;
import com.jaravir.tekila.base.auth.persistence.manager.*;
import com.jaravir.tekila.base.auth.persistence.manager.SecurityManager;
import com.jaravir.tekila.module.auth.SubModulePersistenceFacade;
import org.apache.log4j.Logger;

import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;

/**
 * Created by sajabrayilov on 12/20/2014.
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class UISecurityManager  implements Serializable {
    private transient final static Logger log = Logger.getLogger(UISecurityManager.class);
    @EJB private SecurityManager securityManager;

    public boolean hasPermission (String subModuleName, Privilege privilege) {
        try {
            securityManager.checkPermissions(subModuleName, privilege);
            //log.debug("Permission granted");
            return true;
        } catch (NoPrivilegeFoundException ex) {
            //log.debug("Access denied to submodule " + subModuleName, ex);
            return false;
        }
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}
