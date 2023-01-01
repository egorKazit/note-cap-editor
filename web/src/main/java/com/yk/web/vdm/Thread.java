package com.yk.web.vdm;

import com.yk.common.utils.JsonConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class Thread {

    @Getter
    private final UUID ID;
    @Getter
    private final String name;

    private final Object[] attachments;
    private final Object[] notes;

    public List<ThreadItem> getItems() {
        List<ThreadItem> threadItems = new ArrayList<>();
        if (attachments != null)
            threadItems.addAll(convertItemToExternalFormat(attachments, ThreadItem.Type.ATTACHMENT)
                    .stream().map(attachment -> attachment.toBuilder().content(attachment.getName()).build()).toList());
        if (notes != null) threadItems.addAll(convertItemToExternalFormat(notes, ThreadItem.Type.NOTE));
        return threadItems;
    }

    @Override
    public String toString() {
        return JsonConverter.InstanceHolder.instance.getInstance().convertToJson(this);
    }

    private List<ThreadItem> convertItemToExternalFormat(Object[] itemArray, ThreadItem.Type type) {
        var items = Arrays.stream(JsonConverter.InstanceHolder.instance.getInstance().convertFromJson(JsonConverter.InstanceHolder.instance.getInstance().convertToJson(itemArray), ThreadItem[].class)).toList();
        return items.stream().map(item -> item.toBuilder().type(type).build()).toList();
    }

}
