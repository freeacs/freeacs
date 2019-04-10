package com.github.freeacs.dao;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.io.IOException;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class User implements DataSerializable {
    private Long id;
    private String username;
    private String secret;
    private String fullname;
    private String accesslist;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(id);
        out.writeUTF(username);
        out.writeUTF(secret);
        out.writeUTF(fullname);
        out.writeUTF(accesslist);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readLong();
        username = in.readUTF();
        secret = in.readUTF();
        fullname = in.readUTF();
        accesslist = in.readUTF();
    }
}
