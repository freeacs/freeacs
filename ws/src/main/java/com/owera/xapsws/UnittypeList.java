/**
 * UnittypeList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class UnittypeList  implements java.io.Serializable {
    private com.owera.xapsws.Unittype[] unittypeArray;

    public UnittypeList() {
    }

    public UnittypeList(
           com.owera.xapsws.Unittype[] unittypeArray) {
           this.unittypeArray = unittypeArray;
    }


    /**
     * Gets the unittypeArray value for this UnittypeList.
     * 
     * @return unittypeArray
     */
    public com.owera.xapsws.Unittype[] getUnittypeArray() {
        return unittypeArray;
    }


    /**
     * Sets the unittypeArray value for this UnittypeList.
     * 
     * @param unittypeArray
     */
    public void setUnittypeArray(com.owera.xapsws.Unittype[] unittypeArray) {
        this.unittypeArray = unittypeArray;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UnittypeList)) return false;
        UnittypeList other = (UnittypeList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.unittypeArray==null && other.getUnittypeArray()==null) || 
             (this.unittypeArray!=null &&
              java.util.Arrays.equals(this.unittypeArray, other.getUnittypeArray())));
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
        if (getUnittypeArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getUnittypeArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getUnittypeArray(), i);
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
        new org.apache.axis.description.TypeDesc(UnittypeList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "unittypeList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unittypeArray");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unittypeArray"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unittype"));
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
