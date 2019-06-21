package com.jaravir.tekila.ui.model;

import com.jaravir.tekila.base.entity.BaseEntity;
import com.jaravir.tekila.base.filter.Filterable;
import com.jaravir.tekila.base.persistence.facade.AbstractPersistenceFacade;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.*;

/**
 * Created by kmaharov on 29.08.2016.
 */
public class FilterableLazyTableModel<T extends BaseEntity> extends LazyDataModel<T> {
    private AbstractPersistenceFacade<T> facade;
    private Map<String, Filterable> filterToFilterable;

    public FilterableLazyTableModel(
            AbstractPersistenceFacade<T> facade,
            List<? extends Filterable> filterables) {
        this.facade = facade;
        this.filterToFilterable = new HashMap<>();
        for (final Filterable f : filterables) {
            filterToFilterable.put(f.getField(), f);
        }
    }

    @Override
    public T getRowData(String rowKey) {
        return facade.find(Long.parseLong(rowKey));
    }

    @Override
    public Object getRowKey(T subModule) {
        return subModule.getId();
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
        facade.clearFilters();

        if (filters != null) {
            for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
                String filterProperty = it.next();
                Object filterValue = filters.get(filterProperty);
                Filterable f = filterToFilterable.get(filterProperty);
                if (f != null) {
                    facade.addFilter(f, filterValue);
                }
            }
        }

        int dataSize = (int) facade.count();
        this.setRowCount(dataSize);

        return facade.findAllPaginated(first, pageSize);
    }
}