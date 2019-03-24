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
public class UnitTypeParameter implements DataSerializable {
    private Long id;
    private String name;
    private String flags;
    private Long unitTypeId;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeUTF(name);
        out.writeUTF(flags);
        out.writeLong(unitTypeId);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        name = in.readUTF();
        flags = in.readUTF();
        unitTypeId = in.readLong();
    }
}
