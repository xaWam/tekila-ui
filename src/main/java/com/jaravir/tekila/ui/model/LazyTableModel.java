/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaravir.tekila.ui.model;

import com.jaravir.tekila.base.entity.BaseEntity;
import com.jaravir.tekila.base.filter.Filterable;
import com.jaravir.tekila.base.persistence.Paginable;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class LazyTableModel<T extends BaseEntity> extends LazyDataModel<T> {

    private List<T> subList;
    private final Paginable subFacade;
    private transient final static Logger log = Logger.getLogger(LazyTableModel.class);

    public LazyTableModel(AbstractPersistenceFacade<T> subFacade) {
        this.subFacade = subFacade;
        log.debug(subFacade.toString());
    }

    /**
     *
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return
     */
    //@Override
    public List<T> loadOld(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        subFacade.clearFilters();
        if (filters != null && filters.size() > 0) {
            Iterator<String> it = filters.keySet().iterator();
            log.debug("has more filters? " + it.hasNext());
            boolean match = false;

            while (it.hasNext()) {
                try {
                    String filteredPropertyName = it.next();
                    Filterable filter = subFacade.getFilterByCode(filteredPropertyName);

                    if (filter != null) {
                        subFacade.addFilter(filter, filters.get(filteredPropertyName));
                    }
                    // log.debug("propertyName="+filteredPropertyName+", value="+actualValue+", filter="+filteredValue+", match="+match);
                } catch (IllegalArgumentException ex) {
                    log.error(ex);
                }

            } //end while on filters
            //log.debug("after all filters: sub=" + sub.getId() + ", match=" + match);

        } //end if filters != null

        subList = subFacade.findAllPaginated(first, pageSize);
        log.debug("initial sublist: " + (subList != null ? subList.size() : subList));

        if (subList == null) {
            subList = subFacade.findAllPaginated(first, first % pageSize);
        }

        /*if (filters != null)
            log.debug("initial subList=" + subList.size()+", filters="+filters.toString()+", filtersSize="+filters.size());
         */
        //filtration
        //set total row count
        //this.setRowCount(subList.size());
        int totalRowCount = (int) subFacade.count();
        log.debug("total row count: " + totalRowCount);
        this.setRowCount(totalRowCount);
        //this.setRowCount(filteredList.size());

        //log.debug(String.format("Count=%d, filtered sublist=%s", totalRowCount, filteredList));
        //log.debug(String.format("Count=%d", totalRowCount));
        /* List<T> filteredListAsList = new ArrayList<T>(filteredList);
        this.setWrappedData(filteredListAsList);
        return filteredListAsList;
         */
        return new ArrayList<T>(subList);
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        //log.debug(String.format("Loading pageSize=%d, first=%d", pageSize, first));
        List<T> filteredList = new ArrayList<>();

        subList = subFacade.findAllPaginated(first, pageSize);
        //log.debug("initial sublist: " + (subList != null ? subList.size() : subList));

        if (subList == null) {
            subList = subFacade.findAllPaginated(first, first % pageSize);
        }

        /*if (filters != null)
            log.debug("initial subList=" + subList.size()+", filters="+filters.toString()+", filtersSize="+filters.size());
         */
        //filtration
        StringBuilder sb = new StringBuilder("Sublist structure: {");

        for (T sub : subList) {
            //log.debug("sub: " + sub + ", filter: " + filters + ", cond: " + (filters != null && filters.size() > 0));
            sb.append(sub.getId()).append(", ");

            if (filters != null && filters.size() > 0) {
                Iterator<String> it = filters.keySet().iterator();
                //log.debug("has more filters? " + it.hasNext());
                boolean match = false;

                while (it.hasNext()) {

                    try {
                        String filteredPropertyName = it.next();
                        String filteredValue = (String) filters.get(filteredPropertyName);
                        String actualValue = getFiltrationResult(filteredPropertyName, sub);
                        //         log.debug("actualValue="+actualValue);
                        //        log.debug("after method invoc: " + actualValue);
                        //actualValue = sub.getClass().getMethod("get" + filteredPropertyName, Subscriber.class).invoke(sub).toString();
                        if (filteredValue == null || actualValue.toLowerCase().startsWith(filteredValue.toLowerCase())) //filteredList.add(sub);
                        {
                            match = true;
                        } else {
                            match = false;
                            break;
                        }
                        // log.debug("propertyName="+filteredPropertyName+", value="+actualValue+", filter="+filteredValue+", match="+match);
                    } catch (NoSuchMethodException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (IllegalAccessException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (IllegalArgumentException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (InvocationTargetException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (NullPointerException ex) {
                        log.error(ex);
                        break;
                    }
                } //end while on filters
                //log.debug("after all filters: sub=" + sub.getId() + ", match=" + match);
                if (match) {
                    filteredList.add(sub);
                }

            } //end if filters != null
            else {
                filteredList.add(sub);
            }
        }

        sb.substring(0, sb.length() - 1);
        sb.append("}");
        //log.debug(sb.toString());

        //set total row count
        //this.setRowCount(subList.size());
        if (this.getRowCount() == 0) {
            int totalRowCount = (int) subFacade.count();
            //log.debug("total row count: " + totalRowCount);
            this.setRowCount(totalRowCount);
        }
        //this.setRowCount(filteredList.size());

        //log.debug(String.format("Count=%d, filtered sublist=%s", totalRowCount, filteredList));
        //log.debug(String.format("Count=%d", totalRowCount));
        /* List<T> filteredListAsList = new ArrayList<T>(filteredList);
        this.setWrappedData(filteredListAsList);
        return filteredListAsList;
         */
        //log.debug(String.format("Filtered list size=%d", filteredList.size()));
        //log.debug("Finished loading page");
        return filteredList;
    }

    public List<T> loadOld2(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        log.debug(String.format("Loading pageSize=%d, first=%d", pageSize, first));

        Set<T> filteredList = new LinkedHashSet<T>();

        subList = subFacade.findAllPaginated(first, pageSize);
        log.debug("initial sublist: " + (subList != null ? subList.size() : subList));

        if (subList != null) {
            StringBuilder sb = new StringBuilder("Sublist structure: {");
            for (T el : subList) {
                sb.append(el.getId()).append(", ");
            }
            sb.substring(0, sb.length() - 1);
            sb.append("}");
            log.debug(sb.toString());
        }

        if (subList == null) {
            subList = subFacade.findAllPaginated(first, first % pageSize);
        }

        /*if (filters != null)
            log.debug("initial subList=" + subList.size()+", filters="+filters.toString()+", filtersSize="+filters.size());
         */
        //filtration
        for (T sub : subList) {
            //log.debug("sub: " + sub + ", filter: " + filters + ", cond: " + (filters != null && filters.size() > 0));

            if (filters != null && filters.size() > 0) {
                Iterator<String> it = filters.keySet().iterator();
                log.debug("has more filters? " + it.hasNext());
                boolean match = false;

                while (it.hasNext()) {

                    try {
                        String filteredPropertyName = it.next();
                        String filteredValue = (String) filters.get(filteredPropertyName);
                        String actualValue = getFiltrationResult(filteredPropertyName, sub);
                        //         log.debug("actualValue="+actualValue);
                        //        log.debug("after method invoc: " + actualValue);
                        //actualValue = sub.getClass().getMethod("get" + filteredPropertyName, Subscriber.class).invoke(sub).toString();
                        if (filteredValue == null || actualValue.toLowerCase().startsWith(filteredValue.toLowerCase())) //filteredList.add(sub);
                        {
                            match = true;
                        } else {
                            match = false;
                            break;
                        }
                        // log.debug("propertyName="+filteredPropertyName+", value="+actualValue+", filter="+filteredValue+", match="+match);
                    } catch (NoSuchMethodException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (IllegalAccessException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (IllegalArgumentException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (InvocationTargetException ex) {
                        log.error(ex);
                        filteredList.add(sub);
                    } catch (NullPointerException ex) {
                        log.error(ex);
                        break;
                    }
                } //end while on filters
                //log.debug("after all filters: sub=" + sub.getId() + ", match=" + match);
                if (match) {
                    filteredList.add(sub);
                }

            } //end if filters != null
            else {
                filteredList.add(sub);
            }
        }
        //set total row count
        //this.setRowCount(subList.size());
        int totalRowCount = (int) subFacade.count();
        log.debug("total row count: " + totalRowCount);
        this.setRowCount(totalRowCount);
        //this.setRowCount(filteredList.size());

        //log.debug(String.format("Count=%d, filtered sublist=%s", totalRowCount, filteredList));
        //log.debug(String.format("Count=%d", totalRowCount));
        /* List<T> filteredListAsList = new ArrayList<T>(filteredList);
        this.setWrappedData(filteredListAsList);
        return filteredListAsList;
         */
        log.debug("Finished loading page");
        return new ArrayList<T>(filteredList);
    }

    /* @Override
    public T getRowData(String rowKey) {
        //log.debug("getRowData: rowKey=" + rowKey);
        for (T sub : subList) {
            if (Long.toString(sub.getId()).equals(rowKey))
                return sub;
        }
        return null;
    }
     */
    @Override
    public Object getRowKey(T sub) {
        return Long.toString(sub.getId());
    }

    @Override
    public T getRowData(String rowKey) {
        log.debug("getRowData: rowKey=" + rowKey + ", wrappedData=" + ((List<T>) getWrappedData()));
        Long id = Long.valueOf(rowKey);

        for (T sub : (List<T>) getWrappedData()) {
            if (id.equals(sub.getId())) {
                return sub;
            }
        }

        return null;
    }

    private String getFiltrationResult(String propertyName, T subject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String[] propertyArray;
        Object ref = subject;

        if (propertyName.indexOf('.') == -1) {
            propertyArray = new String[1];
            propertyArray[0] = propertyName;
        } else {
            propertyArray = propertyName.split("\\.");
        }

        log.debug("sub=" + subject.toString() + ", propertyName=" + propertyName + ", propertyArray=" + Arrays.asList(propertyArray));

        for (String property : propertyArray) {
            if (ref == null) {
                break;
            }

            try {
                // log.debug("iteration: property="+property+", method="+ref.getClass().getMethod("get" + WordUtils.capitalize(property)).toString());

                ref = ref.getClass().getMethod("get" + WordUtils.capitalize(property)).invoke(ref);
                // log.debug("iteration returned: " + ref);
            } catch (NullPointerException ex) {
                break;
            } catch (InvocationTargetException ex) {
                //log.debug("exception thrown caused by: " + ex.getCause());
                break;
            }
        }
        //log.debug("filtration returns " + ref.toString());
        return (ref != null) ? ref.toString() : null;
    }
}
