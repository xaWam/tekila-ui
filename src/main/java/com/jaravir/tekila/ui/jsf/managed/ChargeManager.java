/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.accounting.entity.Charge;
import com.jaravir.tekila.module.accounting.manager.ChargePersistenceFacade;

import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;

/**
 *
 * @author sajabrayilov
 */
public class ChargeManager implements Serializable {
    private Charge charge;
    private List<Charge> chargeList;
    @EJB
    private ChargePersistenceFacade chargeFacade;
    
    
}
