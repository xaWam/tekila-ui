/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jaravir.tekila.ui.jsf.managed;

import com.jaravir.tekila.module.service.ResourceBucketType;
import com.jaravir.tekila.module.service.entity.ResourceBucket;
import com.jaravir.tekila.module.service.persistence.manager.ResourceBucketPersistenceFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

/**
 *
 * @author sajabrayilov
 */
@ManagedBean
@ViewScoped
public class ResourceBucketManager  implements Serializable {
    private ResourceBucket bucket;
    private ResourceBucket selected;
    private List<SelectItem> typeList;
    private static final String RESOURCE_BUCKET_PATH = "/pages/bucket/";
    private List<ResourceBucket> bucketList;
    private List<ResourceBucket> filteredBucketList;
    private final static String SESSION_SELECTED_BUCKET = "bucket.selected";
   @EJB
   private ResourceBucketPersistenceFacade bucketFacade;

    public ResourceBucket getSelected() {
        if (selected == null) {
             HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            Long pk = (Long) session.getAttribute(SESSION_SELECTED_BUCKET);
            if (pk != null) {
                selected = bucketFacade.find(pk);
                session.removeAttribute(SESSION_SELECTED_BUCKET);
            }            
        }
        return selected;
    }

    public void setSelected(ResourceBucket selected) {
        this.selected = selected;
    }   
   
    public List<ResourceBucket> getBucketList() {
        if (bucketList == null) {
            loadBucketList();
        }
        return bucketList;
    }

    public void setBucketList(List<ResourceBucket> bucketList) {
        this.bucketList = bucketList;
    }

    public List<ResourceBucket> getFilteredBucketList() {
        return filteredBucketList;
    }

    public void setFilteredBucketList(List<ResourceBucket> filteredBucketList) {
        this.filteredBucketList = filteredBucketList;
    } 

    public ResourceBucket getBucket() {
        if (bucket == null)
            bucket = new ResourceBucket();
        return bucket;
    }

    public List<SelectItem> getTypeList() {
        if (typeList == null) {
            typeList = new ArrayList<SelectItem>();
            
            for (ResourceBucketType rb : ResourceBucketType.values()) {
                typeList.add(new SelectItem(rb.toString()));
            }
        }
        return typeList;
    }

    public void setTypeList(List<SelectItem> typeList) {
        this.typeList = typeList;
    }

    public void setBucket(ResourceBucket bucket) {
        this.bucket = bucket;
    }
    
    public String create() {
        this.bucketFacade.save(bucket);
        return RESOURCE_BUCKET_PATH + "index";
    }
    
    public String show() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        session.setAttribute(SESSION_SELECTED_BUCKET, Long.valueOf(selected.getId()));
        return RESOURCE_BUCKET_PATH + "view";
    }
    
    public String update () {
        ResourceBucket old = bucketFacade.find(selected.getId());
        if (!old.equals(selected))
            bucketFacade.update(selected);
       return RESOURCE_BUCKET_PATH + "index";
    }
    
    public boolean filterByType(Object value, Object filter, Locale en) {
         return ((ResourceBucketType) value).toString().contains((String) filter);
    }
    
    private void loadBucketList() {
        bucketList = bucketFacade.findAll();
    }
}
