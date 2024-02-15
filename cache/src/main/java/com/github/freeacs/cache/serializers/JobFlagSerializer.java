package com.github.freeacs.cache.serializers;

import com.github.freeacs.dbi.JobFlag;
import com.github.freeacs.dbi.JobType;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;
import lombok.NonNull;

public class JobFlagSerializer implements CompactSerializer<JobFlag> {
    @Override
    public @NonNull JobFlag read(@NonNull CompactReader reader) {
        var jobType = JobType.valueOf(reader.readString("type"));
        var jobServiceWindow = JobFlag.JobServiceWindow.valueOf(reader.readString("serviceWindow"));
        return new JobFlag(jobType, jobServiceWindow);
    }

    @Override
    public void write(@NonNull CompactWriter writer, @NonNull JobFlag object) {
        writer.writeString("type", object.getType().name());
        writer.writeString("serviceWindow", object.getServiceWindow().name());
    }

    @Override
    public @NonNull Class<JobFlag> getCompactClass() {
        return JobFlag.class;
    }

    @Override
    public @NonNull String getTypeName() {
        return "jobFlag";
    }
}
