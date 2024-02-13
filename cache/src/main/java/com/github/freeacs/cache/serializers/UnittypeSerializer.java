package com.github.freeacs.cache.serializers;

import com.github.freeacs.dbi.Unittype;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import lombok.NonNull;

import java.util.Objects;

public class UnittypeSerializer implements CompactSerializer<Unittype> {
    @Override
    public @NonNull Unittype read(CompactReader reader) {
        var unittype = new Unittype();
        unittype.setId(reader.readInt32("id"));
        unittype.setName(Objects.requireNonNull(reader.readString("name"), "unittype mame cannot be null"));
        unittype.setProtocol(Unittype.ProvisioningProtocol.toEnum(reader.readString("protocol")));
        unittype.setDescription(reader.readString("description"));
        unittype.setVendor(reader.readString("vendor"));
        return unittype;
    }

    @Override
    public void write(CompactWriter writer, Unittype unittype) {
        writer.writeInt32("id", unittype.getId());
        writer.writeString("name", unittype.getName());
        writer.writeString("protocol", unittype.getProtocol().name());
        writer.writeString("description", unittype.getDescription());
        writer.writeString("vendor", unittype.getVendor());
    }

    @Override
    public @NonNull Class<Unittype> getCompactClass() {
        return Unittype.class;
    }

    @Override
    public @NonNull String getTypeName() {
        return "unittype";
    }
}
