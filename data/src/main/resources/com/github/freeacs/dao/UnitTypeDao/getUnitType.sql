select unit_type_id as id, unit_type_name as name, vendor_name as vendor, description, protocol from unit_type where unit_type_id = :id;
