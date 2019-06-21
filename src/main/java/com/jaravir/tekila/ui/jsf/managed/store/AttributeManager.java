package com.jaravir.tekila.ui.jsf.managed.store;

import com.jaravir.tekila.module.store.attribute.AttributePersistenceFacade;
import com.jaravir.tekila.module.store.nas.Attribute;
import com.jaravir.tekila.module.store.nas.Nas;
import com.jaravir.tekila.module.store.nas.NasPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shnovruzov on 6/8/2016.
 */
@ManagedBean
@ViewScoped
public class AttributeManager implements Serializable {
    private final static Logger log = Logger.getLogger(AttributeManager.class);

    @EJB
    private AttributePersistenceFacade attributeFacade;
    @EJB
    private NasPersistenceFacade nasFacade;

    private Long id;
    private String name;
    private Integer tag;
    private String value;
    private String description;
    private Integer status;
    //    private Long nasId;
    private String newName;
    private Integer newTag;
    private String newValue;
    private Integer newStatus;

    private LazyDataModel<Attribute> attributeList;
    private LazyDataModel<Attribute> nasAttributeList;
    private Attribute selectedAtt;
    private List<Nas> nasSelectList;
    private List<SelectItem> statusList;

    @PostConstruct
    private void init() {
        attributeFacade.clearFilters();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Attribute getSelectedAtt() {
        return selectedAtt;
    }

    public void setSelectedAtt(Attribute selectedAtt) {
        this.selectedAtt = selectedAtt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LazyDataModel<Attribute> getAttributeList() {
        if (attributeList == null) {
            attributeList = new LazyTableModel<>(attributeFacade);
            log.debug("Found attrubutes " + attributeFacade.count());
        }
        return attributeList;
    }

    public void setAttributeList(LazyDataModel<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public Integer getNewTag() {
        return newTag;
    }

    public void setNewTag(Integer newTag) {
        this.newTag = newTag;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Integer getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Integer newStatus) {
        this.newStatus = newStatus;
    }

    public List<Nas> getNasSelectList() {
        if (nasSelectList == null) {
            nasSelectList = nasFacade.findAllNas();
        }
        return nasSelectList;
    }

    public void setNasSelectList(List<Nas> nasSelectList) {
        this.nasSelectList = nasSelectList;
    }

    public List<SelectItem> getStatusList() {
        if (statusList == null) {
            statusList = new ArrayList<>();
            statusList.add(new SelectItem(1, "Available"));
            statusList.add(new SelectItem(0, "Not available"));
        }
        return statusList;
    }

    public void setStatusList(List<SelectItem> statusList) {
        this.statusList = statusList;
    }

    public boolean check() {
        if (name == null || name.isEmpty()
                || value == null || value.isEmpty() || status == null) return false;
        return true;
    }

    public void search() {
        attributeFacade.clearFilters();
        if (name != null && !name.isEmpty())
            attributeFacade.addFilter(AttributePersistenceFacade.Filter.NAME, name);
        if (id != null)
            attributeFacade.addFilter(AttributePersistenceFacade.Filter.ID, id);
        if (tag != null)
            attributeFacade.addFilter(AttributePersistenceFacade.Filter.TAG, tag);
        if (description != null && !description.isEmpty())
            attributeFacade.addFilter(AttributePersistenceFacade.Filter.DESCRIPTION, description);
        if (value != null && !value.isEmpty())
            attributeFacade.addFilter(AttributePersistenceFacade.Filter.VALUE, value);
        if (status != null)
            attributeFacade.addFilter(AttributePersistenceFacade.Filter.STATUS, status);

        attributeList = new LazyTableModel<>(attributeFacade);
        long count = attributeFacade.count();
        log.debug(String.format("Found %s attribute", count));
    }

    public void create() throws IOException {

        if (!check()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fill in the fields", "Fill in the fields"));
            return;
        }

        attributeFacade.create(name, value, tag, description, status);

        FacesContext.getCurrentInstance().getExternalContext().redirect("/pages/store/radius/attribute/index.xhtml");
        RequestContext.getCurrentInstance().update("attForm");

    }

    public void onRowEdit(RowEditEvent event) {
        Attribute attribute = (Attribute) event.getObject();
        if (newStatus != null)
            attribute.setStatus(newStatus);
        attributeFacade.update(attribute);
    }

}
