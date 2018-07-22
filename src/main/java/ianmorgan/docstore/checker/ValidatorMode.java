package ianmorgan.docstore.checker;


public enum ValidatorMode {
    Create,   // this will check all mandatory fields exist (same as INSERT in SQL speak)
    Update,   // this only check the supplied fields (same as UPDATE in SQL speak)
    Skip      // no validations - use with care, assumes other business logic will compensate
}
