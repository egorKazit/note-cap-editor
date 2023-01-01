package com.yk.web.vdm;

import com.yk.common.utils.JsonConverter;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class ThreadItem {

    private final UUID ID;
    private final UUID parentUuid;
    private final String name;
    private final String content;
    private final Instant lastChangeOn;
    private final Type type;

    private ThreadItem(UUID ID, UUID parentUuid, String name, String content, Instant lastChangeOn, Type type) {
        this.ID = ID;
        this.parentUuid = parentUuid;
        this.name = name;
        this.content = content;
        this.lastChangeOn = lastChangeOn;
        this.type = type;
    }

    @Override
    public String toString() {
        return JsonConverter.InstanceHolder.instance.getInstance().convertToJson(this);
    }


    public enum Type {
        NOTE,
        ATTACHMENT;
    }

}
