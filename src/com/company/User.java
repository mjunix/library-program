package com.company;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String name;
    private List<Book> borrowedBooks = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void borrowBook(Book book) {
        book.setLoanDate(LocalDateTime.now());
        borrowedBooks.add(book);
    }

    public Book returnBook(int index) {
        Book returnedBook = borrowedBooks.remove(index);
        returnedBook.setLoanDate(null);
        return returnedBook;
    }

    public boolean isLibrarian() {
        return this instanceof Librarian;
    }

    public List<Book> getBorrowedBooks() {
        return new ArrayList<>(borrowedBooks);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", borrowedBooks=" + borrowedBooks +
                '}';
    }
}
