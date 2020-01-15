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

    private User loggedInUser;

    private Scanner scanner = new Scanner(System.in);

    public Library() {
        if(!Files.exists(Path.of(BOOK_DATA_FILE))) {
            createBookDataFile();
        }

        books = readBooksFromFile(BOOK_DATA_FILE);

        if(!Files.exists(Path.of(USER_DATA_FILE))) {
            createUserDataFile();
        }

        users = (List<User>) FileUtility.loadObject(USER_DATA_FILE);
    }

    public void startProgram() {
        System.out.println("Welcome to Library Program");
        System.out.println("Please login...");
        login();
    }

    private void login() {
        do {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            if((loggedInUser = getUserFromUsername(username)) != null) {
                // logged in successfully
                break;
            }
            else {
                System.out.println("ERROR! Wrong username. Try again!");
            }
        } while(true);
    }

    private User getUserFromUsername(String username) {
        for (User user : users) {
            if(user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }


    private List<Book> readBooksFromFile(String filename) {
        List<Book> result = new ArrayList<>();

        List<String> lines = (List<String>) FileUtility.loadText(filename);

        for (String line : lines) {
            String[] tokens = line.split(BOOK_FIELD_SEPARATOR);

            if(tokens.length != 3) {
                continue;
            }

            String title = tokens[0];
            String author = tokens[1];
            String description = tokens[2];

            result.add(new Book(title, author, description));
        }

        return result;
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
