/**
 * DeleteUnittypeRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class DeleteUnittypeRequest  implements java.io.Serializable {
    private com.owera.xapsws.Login login;

    private java.lang.String unittypeName;

    public DeleteUnittypeRequest() {
    }

    public DeleteUnittypeRequest(
           com.owera.xapsws.Login login,
           java.lang.String unittypeName) {
           this.login = login;
           this.unittypeName = unittypeName;
    }


    /**
     * Gets the login value for this DeleteUnittypeRequest.
     * 
     * @return login
     */
    public com.owera.xapsws.Login getLogin() {
        return login;
    }


    /**
     * Sets the login value for this DeleteUnittypeRequest.
     * 
     * @param login
     */
    public void setLogin(com.owera.xapsws.Login login) {
        this.login = login;
    }


    /**
     * Gets the unittypeName value for this DeleteUnittypeRequest.
     * 
     * @return unittypeName
     */
    public java.lang.String getUnittypeName() {
        return unittypeName;
    }


    /**
     * Sets the unittypeName value for this DeleteUnittypeRequest.
     * 
     * @param unittypeName
     */
    public void setUnittypeName(java.lang.String unittypeName) {
        this.unittypeName = unittypeName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeleteUnittypeRequest)) return false;
        DeleteUnittypeRequest other = (DeleteUnittypeRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.login==null && other.getLogin()==null) || 
             (this.login!=null &&
              this.login.equals(other.getLogin()))) &&
            ((this.unittypeName==null && other.getUnittypeName()==null) || 
             (this.unittypeName!=null &&
              this.unittypeName.equals(other.getUnittypeName())));
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
        if (getLogin() != null) {
            _hashCode += getLogin().hashCode();
        }
        if (getUnittypeName() != null) {
            _hashCode += getUnittypeName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DeleteUnittypeRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">DeleteUnittypeRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("login");
        elemField.setXmlName(new javax.xml.namespace.QName("", "login"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Login"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unittypeName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unittypeName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
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
