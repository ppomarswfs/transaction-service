package com.smallworldfs.transactionservice.transaction.error;

import com.smallworldfs.error.issue.DefaultIssueType;
import com.smallworldfs.error.issue.Issue;
import com.smallworldfs.error.issue.IssueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionIssue implements Issue {

    TRANSACTION_NOT_FOUND("Transaction with id {0} could be not found.", DefaultIssueType.NOT_FOUND),
    TRANSACTION_EXCEEDS_SENDING_LIMIT("Sending principal amount: {0} exceeds limit in single operation.",
            DefaultIssueType.REQUEST_ERROR),
    MIN_FEE_IS_TOO_SMALL(
            "The difference between sending ({0}) and payout ({1}) must be at least {2}$",
            DefaultIssueType.REQUEST_ERROR),
    CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS("Client cannot has more than {0} transactions in progress",
            DefaultIssueType.REQUEST_ERROR),
    CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD(
            "Client cannot has more than {0}$ in a given {1} days period. Now sender would has {2}$",
            DefaultIssueType.REQUEST_ERROR),
    TRANSACTION_WAS_PAYOUT("Transaction with id {0} was already payout.", DefaultIssueType.REQUEST_ERROR),
    // TODO como hacer para que devuelva el 422? Que
    ;

    private final String messageTemplate;
    private final IssueType type;
}
