package com.github.freeacs.dao;

import com.github.freeacs.shared.Protocol;
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
public class UnitType implements DataSerializable {
    private Long id;
    private String name;
    private String vendor;
    private String description;
    private Protocol protocol;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeUTF(name);
        out.writeUTF(vendor);
        out.writeUTF(description);
        out.writeUTF(protocol.name());
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        name = in.readUTF();
        vendor = in.readUTF();
        description = in.readUTF();
        protocol = Protocol.valueOf(in.readUTF());
    }
}
