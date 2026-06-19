package com.asg.payroll.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ActionType {
    ISCREATED,
    ISUPDATED,
    NOCHANGE,
    ISDELETED;

    @JsonCreator
    public static ActionType fromValue(String value) {

        if (value == null) {
            return null;
        }

        return switch (value.toUpperCase()) {
            case "NOCHANGE" -> NOCHANGE;
            case "ISCREATED" -> ISCREATED;
            case "ISDELETED" -> ISDELETED;
            case "ISUPDATED" -> ISUPDATED;
            default -> throw new IllegalArgumentException(
                    "Invalid ActionType: " + value +
                            ". Valid values are: NOCHANGE, ISCREATED, ISDELETED, ISUPDATED"
            );
        };
    }
}
