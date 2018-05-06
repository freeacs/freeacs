/**
 * GetUnittypesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class GetUnittypesResponse  implements java.io.Serializable {
    private com.owera.xapsws.UnittypeList unittypes;

    public GetUnittypesResponse() {
    }

    public GetUnittypesResponse(
           com.owera.xapsws.UnittypeList unittypes) {
           this.unittypes = unittypes;
    }


    /**
     * Gets the unittypes value for this GetUnittypesResponse.
     * 
     * @return unittypes
     */
    public com.owera.xapsws.UnittypeList getUnittypes() {
        return unittypes;
    }


    /**
     * Sets the unittypes value for this GetUnittypesResponse.
     * 
     * @param unittypes
     */
    public void setUnittypes(com.owera.xapsws.UnittypeList unittypes) {
        this.unittypes = unittypes;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUnittypesResponse)) return false;
        GetUnittypesResponse other = (GetUnittypesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.unittypes==null && other.getUnittypes()==null) || 
             (this.unittypes!=null &&
              this.unittypes.equals(other.getUnittypes())));
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
        if (getUnittypes() != null) {
            _hashCode += getUnittypes().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetUnittypesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnittypesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unittypes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unittypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "unittypeList"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
