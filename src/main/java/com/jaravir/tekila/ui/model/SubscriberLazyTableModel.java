/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.model;

import com.jaravir.tekila.module.subscription.persistence.entity.Subscriber;
import com.jaravir.tekila.module.subscription.persistence.entity.SubscriberDetails;
import com.jaravir.tekila.module.subscription.persistence.management.SubscriberPersistenceFacade;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;


/**
 *
 * @author sajabrayilov
 */
public class SubscriberLazyTableModel extends LazyDataModel<Subscriber>{
   
    private SubscriberPersistenceFacade subFacade;
    private List<Subscriber> subList;
    private final static Logger log = Logger.getLogger(SubscriberLazyTableModel.class);
        
    public SubscriberLazyTableModel (SubscriberPersistenceFacade subFacade) {
        this.subFacade = subFacade;
    }
            
    @Override
    public List<Subscriber> load (int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        subList = subFacade.findAllPaginated(first, pageSize);
        
        Set<Subscriber> filteredList = new LinkedHashSet<Subscriber>();
        
        if (subList == null)
            subList = subFacade.findAllPaginated(first, first % pageSize);
        
        //log.debug("initial subList: " + subList.toString());
        //filtration
        for (Subscriber sub : subList) {
            //log.debug("filter: " + filters + ", cond: " + (filters != null && filters.size() > 0));
            
            if (filters != null && filters.size() > 0) {
                Iterator<String> it = filters.keySet().iterator();
                //log.debug("has more filters? " + it.hasNext());
                boolean match = false;
                
                while (it.hasNext()) {
                                        
                    try {
                        String filteredPropertyName = it.next();
                        String filteredValue = (String) filters.get(filteredPropertyName);
                        String actualValue = getFiltrationResult(filteredPropertyName, sub);
                        
                        //log.debug("after method invoc: " + actualValue);
                        //actualValue = sub.getClass().getMethod("get" + filteredPropertyName, Subscriber.class).invoke(sub).toString();
                        if (filteredValue == null || actualValue.contains(filteredValue))
                            //filteredList.add(sub);
                            match = true;
                        else {
                            match = false;
                            break;
                        }
                    //    log.debug("propertyName="+filteredPropertyName+", value="+actualValue+", filter="+filteredValue+", match="+match);
                    }
                    catch (NoSuchMethodException ex) { 
                         log.error(ex);                        
                         filteredList.add(sub);
                        } 
                    catch (IllegalAccessException ex) {
                       log.error(ex);                      
                       filteredList.add(sub);
                    } catch (IllegalArgumentException ex) {
                       log.error(ex);
                       filteredList.add(sub);
                    } catch (InvocationTargetException ex) {
                       log.error(ex);
                       filteredList.add(sub);
                    }                  
                } //end while on filters
                //log.debug("after all filters: sub" + sub.getDetails().getFirstName() + ", match=" + match);
                if (match)
                    filteredList.add(sub);
                
            } //end if filters != null
            else {
                filteredList.add(sub);
            }
        }
        //set total row count
        this.setRowCount(subList.size());
        
       //log.debug("filtered sublist: " + filteredList);
        return new ArrayList<Subscriber>(filteredList);
    }
    
    @Override
    public Subscriber getRowData(String rowKey) {
       // log.debug("getRowData: rowKey=" + rowKey);
        for (Subscriber sub : subList) {
            if (Long.toString(sub.getId()).equals(rowKey))
                return sub;
        }
        return null;
    }
    
    @Override
    public Object getRowKey(Subscriber sub) {
        return Long.toString(sub.getId());
    }
    
    private String getFiltrationResult(String propertyName, Subscriber sub) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
       // log.debug("propertyName=" + propertyName);
        if (propertyName.indexOf('.') == -1) {
          return sub.getClass().getMethod("get"+WordUtils.capitalize(propertyName)).invoke(sub).toString();
            
        }
        else {
            String[] propertyArray = propertyName.split("\\.");
            SubscriberDetails det = sub.getDetails();
            //log.debug("size: " + propertyArray.length);
           return det.getClass().getMethod("get" + WordUtils.capitalize(propertyArray[1])).invoke(det).toString();            
        }
    }
}
