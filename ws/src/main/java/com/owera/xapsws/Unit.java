/**
 * Unit.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class Unit  implements java.io.Serializable {
    private java.lang.String unitId;

    private java.lang.String serialNumber;

    private com.owera.xapsws.Profile profile;

    private com.owera.xapsws.Unittype unittype;

    private com.owera.xapsws.ParameterList parameters;

    public Unit() {
    }

    public Unit(
           java.lang.String unitId,
           java.lang.String serialNumber,
           com.owera.xapsws.Profile profile,
           com.owera.xapsws.Unittype unittype,
           com.owera.xapsws.ParameterList parameters) {
           this.unitId = unitId;
           this.serialNumber = serialNumber;
           this.profile = profile;
           this.unittype = unittype;
           this.parameters = parameters;
    }


    /**
     * Gets the unitId value for this Unit.
     * 
     * @return unitId
     */
    public java.lang.String getUnitId() {
        return unitId;
    }


    /**
     * Sets the unitId value for this Unit.
     * 
     * @param unitId
     */
    public void setUnitId(java.lang.String unitId) {
        this.unitId = unitId;
    }


    /**
     * Gets the serialNumber value for this Unit.
     * 
     * @return serialNumber
     */
    public java.lang.String getSerialNumber() {
        return serialNumber;
    }


    /**
     * Sets the serialNumber value for this Unit.
     * 
     * @param serialNumber
     */
    public void setSerialNumber(java.lang.String serialNumber) {
        this.serialNumber = serialNumber;
    }


    /**
     * Gets the profile value for this Unit.
     * 
     * @return profile
     */
    public com.owera.xapsws.Profile getProfile() {
        return profile;
    }


    /**
     * Sets the profile value for this Unit.
     * 
     * @param profile
     */
    public void setProfile(com.owera.xapsws.Profile profile) {
        this.profile = profile;
    }


    /**
     * Gets the unittype value for this Unit.
     * 
     * @return unittype
     */
    public com.owera.xapsws.Unittype getUnittype() {
        return unittype;
    }


    /**
     * Sets the unittype value for this Unit.
     * 
     * @param unittype
     */
    public void setUnittype(com.owera.xapsws.Unittype unittype) {
        this.unittype = unittype;
    }


    /**
     * Gets the parameters value for this Unit.
     * 
     * @return parameters
     */
    public com.owera.xapsws.ParameterList getParameters() {
        return parameters;
    }


    /**
     * Sets the parameters value for this Unit.
     * 
     * @param parameters
     */
    public void setParameters(com.owera.xapsws.ParameterList parameters) {
        this.parameters = parameters;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Unit)) return false;
        Unit other = (Unit) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.unitId==null && other.getUnitId()==null) || 
             (this.unitId!=null &&
              this.unitId.equals(other.getUnitId()))) &&
            ((this.serialNumber==null && other.getSerialNumber()==null) || 
             (this.serialNumber!=null &&
              this.serialNumber.equals(other.getSerialNumber()))) &&
            ((this.profile==null && other.getProfile()==null) || 
             (this.profile!=null &&
              this.profile.equals(other.getProfile()))) &&
            ((this.unittype==null && other.getUnittype()==null) || 
             (this.unittype!=null &&
              this.unittype.equals(other.getUnittype()))) &&
            ((this.parameters==null && other.getParameters()==null) || 
             (this.parameters!=null &&
              this.parameters.equals(other.getParameters())));
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
        if (getUnitId() != null) {
            _hashCode += getUnitId().hashCode();
        }
        if (getSerialNumber() != null) {
            _hashCode += getSerialNumber().hashCode();
        }
        if (getProfile() != null) {
            _hashCode += getProfile().hashCode();
        }
        if (getUnittype() != null) {
            _hashCode += getUnittype().hashCode();
        }
        if (getParameters() != null) {
            _hashCode += getParameters().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Unit.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unit"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unitId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unitId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serialNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("", "serialNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("profile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "profile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Profile"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("unittype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "unittype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "Unittype"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameters");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "parameterList"));
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
