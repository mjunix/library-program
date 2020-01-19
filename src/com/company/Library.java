package com.company;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Library {
    private static final String BOOK_DATA_FILE = "books.ser";
    private static final String USER_DATA_FILE = "users.ser";

    private List<Book> availableBooks;
    private List<User> users;

    private User currentUser;

    private Scanner scanner = new Scanner(System.in);

    public Library() {
        if (!Files.exists(Path.of(BOOK_DATA_FILE))) {
            createBookDataFile();
        }

        availableBooks = (List<Book>) FileUtility.loadObject(BOOK_DATA_FILE);

        if (!Files.exists(Path.of(USER_DATA_FILE))) {
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
        while (true) {
            System.out.print("Enter username (or empty string to quit): ");
            String username = scanner.nextLine();

            if (username.isBlank()) {
                // user wants to quit
                break;
            } else if (loggedInSuccessFully(username)) {
                // logged in successfully
                showMainMenu();
            } else {
                System.out.println("ERROR! Wrong username. Try again!");
            }
        }
    }

    private boolean loggedInSuccessFully(String username) {
        return (currentUser = getUserFromUsername(username)) != null;
    }

    private void showMainMenu() {
        while (true) {
            System.out.println();
            System.out.println("Main menu");
            System.out.println("1. Show all books");
            System.out.println("2. Show book details");
            System.out.println("3. Borrow book");
            System.out.println("4. Search book");
            System.out.println("5. Show my borrowed books");
            System.out.println("6. Return book");
            System.out.println("7. Show available books");
            System.out.println("8. Show all borrowed books          (Librarian only)");
            System.out.println("9. Add new book                     (Librarian only)");
            System.out.println("10. Remove book                     (Librarian only)");
            System.out.println("11. Show all users                  (Librarian only)");
            System.out.println("12. Search for user                 (Librarian only)");
            System.out.println("13. Show all books borrowed by user (Librarian only)");
            System.out.println("14. Sort books");
            System.out.println("0. Logout");

            int choice = getIntegerFromUser("Enter option: ");

            switch (choice) {
                case 1:
                    showAllBooks();
                    break;
                case 2:
                    showBookDetails();
                    break;
                case 3:
                    borrowBook();
                    break;
                case 4:
                    searchBook();
                    break;
                case 5:
                    showBorrowedBooks();
                    break;
                case 6:
                    returnBook();
                    break;
                case 7:
                    showAvailableBooks();
                    break;
                case 8:
                    showAllBorrowedBooks();
                    break;
                case 9:
                    addNewBook();
                    break;
                case 10:
                    removeBook();
                    break;
                case 11:
                    showAllUsers();
                    break;
                case 12:
                    searchForUser();
                    break;
                case 13:
                    showAllBooksBorrowedByUser();
                    break;
                case 14:
                    sortBooks();
                    break;
                case 0: // logout
                    saveProgramStateToFiles();
                    return;
                default:
                    System.out.println("ERROR: Invalid choice! Try again!");
                    break;
            }
        }
    }

    private void sortBooks() {
        while (true) {
            System.out.println("Sort by:");
            System.out.println("1. Title");
            System.out.println("2. Author");
            System.out.println("0. Exit");

            int value = getIntegerFromUser("Sort by: ");

            if(value == 0) {
                return;
            }

            if(value < 0 || value > 2) {
                System.out.println("ERROR: Invalid value! Try again!");
                continue;
            }

            Comparator<? super Book> comparator = null;

            if(value == 1) { // sort by title
                comparator = new Comparator<Book>() {
                    @Override
                    public int compare(Book b1, Book b2) {
                        return b1.getTitle().compareToIgnoreCase(b2.getTitle());
                    }
                };
            }
            else if(value == 2) { // sort by author
                comparator = new Comparator<Book>() {
                    @Override
                    public int compare(Book b1, Book b2) {
                        return b1.getAuthor().compareToIgnoreCase(b2.getAuthor());
                    }
                };
            }

            List<Book> books = getAllBooks();
            books.sort(comparator);

            for (Book book : books) {
                System.out.println(book);
            }

            break;
        }
    }

    private void showAllBooksBorrowedByUser() {
        if (!(currentUser instanceof Librarian)) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        while (true) {
            for (int i = 0; i < users.size(); i++) {
                System.out.println((i + 1) + ". " + users.get(i).getName());
            }
            System.out.println("0. Exit");

            int index = getIntegerFromUser("Enter index of user: ");

            if (index == 0) {
                return;
            }

            index--; // make index zero-based

            if (index < 0 || index >= users.size()) {
                System.out.println("ERROR: Invalid index! Try again!");
                continue;
            }

            for (Book borrowedBook : users.get(index).getBorrowedBooks()) {
                System.out.println(borrowedBook);
            }

            break;
        }
    }

    private void searchForUser() {
        if (!(currentUser instanceof Librarian)) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        System.out.print("Enter search string: ");
        String searchString = scanner.nextLine().toLowerCase();

        for (User user : users) {
            if(user.getName().contains(searchString)) {
                System.out.println(user);
            }
        }
    }

    private void showAllUsers() {
        if (!(currentUser instanceof Librarian)) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        for (User user : users) {
            System.out.println(user);
        }
    }

    private void removeBook() {
        if (!(currentUser instanceof Librarian)) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        while(true) {
            for (int i = 0; i < availableBooks.size(); i++) {
                System.out.println((i + 1) + ". " + availableBooks.get(i));
            }
            System.out.println("0. Exit");

            int index = getIntegerFromUser("Enter index of book to remove: ");

            if(index == 0) {
                return;
            }

            index--; // make index zero-based

            if(index < 0 || index >= availableBooks.size()) {
                System.out.println("ERROR: Invalid index! Try again!");
                continue;
            }

            availableBooks.remove(index);

            break;
        }
    }

    private void addNewBook() {
        if (!(currentUser instanceof Librarian)) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        System.out.print("Enter title: ");
        String title = scanner.nextLine();

        System.out.print("Enter author: ");
        String author = scanner.nextLine();

        System.out.print("Enter description: ");
        String description = scanner.nextLine();

        availableBooks.add(new Book(title, author, description));
    }

    private void showAllBorrowedBooks() {
        if (!(currentUser instanceof Librarian)) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        for (User user : users) {
            for (Book borrowedBook : user.getBorrowedBooks()) {
                System.out.println(borrowedBook);
            }
        }
    }

    private void showAvailableBooks() {
        for (Book book : availableBooks) {
            System.out.println(book);
        }
    }

    private void returnBook() {
        while (true) {
            for (int i = 0; i < currentUser.getBorrowedBooks().size(); i++) {
                System.out.println((i + 1) + ". " + currentUser.getBorrowedBooks().get(i).getTitle());
            }
            System.out.println("0. Exit");

            int index = getIntegerFromUser("Enter index of book to return: ");

            if (index == 0) {
                return; // exit
            }

            index--; // make index zero-based

            if (index < 0 || index >= currentUser.getBorrowedBooks().size()) {
                System.out.println("ERROR: Invalid index. Try again!");
                continue;
            }

            availableBooks.add(currentUser.returnBook(index));
            break;
        }
    }

    private void showBorrowedBooks() {
        for (Book borrowedBook : currentUser.getBorrowedBooks()) {
            System.out.println(borrowedBook);
        }
    }

    private void searchBook() {
        while (true) {
            System.out.println("Search by:");
            System.out.println("1. Title");
            System.out.println("2. Author");
            System.out.println("0. Exit");

            int choice = getIntegerFromUser("Enter choice: ");

            if (choice == 0) {
                return;
            }

            if (choice > 2 || choice < 0) {
                System.out.println("ERROR: Invalid choice! Try again!");
                continue;
            }

            System.out.print("Enter search string: ");
            String searchString = scanner.nextLine().toLowerCase();

            for (Book book : getAllBooks()) {
                if ((choice == 1 && book.getTitle().toLowerCase().contains(searchString)) ||
                        (choice == 2 && book.getAuthor().toLowerCase().contains(searchString))) {
                    System.out.println(book);
                }
            }

            break;
        }
    }

    private void borrowBook() {
        while (true) {
            for (int i = 0; i < availableBooks.size(); i++) {
                System.out.println((i + 1) + ". " + availableBooks.get(i));
            }
            System.out.println("0. Exit");

            int index = getIntegerFromUser("Enter index of book to borrow: ");

            if (index == 0) {
                return; // exit
            }

            index--; // make index zero-based

            if (index < 0 || index >= availableBooks.size()) {
                System.out.println("ERROR: Invalid index! Try again!");
                continue;
            }

            Book bookToBorrow = availableBooks.remove(index);
            currentUser.borrowBook(bookToBorrow);
            break;
        }
    }

    private void showBookDetails() {
        while (true) {
            List<Book> allBooks = getAllBooks();

            for (int i = 0; i < allBooks.size(); i++) {
                Book book = allBooks.get(i);
                System.out.println((i + 1) + ". \"" + book.getTitle() + "\" by " + book.getAuthor());
            }
            System.out.println("0. Exit");

            int choice = getIntegerFromUser("Enter index of book to see its detailed info: ");

            if (choice == 0) {
                return;
            }

            choice--; // make zero-based

            if (choice < 0 || choice >= allBooks.size()) {
                System.out.println("ERROR: Invalid index! Try again!");
                continue;
            }

            Book book = allBooks.get(choice);
            System.out.println("Title: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Description: " + book.getDescription());
            System.out.println("Available: " + availableBooks.contains(book));
            break;
        }
    }

    private void showAllBooks() {
        for (Book book : getAllBooks()) {
            System.out.println(book);
        }
    }

    private List<Book> getAllBooks() {
        List<Book> allBooks = new ArrayList<>(availableBooks);

        for (User user : users) {
            allBooks.addAll(user.getBorrowedBooks());
        }

        return allBooks;
    }

    private int getIntegerFromUser(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid integer. Try again!");
            }
        }
    }

    private User getUserFromUsername(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }

    private void saveProgramStateToFiles() {
        FileUtility.saveObject(BOOK_DATA_FILE, availableBooks);
        FileUtility.saveObject(USER_DATA_FILE, users);
    }

    private void createBookDataFile() {
        List<Book> defaultBooks = new ArrayList<>();
        defaultBooks.add(new Book("Book1", "Author1", "Description1"));
        defaultBooks.add(new Book("Book2", "Author2", "Description2"));
        defaultBooks.add(new Book("Book3", "Author3", "Description3"));
        defaultBooks.add(new Book("Book4", "Author4", "Description4"));
        defaultBooks.add(new Book("Book5", "Author5", "Description5"));

        FileUtility.saveObject(BOOK_DATA_FILE, defaultBooks);
    }

    private void createUserDataFile() {
        List<User> defaultUsers = new ArrayList<>();
        defaultUsers.add(new User("johan"));
        defaultUsers.add(new Librarian("admin"));

        FileUtility.saveObject(USER_DATA_FILE, defaultUsers);
    }
}
