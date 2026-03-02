package com.example.library.domain;

public class LoanRecord {
    private String loanId;
    private String isbn;
    private String readerId;
    private String borrowedAt;
    private String dueDate;
    private String returnedAt;
    private int overdueDays;
    private int fineAmount;
    private String status;

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public String getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(String borrowedAt) { this.borrowedAt = borrowedAt; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getReturnedAt() { return returnedAt; }
    public void setReturnedAt(String returnedAt) { this.returnedAt = returnedAt; }
    public int getOverdueDays() { return overdueDays; }
    public void setOverdueDays(int overdueDays) { this.overdueDays = overdueDays; }
    public int getFineAmount() { return fineAmount; }
    public void setFineAmount(int fineAmount) { this.fineAmount = fineAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
