package com.yk.capper.repository;

import com.sap.cds.CdsData;
import com.sap.cds.ql.cqn.CqnSelect;

import java.util.List;
import java.util.Map;

public interface PersistenceServiceWrapper<T> {
    List<T> readAllEntries();

    List<T> readEntries(Map<String, Object> conditions);

    List<T> readEntries(CqnSelect cqnSelect);

    List<T> readEntries(List<String> uuids);

    T readEntry(String uuid);

    boolean create(List<? extends CdsData> cdsData);

    boolean update(List<? extends CdsData> cdsData);

    boolean delete(List<String> uuids);
}
