/**
 * UnitIdList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class UnitIdList  implements java.io.Serializable {
    private java.lang.String[] unitIdArray;

    public UnitIdList() {
    }

    public UnitIdList(
           java.lang.String[] unitIdArray) {
           this.unitIdArray = unitIdArray;
    }


    /**
     * Gets the unitIdArray value for this UnitIdList.
     * 
     * @return unitIdArray
     */
    public java.lang.String[] getUnitIdArray() {
        return unitIdArray;
    }


    /**
     * Sets the unitIdArray value for this UnitIdList.
     * 
     * @param unitIdArray
     */
    public void setUnitIdArray(java.lang.String[] unitIdArray) {
        this.unitIdArray = unitIdArray;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnitIdList)) return false;
        UnitIdList other = (UnitIdList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.unitIdArray==null && other.getUnitIdArray()==null) || 
             (this.unitIdArray!=null &&
              java.util.Arrays.equals(this.unitIdArray, other.getUnitIdArray())));
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
        if (getUnitIdArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnitIdArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnitIdArray(), i);
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
        new org.apache.axis.description.TypeDesc(UnitIdList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "unitIdList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitIdArray");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unitIdArray"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("", "unitId"));
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
