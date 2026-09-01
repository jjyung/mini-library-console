package com.example.library.service;

import com.example.library.dto.BookDTO;
import com.example.library.dto.LoanDTO;
import com.example.library.dto.LoanReturnDTO;
import com.example.library.dto.PostBooksRequestDTO;
import com.example.library.dto.PostLoansRequestDTO;
import com.example.library.dto.PostLoansReturnsRequestDTO;
import java.util.List;

public interface LibraryService {
    BookDTO createBook(PostBooksRequestDTO request);
    List<BookDTO> listBooks();
    LoanDTO checkoutBook(PostLoansRequestDTO request);
    LoanReturnDTO returnBook(PostLoansReturnsRequestDTO request);
}
