package com.company;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Library {
    private static final String BOOK_DATA_FILE = "books.txt";
    private static final String USER_DATA_FILE = "users.ser";

    private static final String BOOK_FIELD_SEPARATOR = ":::";

    private List<Book> books;
    private List<User> users;
    private Scanner scanner = new Scanner(System.in);

    public Library() {
        if(!Files.exists(Path.of(BOOK_DATA_FILE))) {
            createBookDataFile();
        }

        List<String> bookLines = (List<String>) FileUtility.loadText(BOOK_DATA_FILE);
        books = new ArrayList<>();

        for (String bookLine : bookLines) {
            String[] tokens = bookLine.split(BOOK_FIELD_SEPARATOR);

            if(tokens.length != 3) {
                continue;
            }

            String title = tokens[0];
            String author = tokens[1];
            String description = tokens[2];

            books.add(new Book(title, author, description));
        }

        if(!Files.exists(Path.of(USER_DATA_FILE))) {
            createUserDataFile();
        }

        users = (List<User>) FileUtility.loadObject(USER_DATA_FILE);
    }

    private void createBookDataFile() {
        List<Book> defaultBooks = new ArrayList<>();
        defaultBooks.add(new Book("Book1", "Author1", "Description1"));
        defaultBooks.add(new Book("Book2", "Author2", "Description2"));
        defaultBooks.add(new Book("Book3", "Author3", "Description3"));
        defaultBooks.add(new Book("Book4", "Author4", "Description4"));
        defaultBooks.add(new Book("Book5", "Author5", "Description5"));

        List<String> bookLines = new ArrayList<>();

        for (Book book : defaultBooks) {
            bookLines.add(book.getTitle() + BOOK_FIELD_SEPARATOR + book.getAuthor() + BOOK_FIELD_SEPARATOR + book.getDescription());
        }

        FileUtility.saveText(BOOK_DATA_FILE, bookLines);
    }

    private void createUserDataFile() {
        List<User> defaultUsers = new ArrayList<>();
        defaultUsers.add(new User("johan"));
        defaultUsers.add(new Librarian("admin"));

        FileUtility.saveObject(USER_DATA_FILE, defaultUsers);
    }
}
