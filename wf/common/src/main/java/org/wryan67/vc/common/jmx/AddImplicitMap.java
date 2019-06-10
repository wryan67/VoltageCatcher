package org.wryan67.vc.common.jmx;

public class AddImplicitMap {
    public Class  ownerType;
    public String fieldName;
    public String itemName;
    public Class  itemType; 
    public String keyFieldName;

    public AddImplicitMap(Class ownerType, String fieldName, String itemName, Class itemType, String keyFieldName) {
        this.ownerType = ownerType;
        this.fieldName = fieldName;
        this.itemName = itemName;
        this.itemType = itemType;
        this.keyFieldName = keyFieldName;
    }
}
