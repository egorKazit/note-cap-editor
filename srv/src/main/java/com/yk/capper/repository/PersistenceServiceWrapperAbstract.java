package com.yk.capper.repository;

import com.sap.cds.CdsData;
import com.sap.cds.ql.*;
import com.sap.cds.ql.cqn.CqnComparisonPredicate.Operator;
import com.sap.cds.ql.cqn.CqnPredicate;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public abstract class PersistenceServiceWrapperAbstract<S extends StructuredType<S>, T>
        implements PersistenceServiceWrapper<T> {

    private static final String ID = "ID";

    private final PersistenceService persistenceService;
    private final Class<T> typeOfT;
    private final Class<S> typeOfS;

    public PersistenceServiceWrapperAbstract(PersistenceService persistenceService, Class<T> typeOfT, Class<S> typeOfS) {
        this.persistenceService = persistenceService;
        this.typeOfT = typeOfT;
        this.typeOfS = typeOfS;
    }

    @Override
    public List<T> readAllEntries() {
        CqnSelect headerCqnSelect = Select.from(getSourceType());
        return persistenceService.run(headerCqnSelect).listOf(getTargetType());
    }

    @Override
    public  List<T> readEntries(Map<String, Object> conditions) {
        if (conditions.isEmpty())
            return readAllEntries();
        CqnPredicate filter = conditions.entrySet().stream()
                .map(entry -> CQL.comparison(CQL.get(entry.getKey()), Operator.EQ, CQL.val(entry.getValue())))
                .collect(CQL.withAnd());
        CqnSelect headerCqnSelect = Select.from(getSourceType()).where(filter);
        return persistenceService.run(headerCqnSelect).listOf(getTargetType());
    }

    @Override
    public List<T> readEntries(CqnSelect cqnSelect) {
        return persistenceService.run(cqnSelect).listOf(getTargetType());
    }

    @Override
    public List<T> readEntries(List<String> uuids) {
        return persistenceService.run(Select.from(getSourceType()).where(entry -> entry.get(ID).in(uuids)))
                .listOf(getTargetType());
    }

    @Override
    public T readEntry(String uuid) {
        return persistenceService.run(Select.from(getSourceType()).where(entry -> entry.get(ID).eq(uuid)))
                .first(getTargetType()).orElse(null);
    }

    @Override
    public boolean create(List<? extends CdsData> cdsData) {
        return persistenceService.run(Insert.into(getSourceType()).entries(cdsData))
                .batchCount() > 0;
    }

    @Override
    public boolean update(List<? extends CdsData> cdsData) {
        return persistenceService.run(Update.entity(getSourceType()).entries(cdsData))
                .batchCount() > 0;
    }

    @Override
    public boolean delete(List<String> uuids) {
        return persistenceService.run(Delete.from(getSourceType()).where(entry -> entry.get(ID).in(uuids)))
                .batchCount() > 0;
    }

    private Class<S> getSourceType() {
        return typeOfS;
    }

    private Class<T> getTargetType() {
        return typeOfT;
    }

}
