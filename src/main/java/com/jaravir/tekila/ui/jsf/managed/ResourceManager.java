/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.service.entity.Resource;
import com.jaravir.tekila.module.service.entity.ResourceBucket;
import com.jaravir.tekila.module.service.persistence.manager.ResourceBucketPersistenceFacade;
import com.jaravir.tekila.module.service.persistence.manager.ResourcePersistenceFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;

/**
 *
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class ResourceManager implements Serializable {
    
  private final static String RESOURCE_PATH = "/pages/resource/"  ;
  private final static String SESSION_RESOURCE_SELECTED = "resource.selected";
  private Resource resource;
  private Resource selected;
  private List<String> resourceBucketIdsList;
  private List<Resource> filteredResources;
  private List<SelectItem> bucketSelectList;
  private List<Resource> resourceList;
  private transient final static Logger log = Logger.getLogger(ResourceManager.class);
  
  @EJB
  private ResourcePersistenceFacade resourceFacade;
  @EJB
  private ResourceBucketPersistenceFacade bucketFacade;

    public Resource getResource() {
        if (resource == null)
            resource = new Resource();
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getSelected() {
        if (selected == null) {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            Long pk = (Long) session.getAttribute(SESSION_RESOURCE_SELECTED);
            //log.debug("Resource PK: " + pk);
            if (pk != null) {
                selected = resourceFacade.find(pk);
                session.removeAttribute(SESSION_RESOURCE_SELECTED);
            }               
        }
        return selected;
    }

    public void setSelected(Resource selected) {
        this.selected = selected;
    }

    public List<String> getResourceBucketIdsList() {
        if (resourceBucketIdsList == null) {
            resourceBucketIdsList = new ArrayList<String>();
        }
        return resourceBucketIdsList;
    }

    public void setResourceBucketIdsList(List<String> resourceBucketIdsList) {
        this.resourceBucketIdsList = resourceBucketIdsList;
    }

    public List<Resource> getFilteredResources() {
        return filteredResources;
    }

    public void setFilteredResources(List<Resource> filteredResources) {
        this.filteredResources = filteredResources;
    }

    public List<SelectItem> getBucketSelectList() {
        List<ResourceBucket> resList = null;
        
        if (bucketSelectList == null) {
            bucketSelectList = new ArrayList<SelectItem>();
            List<String> idList = null;
            
            if (selected != null && (idList = selected.getListOfBucketIds()) != null)
                resList = bucketFacade.findAllNotInList(idList);
            else
                resList = bucketFacade.findAll();
            
            for (ResourceBucket bck : resList) {
                bucketSelectList.add(new SelectItem(bck.getId(), bck.getType() + "/" + bck.getCapacity()));
            }
        }            
        return bucketSelectList;
    }

    public void setBucketSelectList(List<SelectItem> bucketSelectList) {
        this.bucketSelectList = bucketSelectList;
    } 

    public List<Resource> getResourceList() {
        if (resourceList == null)
            loadResources();
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }
       
    public String create() {
        resource.setBucketList(bucketFacade.findAllInList(resourceBucketIdsList));
        //Active 7 days a week
        resource.setActiveOnBusinessDays();
        resource.addActiveDayOfWeek(DateTimeConstants.SUNDAY);
        resource.addActiveDayOfWeek(DateTimeConstants.SATURDAY);
        
       // Logger log = LoggerFactory.getLogger(this.getClass());
        //log.debug(resource.toString());
        resourceFacade.save(resource);
        return RESOURCE_PATH + "index";
    }    
    
    public String show() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.setAttribute(SESSION_RESOURCE_SELECTED, selected.getId());
        //log.debug(selected.toString());
        return RESOURCE_PATH + "view";
    }
    
    public void removeBucket(ResourceBucket bucket) {
        selected.removeBucket(bucket);
    }
    
    public void addBucket() {
        selected.addBuckets(bucketFacade.findAllInList(resourceBucketIdsList));
    }
    
    public String save() {
        resourceFacade.update(selected);
        return RESOURCE_PATH + "index";
    }
    
    private void loadResources() {
       resourceList = resourceFacade.findAll();
    }
}
