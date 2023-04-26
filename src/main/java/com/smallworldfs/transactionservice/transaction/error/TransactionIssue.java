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
    TRANSACTION_SENDING_IS_LESS_THAN_PAYOUT("Sending amount: {0} is less than payout amount: {1}",
            DefaultIssueType.REQUEST_ERROR),
    CLIENT_EXCEED_LIMIT_OPEN_TRANSACTIONS("Client cannot has more than 5 transactions in progress",
            DefaultIssueType.REQUEST_ERROR),
    CLIENT_EXCEED_LIMIT_TO_SEND_IN_PERIOD("Client cannot has more than 5000$ in a given 30 day period",
            DefaultIssueType.REQUEST_ERROR),
            ;

    private final String messageTemplate;
    private final IssueType type;
}
