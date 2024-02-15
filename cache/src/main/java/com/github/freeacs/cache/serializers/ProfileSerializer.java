package com.github.freeacs.cache.serializers;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import lombok.NonNull;

import java.util.Objects;

public class ProfileSerializer implements CompactSerializer<Profile> {
    @Override
    public @NonNull Profile read(CompactReader reader) {
        var unittype = new Unittype();
        var unittypeName = reader.readString("unittypeName");
        unittype.setName(Objects.requireNonNull(unittypeName, "unittype name cannot be null"));
        unittype.setId(reader.readInt32("unittypeId"));
        var profiile = new Profile(reader.readString("name"), unittype);
        profiile.setId(reader.readInt32("id"));
        return profiile;
    }

    @Override
    public void write(CompactWriter writer, Profile profile) {
        writer.writeInt32("id", profile.getId());
        writer.writeString("name", profile.getName());
        writer.writeString("unittypeName", profile.getUnittype().getName());
        writer.writeInt32("unittypeId", profile.getUnittype().getId());
    }

    @Override
    public @NonNull Class<Profile> getCompactClass() {
        return Profile.class;
    }

    @Override
    public @NonNull String getTypeName() {
        return "profile";
    }
}
