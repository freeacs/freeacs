package com.github.freeacs.dbi;

public class ACSDao {

    private static final String GET_UNITTYPE_BY_ID = "SELECT unit_type_id, matcher_id, unit_type_name, vendor_name, description, protocol FROM unit_type WHERE unit_type_id = ?";

    private static final String GET_UNITTYPE_PARAMETERS_BY_UNITTYPE_ID = "SELECT unit_type_param_id, unit_type_id, name, value, flags FROM unit_type_param WHERE unit_type_id = ?";

    private static final String GET_PROFILE_PARAMETERS_BY_PROFILE_ID = "SELECT profile_id, unit_type_param_id, value FROM profile_param WHERE profile_id = ?";

    private static final String GET_JOB_PARAMETERS_BY_JOB_ID = "SELECT job_id, unit_type_param_id, value FROM job_param WHERE job_id = ?";

    private static final String GET_UNIT_PARAMETERS_BY_UNIT_ID = "SELECT unit_id, unit_type_param_id, value FROM unit_param WHERE unit_id = ?";


    // TODO add methods to get unittype, profile, job, and unit parameters, similar to the methods in ACS.java, but without the map memoization
    // instead use the ACSCacheManager to memoize the results

}
