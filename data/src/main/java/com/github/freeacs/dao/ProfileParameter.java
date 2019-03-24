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
public class ProfileParameter implements DataSerializable {
    private Long profileId;
    private Long unitTypeParamId;
    private String value;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(profileId);
        out.writeLong(unitTypeParamId);
        out.writeUTF(value);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        profileId = in.readLong();
        unitTypeParamId = in.readLong();
        value = in.readUTF();
    }
}
