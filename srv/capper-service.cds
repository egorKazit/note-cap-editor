using {capper.db as db} from '../db/vdm-schema';

service CapperService @(requires: 'capper') {
    @(restrict: [{
        grant: '*',
        to   : 'capper'
    }])
    entity Thread @(Capabilities: {
        InsertRestrictions.Insertable: true,
        UpdateRestrictions.Updatable : false,
        DeleteRestrictions.Deletable : true,
    })                as projection on db.Thread {
        *,
        @readonly status,
    };

    @(restrict: [{
        grant: '*',
        to   : 'capper'
    }])
    entity Note       as projection on db.Note;

    @(restrict: [{
        grant: '*',
        to   : 'capper'
    }])
    entity Attachment as projection on db.Attachment;

}
