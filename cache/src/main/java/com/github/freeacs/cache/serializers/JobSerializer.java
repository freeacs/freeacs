package com.github.freeacs.cache.serializers;

import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobStatus;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import lombok.NonNull;

public class JobSerializer implements CompactSerializer<Job> {

    @Override
    public @NonNull Job read(@NonNull CompactReader reader) {
        var job = new Job();
        job.setId(reader.readInt32("id"));
        job.setName(reader.readString("name"));
        job.setGroup(reader.readCompact("group"));
        job.setDescription(reader.readString("description"));
        job.setFlags(reader.readCompact("flags"));
        job.setFile(reader.readCompact("file"));
        job.setSRules(reader.readString("sRules"));
        job.setStopRules(job.getSRules()); // populate stopRules list with sRules
        job.setStatus(JobStatus.valueOf(reader.readString("status")));
        job.setRepeatCount(reader.readInt32("repeatCount"));
        job.setRepeatInterval(reader.readInt32("repeatInterval"));
        job.setUnconfirmedTimeout(reader.readInt32("unconfirmedTimeout"));
        job.setConfirmedFailed(reader.readInt32("confirmedFailed"));
        job.setCompletedHadFailures(reader.readInt32("completedHadFailures"));
        job.setCompletedNoFailures(reader.readInt32("completedNoFailures"));
        return job;
    }

    @Override
    public void write(@NonNull CompactWriter writer, @NonNull Job object) {
        writer.writeInt32("id", object.getId());
        writer.writeString("name", object.getName());
        writer.writeCompact("group", object.getGroup());
        writer.writeString("description", object.getDescription());
        writer.writeCompact("flags", object.getFlags());
        writer.writeCompact("file", object.getFile());
        writer.writeString("sRules", object.getSRules());
        writer.writeString("status", object.getStatus().name());
        writer.writeInt32("repeatCount", object.getRepeatCount());
        writer.writeInt32("repeatInterval", object.getRepeatInterval());
        writer.writeInt32("unconfirmedTimeout", object.getUnconfirmedTimeout());
        writer.writeInt32("confirmedFailed", object.getConfirmedFailed());
        writer.writeInt32("completedHadFailures", object.getCompletedHadFailures());
        writer.writeInt32("completedNoFailures", object.getCompletedNoFailures());
    }

    @Override
    public @NonNull Class<Job> getCompactClass() {
        return Job.class;
    }

    @Override
    public @NonNull String getTypeName() {
        return "job";
    }
}
