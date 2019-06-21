package com.jaravir.tekila.ui.util;

import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;

import java.io.Serializable;

/**
 * Created by sajabrayilov on 12/10/2014.
 */
public class PermissionNode  implements Serializable {
    private String name;
    private String id;
    private SelectBooleanCheckbox check;

    public PermissionNode(String name, String id, SelectBooleanCheckbox check) {
        this.name = name;
        this.id = id;
        this.check = check;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SelectBooleanCheckbox getCheck() {
        return check;
    }

    public void setCheck(SelectBooleanCheckbox check) {
        this.check = check;
    }
}
