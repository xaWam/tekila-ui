/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 *
 * @author sajabrayilov
 */

@ManagedBean(eager=true)
//@ApplicationScoped
public class Interceptor  implements Serializable {
    
    /**
     * Creates a new instance of Interceptor
     */
    //@PostConstruct
    public void init() {
  /*      Logger log = Logger.getLogger(Interceptor.class);
        log.debug("loaded");
        log.info("loaded");
        log.error("loaded");
        log.fatal("LOGGER LOADED!!!");
          */
    }
    
}
