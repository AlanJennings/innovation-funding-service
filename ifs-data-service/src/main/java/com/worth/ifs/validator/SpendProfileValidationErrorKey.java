package com.worth.ifs.validator;

/**
 * Enum for listing all Spend Profile related error keys
 *
 * Created by xiaonan.zhang on 21/11/2016.
 */
public enum SpendProfileValidationErrorKey {
    COST_SHOULD_NOT_BE_NULL("validation.spend.profile.cost.should.not.be.null"),
    COST_SHOULD_NOT_BE_FRACTIONAL("validation.spend.profile.cost.should.not.be.fractional"),
    COST_SHOULD_NOT_BE_LESS_THAN_ZERO("validation.spend.profile.cost.should.not.be.less.than.zero"),
    COST_SHOULD_BE_WITHIN_UPPER_LIMIT("validation.spend.profile.cost.should.be.within.upper.limit");

    private String errorKey;

    SpendProfileValidationErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return this.errorKey;
    }
}
