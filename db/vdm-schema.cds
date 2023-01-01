namespace capper.db;

using {
    cuid,
    managed
} from '@sap/cds/common';

@assert.unique: {name: [name]}
entity Thread : cuid, managed {
    name        : String not null @assert.unique;

    @assert.range
    @readonly
    status      : Integer enum {
        inPreparation = 1; // Thread is created
        prepared      = 2; // Thread is saved, but does not contain any items
        inProgress    = 3; // Thread contains items
    };
    notes       : Composition of many Note
                      on notes.thread = $self;
    attachments : Composition of many Attachment
                      on attachments.thread = $self;
}

entity Note : cuid, managed {
    content               : String not null;

    @assert.range
    @readonly
    synchronizationStatus : Integer enum {
        underInitialSynchronization = 1; // initial synchronization stated
        underUpdateSynchronization  = 2; // update synchronization stated
        synchronized                = 3; // synchronization finished succesfully
        failed                      = 4; // synchronization was not finished
        underDeleteSynchronization  = 5; // delete synchronization stated
    };

    @readonly
    synchronizationId     : String;
    thread                : Association to one Thread not null;
}

entity Attachment : cuid, managed {

    name              : String;
    synchronizationId : String;
    content           : LargeBinary @Core.MediaType  : contentType;
    contentType       : String      @Core.IsMediaType: true;
    thread            : Association to one Thread not null;

}
