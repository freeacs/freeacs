package com.github.freeacs.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit implements DataSerializable {
    private String unitId;
    private Long profileId;
    private Long unitTypeId;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(unitId);
        out.writeLong(profileId);
        out.writeLong(unitTypeId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        unitId = in.readUTF();
        profileId = in.readLong();
        unitTypeId = in.readLong();
    }
}
