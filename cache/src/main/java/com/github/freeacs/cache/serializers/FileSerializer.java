package com.github.freeacs.cache.serializers;

import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.User;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactWriter;

import java.util.Optional;

public class FileSerializer implements CompactSerializer<File> {
    @Override
    public File read(CompactReader reader) {
        var file = new File();
        file.setId(reader.readInt32("id"));
        file.setName(reader.readString("name"));
        file.setTargetName(reader.readString("targetName"));
        file.setDescription(reader.readString("description"));
        file.setType(FileType.valueOf(reader.readString("type")));
        file.setVersion(reader.readString("version"));
        file.setTimestamp(new java.util.Date(reader.readInt64("timestamp")));
        file.setLength(reader.readInt32("length"));
        Optional.ofNullable(reader.readNullableInt32("unittypeId")).ifPresent(unittypeId -> file.setUnittype(new Unittype().withId(unittypeId)));
        Optional.ofNullable(reader.readNullableInt32("ownerId")).ifPresent(ownerId -> file.setOwner(new User().withId(ownerId)));
        return file;
    }

    @Override
    public void write(CompactWriter writer, File file) {
        writer.writeInt32("id", file.getId());
        writer.writeString("name", file.getName());
        writer.writeString("targetName", file.getTargetName());
        writer.writeString("description", file.getDescription());
        writer.writeString("type", file.getType().name());
        writer.writeString("version", file.getVersion());
        writer.writeInt64("timestamp", file.getTimestamp().getTime());
        writer.writeInt32("length", file.getLength());
        writer.writeNullableInt32("unittypeId", Optional.ofNullable(file.getUnittype()).map(Unittype::getId).orElse(null));
        writer.writeNullableInt32("ownerId", Optional.ofNullable(file.getOwner()).map(User::getId).orElse(null));
    }

    @Override
    public Class<File> getCompactClass() {
        return File.class;
    }

    @Override
    public String getTypeName() {
        return "file";
    }
}