/**
 * ParameterList.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class ParameterList  implements java.io.Serializable {
    private com.owera.xapsws.Parameter[] parameterArray;

    public ParameterList() {
    }

    public ParameterList(
           com.owera.xapsws.Parameter[] parameterArray) {
           this.parameterArray = parameterArray;
    }


    /**
     * Gets the parameterArray value for this ParameterList.
     * 
     * @return parameterArray
     */
    public com.owera.xapsws.Parameter[] getParameterArray() {
        return parameterArray;
    }


    /**
     * Sets the parameterArray value for this ParameterList.
     * 
     * @param parameterArray
     */
    public void setParameterArray(com.owera.xapsws.Parameter[] parameterArray) {
        this.parameterArray = parameterArray;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ParameterList)) return false;
        ParameterList other = (ParameterList) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.parameterArray==null && other.getParameterArray()==null) || 
             (this.parameterArray!=null &&
              java.util.Arrays.equals(this.parameterArray, other.getParameterArray())));
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
        if (getParameterArray() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParameterArray());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParameterArray(), i);
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
        new org.apache.axis.description.TypeDesc(ParameterList.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "parameterList"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameterArray");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameterArray"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Parameter"));
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
