package com.github.freeacs.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.io.IOException;

@Data
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class Profile implements DataSerializable {
    private Long id;
    private String name;
    private Long unitTypeId;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeUTF(name);
        out.writeLong(unitTypeId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        name = in.readUTF();
        unitTypeId = in.readLong();
    }
}
