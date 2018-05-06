/**
 * GetProfilesRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class GetProfilesRequest  implements java.io.Serializable {
    private com.owera.xapsws.Login login;

    private com.owera.xapsws.Unittype unittype;

    private com.owera.xapsws.Profile profile;

    public GetProfilesRequest() {
    }

    public GetProfilesRequest(
           com.owera.xapsws.Login login,
           com.owera.xapsws.Unittype unittype,
           com.owera.xapsws.Profile profile) {
           this.login = login;
           this.unittype = unittype;
           this.profile = profile;
    }


    /**
     * Gets the login value for this GetProfilesRequest.
     * 
     * @return login
     */
    public com.owera.xapsws.Login getLogin() {
        return login;
    }


    /**
     * Sets the login value for this GetProfilesRequest.
     * 
     * @param login
     */
    public void setLogin(com.owera.xapsws.Login login) {
        this.login = login;
    }


    /**
     * Gets the unittype value for this GetProfilesRequest.
     * 
     * @return unittype
     */
    public com.owera.xapsws.Unittype getUnittype() {
        return unittype;
    }


    /**
     * Sets the unittype value for this GetProfilesRequest.
     * 
     * @param unittype
     */
    public void setUnittype(com.owera.xapsws.Unittype unittype) {
        this.unittype = unittype;
    }


    /**
     * Gets the profile value for this GetProfilesRequest.
     * 
     * @return profile
     */
    public com.owera.xapsws.Profile getProfile() {
        return profile;
    }


    /**
     * Sets the profile value for this GetProfilesRequest.
     * 
     * @param profile
     */
    public void setProfile(com.owera.xapsws.Profile profile) {
        this.profile = profile;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetProfilesRequest)) return false;
        GetProfilesRequest other = (GetProfilesRequest) obj;
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
            ((this.unittype==null && other.getUnittype()==null) || 
             (this.unittype!=null &&
              this.unittype.equals(other.getUnittype()))) &&
            ((this.profile==null && other.getProfile()==null) || 
             (this.profile!=null &&
              this.profile.equals(other.getProfile())));
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
        if (getUnittype() != null) {
            _hashCode += getUnittype().hashCode();
        }
        if (getProfile() != null) {
            _hashCode += getProfile().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetProfilesRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("login");
        elemField.setXmlName(new javax.xml.namespace.QName("", "login"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Login"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unittype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unittype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unittype"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("profile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "profile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Profile"));
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
