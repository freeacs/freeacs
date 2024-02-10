package com.github.freeacs.cache.serializers;

import com.github.freeacs.dbi.File;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class FileSerializer implements CompactSerializer<File> {
    @Override
    public File read(CompactReader reader) {
        var id = reader.readInt32("id");
        var name = reader.readString("name");
        var file = new File();
        file.setId(id);
        file.setName(name);
        return file;
    }

    @Override
    public void write(CompactWriter writer, File employee) {
        writer.writeInt32("id", employee.getId());
        writer.writeString("name", employee.getName());
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