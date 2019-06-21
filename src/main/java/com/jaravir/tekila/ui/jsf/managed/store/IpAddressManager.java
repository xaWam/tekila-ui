package com.jaravir.tekila.ui.jsf.managed.store;


import com.jaravir.tekila.module.store.RangePersistenceFacade;
import com.jaravir.tekila.module.store.StorePersistenceFacade;
import com.jaravir.tekila.module.store.ip.persistence.IpAddress;
import com.jaravir.tekila.module.store.ip.persistence.IpAddressRange;
import com.jaravir.tekila.module.store.nas.Nas;
import com.jaravir.tekila.module.store.nas.NasPersistenceFacade;
import com.jaravir.tekila.ui.model.LazyTableModel;
import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.LazyDataModel;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.print.attribute.standard.Severity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajabrayilov on 11/25/2014.
 */
@ManagedBean
@ViewScoped
public class IpAddressManager implements Serializable {
    private transient final static Logger log = Logger.getLogger(IpAddressManager.class);
    private final static String PATH = "/pages/store/range/";

    @EJB
    private StorePersistenceFacade storeFacade;
    @EJB
    private RangePersistenceFacade rangeFacade;
    @EJB
    private NasPersistenceFacade nasFacade;

    private String startAddress_octet_1;
    private String startAddress_octet_2;
    private String startAddress_octet_3;
    private String startAddress_octet_4;

    private String endAddress_octet_1;
    private String endAddress_octet_2;
    private String endAddress_octet_3;
    private String endAddress_octet_4;

    private IpAddressRange range;

    //view
    private LazyDataModel<IpAddressRange> rangeList;
    private IpAddressRange selected;
    private List<IpAddressRange> filteredList;
    private Long rangeID;

    private List<Nas> nasSelectList;
    private Nas selectedNas;

    public Nas getSelectedNas() {
        return selectedNas;
    }

    public void setSelectedNas(Nas selectedNas) {
        this.selectedNas = selectedNas;
    }

    public List<Nas> getNasSelectList() {
        if (nasSelectList == null) {
            nasSelectList = nasFacade.findAllNas();
            log.debug("Nas List Size: " + nasSelectList.size());
        }
        return nasSelectList;
    }

    public void setNasSelectList(List<Nas> nasSelectList) {
        this.nasSelectList = nasSelectList;
    }

    public IpAddressRange getRange() {
        return range;
    }

    public void setRange(IpAddressRange range) {
        this.range = range;
    }

    public String getStartAddress_octet_1() {
        return startAddress_octet_1;
    }

    public void setStartAddress_octet_1(String startAddress_octet_1) {
        this.startAddress_octet_1 = startAddress_octet_1;
    }

    public String getStartAddress_octet_2() {
        return startAddress_octet_2;
    }

    public void setStartAddress_octet_2(String startAddress_octet_2) {
        this.startAddress_octet_2 = startAddress_octet_2;
    }

    public String getStartAddress_octet_3() {
        return startAddress_octet_3;
    }

    public void setStartAddress_octet_3(String startAddress_octet_3) {
        this.startAddress_octet_3 = startAddress_octet_3;
    }

    public String getStartAddress_octet_4() {
        return startAddress_octet_4;
    }

    public void setStartAddress_octet_4(String startAddress_octet_4) {
        this.startAddress_octet_4 = startAddress_octet_4;
    }

    public String getEndAddress_octet_1() {
        return endAddress_octet_1;
    }

    public void setEndAddress_octet_1(String endAddress_octet_1) {
        this.endAddress_octet_1 = endAddress_octet_1;
    }

    public String getEndAddress_octet_2() {
        return endAddress_octet_2;
    }

    public void setEndAddress_octet_2(String endAddress_octet_2) {
        this.endAddress_octet_2 = endAddress_octet_2;
    }

    public String getEndAddress_octet_3() {
        return endAddress_octet_3;
    }

