package com.example.library.service;

import com.example.library.dao.model.BookRecord;
import com.example.library.dao.model.BorrowCommand;
import com.example.library.dao.model.CreateBookCommand;
import com.example.library.dao.model.LoanRecord;
import com.example.library.dao.model.ReturnCommand;

import java.util.List;

public interface LibraryService {

    List<BookRecord> listBooks();

    BookRecord createBook(CreateBookCommand command);

    LoanRecord borrow(BorrowCommand command);

    LoanRecord returnLoan(ReturnCommand command);
}
