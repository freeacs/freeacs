/**
 * UnitList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class UnitList  implements java.io.Serializable {
    private com.owera.xapsws.Unit[] unitArray;

    public UnitList() {
    }

    public UnitList(
           com.owera.xapsws.Unit[] unitArray) {
           this.unitArray = unitArray;
    }


    /**
     * Gets the unitArray value for this UnitList.
     * 
     * @return unitArray
     */
    public com.owera.xapsws.Unit[] getUnitArray() {
        return unitArray;
    }


    /**
     * Sets the unitArray value for this UnitList.
     * 
     * @param unitArray
     */
    public void setUnitArray(com.owera.xapsws.Unit[] unitArray) {
        this.unitArray = unitArray;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnitList)) return false;
        UnitList other = (UnitList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.unitArray==null && other.getUnitArray()==null) || 
             (this.unitArray!=null &&
              java.util.Arrays.equals(this.unitArray, other.getUnitArray())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getUnitArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnitArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnitArray(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UnitList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "unitList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitArray");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unitArray"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unit"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("", "item"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