    public void setEndAddress_octet_3(String endAddress_octet_3) {
        this.endAddress_octet_3 = endAddress_octet_3;
    }

    public String getEndAddress_octet_4() {
        return endAddress_octet_4;
    }

    public void setEndAddress_octet_4(String endAddress_octet_4) {
        this.endAddress_octet_4 = endAddress_octet_4;
    }

    public LazyDataModel<IpAddressRange> getRangeList() {
        if (rangeList == null) {
            rangeList = new LazyTableModel<>(rangeFacade);
        }
        return rangeList;
    }

    public void setRangeList(LazyDataModel<IpAddressRange> rangeList) {
        this.rangeList = rangeList;
    }

    public IpAddressRange getSelected() {
        if (selected == null && rangeID != null)
            selected = rangeFacade.find(rangeID);

        return selected;
    }

    public void setSelected(IpAddressRange selected) {
        this.selected = selected;
    }

    public List<IpAddressRange> getFilteredList() {
        return filteredList;
    }

    public void setFilteredList(List<IpAddressRange> filteredList) {
        this.filteredList = filteredList;
    }

    public Long getRangeID() {
        return rangeID;
    }

    public void setRangeID(Long rangeID) {
        this.rangeID = rangeID;
    }

    public String create() {
        if (startAddress_octet_1 == null || startAddress_octet_1.isEmpty()
                || startAddress_octet_2 == null || startAddress_octet_2.isEmpty()
                || startAddress_octet_3 == null || startAddress_octet_3.isEmpty()
                || startAddress_octet_4 == null || startAddress_octet_4.isEmpty()
                || endAddress_octet_1 == null || endAddress_octet_1.isEmpty()
                || endAddress_octet_2 == null || endAddress_octet_2.isEmpty()
                || endAddress_octet_3 == null || endAddress_octet_3.isEmpty()
                || endAddress_octet_4 == null || endAddress_octet_4.isEmpty()
                || selectedNas == null
                ) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "All fields are required", "All fields are required"));
            return null;
        }

        try {
            IpAddress start = new IpAddress(
                    Integer.valueOf(startAddress_octet_1),
                    Integer.valueOf(startAddress_octet_2),
                    Integer.valueOf(startAddress_octet_3),
                    Integer.valueOf(startAddress_octet_4)
            );

            IpAddress end = new IpAddress(
                    Integer.valueOf(endAddress_octet_1),
                    Integer.valueOf(endAddress_octet_2),
                    Integer.valueOf(endAddress_octet_3),
                    Integer.valueOf(endAddress_octet_4)
            );

            log.debug(String.format("Start address=%s, end address=%s", start, end));

            if (end.isBeforeOrEquals(start)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Range is invalid", "Range is invalid"));
                return null;
            }

            Nas nas = nasFacade.update(selectedNas);

            range = new IpAddressRange(start, end, nas);
            rangeFacade.save(range);

            return PATH + "index.xhtml";
        } catch (Exception ex) {
            log.debug(ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "PLease fill all fields with numbers from 0 to 255. 0.0.0.0, 255.255.255.255, x.x.x.0 and x.x.x.255 are not allowed",
                    "PLease fill all fields with numbers from 0 to 255. 0.0.0.0, 255.255.255.255, x.x.x.0 and x.x.x.255 are not allowed")
            );
            return null;
        }
    }

    public String save() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, "Ok", "Ok"));
        return null;
    }

    public String view() {
        log.debug("selected range: " + selected);
        if (selected == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select a range", "Please select a range"));
            return null;
        }

        return new StringBuilder(PATH).append("range.xhtml?range=").append(selected.getId()).append("&amp;faces-redirect=true&amp;includeViewParams=true").toString();
    }

    public String v() {
        String res = PATH + "range.xhtml";
        log.debug("Range path=" + res);
        return res;
    }

    public void onRowEdit(RowEditEvent event) {
        IpAddressRange range = (IpAddressRange) event.getObject();
        rangeFacade.update(range);
    }

}
