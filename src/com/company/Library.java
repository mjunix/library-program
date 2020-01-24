package com.company;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Library {
    private static final String BOOK_DATA_FILE = "books.ser";
    private static final String USER_DATA_FILE = "users.ser";

    private List<Book> availableBooks;
    private List<User> users;

    private User currentUser;

    private Scanner scanner = new Scanner(System.in);

    private static final Duration LOAN_DURATION = Duration.ofSeconds(30); // intentionally very short to simplify testing

    public Library() {
        if (!Files.exists(Path.of(BOOK_DATA_FILE))) {
            createDefaultBookDataFile();
        }

        availableBooks = (List<Book>) FileUtility.loadObject(BOOK_DATA_FILE);

        if (!Files.exists(Path.of(USER_DATA_FILE))) {
            createDefaultUserDataFile();
        }

        users = (List<User>) FileUtility.loadObject(USER_DATA_FILE);
    }

    public void startProgram() {
        System.out.println("Welcome to Library Program");
        System.out.println("NOTE: Program state is only saved when logging out!");
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
                showLoginMessages();
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
            System.out.println("4. Return book");
            System.out.println("5. Show my borrowed books");
            System.out.println("6. Search book");
            System.out.println("7. Show available books");
            System.out.println("8. Sort books");

            if(currentUser.isLibrarian()) {
                System.out.println("9. Show all borrowed books          (Librarian only)");
                System.out.println("10. Add new book                    (Librarian only)");
                System.out.println("11. Remove book                     (Librarian only)");
                System.out.println("12. Show all users                  (Librarian only)");
                System.out.println("13. Search for user                 (Librarian only)");
                System.out.println("14. Show books borrowed by a user   (Librarian only)");
            }

            System.out.println("0. Logout");

            int choice = getIntegerFromUser("Enter option: ");

            System.out.println();

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
                    returnBook();
                    break;
                case 5:
                    showBorrowedBooks();
                    break;
                case 6:
                    searchBook();
                    break;
                case 7:
                    showAvailableBooks();
                    break;
                case 8:
                    sortBooks();
                    break;
                case 9:
                    showAllBorrowedBooks();
                    break;
                case 10:
                    addNewBook();
                    break;
                case 11:
                    removeBook();
                    break;
                case 12:
                    showAllUsers();
                    break;
                case 13:
                    searchForUser();
                    break;
                case 14:
                    showBooksBorrowedByAUser();
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

            List<Book> books = getAllBooks();

            if(value == 1) { // sort by title
                books.sort((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
            }
            else if(value == 2) { // sort by author
                books.sort((b1, b2) -> b1.getAuthor().compareToIgnoreCase(b2.getAuthor()));
            }

            printBookList(books);

            break;
        }
    }

    private void showBooksBorrowedByAUser() {
        if (!currentUser.isLibrarian()) {
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

            User user = users.get(index);

            if(user.getBorrowedBooks().isEmpty()) {
                System.out.println("This user has not borrowed any books!");
                return;
            }

            for (Book borrowedBook : user.getBorrowedBooks()) {
                System.out.println(borrowedBook.getTitle());
            }

            break;
        }
    }

    private void searchForUser() {
        if (!currentUser.isLibrarian()) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        System.out.print("Enter search string: ");
        String searchString = scanner.nextLine().toLowerCase();

        boolean matchFound = false;

        for (User user : users) {
            if(user.getName().toLowerCase().contains(searchString)) {
                System.out.println(user.getName());
                matchFound = true;
            }
        }

        if(!matchFound) {
            System.out.println("No user with that name exist!");
        }
    }

    private void showAllUsers() {
        if (!currentUser.isLibrarian()) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        for (User user : users) {
            System.out.println(user.getName() + (user.isLibrarian() ? " (librarian)" : ""));
        }
    }

    private void removeBook() {
        if (!currentUser.isLibrarian()) {
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
        if (!currentUser.isLibrarian()) {
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
        if (!currentUser.isLibrarian()) {
            System.out.println("ERROR: This action can only be performed by librarians!");
            return;
        }

        for (User user : users) {
            for (Book borrowedBook : user.getBorrowedBooks()) {
                System.out.printf("\"%s\" borrowed by %s\n", borrowedBook.getTitle(), user.getName());
            }
        }
    }

    private void showAvailableBooks() {
        printBookList(availableBooks);
    }

    private void returnBook() {
        if(currentUser.getBorrowedBooks().isEmpty()) {
            System.out.println("You have not borrowed any books!");
            return;
        }

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
        if(currentUser.getBorrowedBooks().isEmpty()) {
            System.out.println("You have not borrowed any books!");
            return;
        }

        for (Book borrowedBook : currentUser.getBorrowedBooks()) {
            System.out.println("Title: " + borrowedBook.getTitle());
            System.out.println("Author: " + borrowedBook.getAuthor());
            System.out.println("Loan date: " + borrowedBook.getLoanDate());
            LocalDateTime returnDate = borrowedBook.getLoanDate().plus(LOAN_DURATION);
            System.out.println("Return date: " + returnDate +
                    (isOverdue(borrowedBook) ? " (Is overdue!)" : " (To be returned in: " + Duration.between(borrowedBook.getLoanDate(), returnDate) + ")"));
            System.out.println();
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

            System.out.println("\nResult:");
            boolean matchFound = false;

            for (Book book : getAllBooks()) {
                if ((choice == 1 && book.getTitle().toLowerCase().contains(searchString)) ||
                        (choice == 2 && book.getAuthor().toLowerCase().contains(searchString))) {
                    System.out.printf("\"%s\" by %s\n", book.getTitle(), book.getAuthor());
                    matchFound = true;
                }
            }

            if(!matchFound) {
                System.out.println("No results matched your criteria!");
            }

            break;
        }
    }

    private void borrowBook() {
        if(availableBooks.isEmpty()) {
            System.out.println("There are no books in the library!");
            return;
        }

        while (true) {
            for (int i = 0; i < availableBooks.size(); i++) {
                Book book = availableBooks.get(i);
                System.out.printf("%d. \"%s\" by %s\n", (i + 1), book.getTitle(), book.getAuthor());
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

            System.out.printf("You borrowed \"%s\" by %s\n", bookToBorrow.getTitle(), bookToBorrow.getAuthor());

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
            System.out.println("\nTitle: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Description: " + book.getDescription());
            System.out.println("Available: " + availableBooks.contains(book));
            break;
        }
    }

    private void showAllBooks() {
        printBookList(getAllBooks());
    }

    private void printBookList(List<Book> books) {
        for (Book book : books) {
            System.out.printf("\"%s\" by %s\n", book.getTitle(), book.getAuthor());
        }
    }

    private List<Book> getAllBooks() {
        List<Book> allBooks = new ArrayList<>(availableBooks);

        for (User user : users) {
            allBooks.addAll(user.getBorrowedBooks());
        }

        return allBooks;
    }

    private void showLoginMessages() {
        for (Book borrowedBook : currentUser.getBorrowedBooks()) {
            if(isOverdue(borrowedBook)) {
                System.out.println("MESSAGE: \"" + borrowedBook.getTitle() + "\" is overdue!");
            }
        }
    }

    private boolean isOverdue(Book book) {
        return book.getLoanDate() != null && LocalDateTime.now().isAfter(book.getLoanDate().plus(LOAN_DURATION));
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

    private void createDefaultBookDataFile() {
        List<Book> defaultBooks = new ArrayList<>();
        defaultBooks.add(new Book("Harry Potter and the Philosopher’s Stone", "Rowling, J.K.", "Bla bla bla..."));
        defaultBooks.add(new Book("A Confederacy of Dunces", "Toole, John Kennedy", "Bla bla bla..."));
        defaultBooks.add(new Book("The Lord of the Rings", "Tolkien, J. R. R.", "Bla bla bla..."));
        defaultBooks.add(new Book("Effective Java", "Bloch, Joshua", "Bla bla bla..."));
        defaultBooks.add(new Book("Nineteen Eighty Four", "Orwell, George", "Bla bla bla..."));

        FileUtility.saveObject(BOOK_DATA_FILE, defaultBooks);
    }

    private void createDefaultUserDataFile() {
        List<User> defaultUsers = new ArrayList<>();
        defaultUsers.add(new User("johan"));
        defaultUsers.add(new User("peter"));
        defaultUsers.add(new User("kalle"));
        defaultUsers.add(new Librarian("admin"));

        FileUtility.saveObject(USER_DATA_FILE, defaultUsers);
    }
}
