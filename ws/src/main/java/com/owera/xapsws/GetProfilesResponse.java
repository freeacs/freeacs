/**
 * GetProfilesResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class GetProfilesResponse  implements java.io.Serializable {
    private com.owera.xapsws.ProfileList profiles;

    public GetProfilesResponse() {
    }

    public GetProfilesResponse(
           com.owera.xapsws.ProfileList profiles) {
           this.profiles = profiles;
    }


    /**
     * Gets the profiles value for this GetProfilesResponse.
     * 
     * @return profiles
     */
    public com.owera.xapsws.ProfileList getProfiles() {
        return profiles;
    }


    /**
     * Sets the profiles value for this GetProfilesResponse.
     * 
     * @param profiles
     */
    public void setProfiles(com.owera.xapsws.ProfileList profiles) {
        this.profiles = profiles;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetProfilesResponse)) return false;
        GetProfilesResponse other = (GetProfilesResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.profiles==null && other.getProfiles()==null) || 
             (this.profiles!=null &&
              this.profiles.equals(other.getProfiles())));
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
        if (getProfiles() != null) {
            _hashCode += getProfiles().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetProfilesResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetProfilesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("profiles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "profiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "profileList"));
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
