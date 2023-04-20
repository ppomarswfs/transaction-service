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
    ;

    private final String messageTemplate;
    private final IssueType type;
}
