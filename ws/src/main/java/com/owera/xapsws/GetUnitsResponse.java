/**
 * GetUnitsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.owera.xapsws;

public class GetUnitsResponse  implements java.io.Serializable {
    private com.owera.xapsws.UnitList units;

    private boolean moreUnits;

    public GetUnitsResponse() {
    }

    public GetUnitsResponse(
           com.owera.xapsws.UnitList units,
           boolean moreUnits) {
           this.units = units;
           this.moreUnits = moreUnits;
    }


    /**
     * Gets the units value for this GetUnitsResponse.
     * 
     * @return units
     */
    public com.owera.xapsws.UnitList getUnits() {
        return units;
    }


    /**
     * Sets the units value for this GetUnitsResponse.
     * 
     * @param units
     */
    public void setUnits(com.owera.xapsws.UnitList units) {
        this.units = units;
    }


    /**
     * Gets the moreUnits value for this GetUnitsResponse.
     * 
     * @return moreUnits
     */
    public boolean isMoreUnits() {
        return moreUnits;
    }


    /**
     * Sets the moreUnits value for this GetUnitsResponse.
     * 
     * @param moreUnits
     */
    public void setMoreUnits(boolean moreUnits) {
        this.moreUnits = moreUnits;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUnitsResponse)) return false;
        GetUnitsResponse other = (GetUnitsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.units==null && other.getUnits()==null) || 
             (this.units!=null &&
              this.units.equals(other.getUnits()))) &&
            this.moreUnits == other.isMoreUnits();
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
        if (getUnits() != null) {
            _hashCode += getUnits().hashCode();
        }
        _hashCode += (isMoreUnits() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetUnitsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", ">GetUnitsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("units");
        elemField.setXmlName(new javax.xml.namespace.QName("", "units"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xapsws.owera.com/", "unitList"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moreUnits");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moreUnits"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
