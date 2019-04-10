package com.github.freeacs.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission implements DataSerializable {
    private Long id;
    private Long userId;
    private Long unitTypeId;
    private Long profileId;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeLong(userId);
        out.writeLong(unitTypeId);
        out.writeLong(profileId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        userId = in.readLong();
        unitTypeId = in.readLong();
        profileId = in.readLong();
    }
}
