import java.util.*;
import java.io.*;

public class Library {
    ArrayList<Book> books = new ArrayList<>();
    ArrayList<Member> members = new ArrayList<>();
    public static final String HELP_STRING = "EXIT ends the library process\nCOMMANDS outputs this help string\n\nLIST ALL [LONG] outputs " +
    "either the short or long string for all books\nLIST AVAILABLE [LONG] outputs either the short of long string for " + 
    "all available books\nNUMBER COPIES outputs the number of copies of each book\nLIST GENRES outputs the name of every genre in " +
    "the system\nLIST AUTHORS outputs the name of every author in the system\n\nGENRE <genre> outputs the short string of every " +
    "book with the specified genre\nAUTHOR <author> outputs the short string of every book by the specified author\n\nBOOK <serialNumber> [LONG] outputs " +
    "either the short or long string for the specified book\nBOOK HISTORY <serialNumber> outputs the rental history of the " +
    "specified book\n\nMEMBER <memberNumber> outputs the information of the specified member\nMEMBER BOOKS <memberNumber> outputs the books " +
    "currently rented by the specified member\nMEMBER HISTORY <memberNumber> outputs the rental history of the " +
    "specified member\n\nRENT <memberNumber> <serialNumber> loans out the specified book to the given member\nRELINQUISH <memberNumber> <serialNumber> " +
    "returns the specified book from the member\nRELINQUISH ALL <memberNumber> returns all books rented by the specified member\n\nADD MEMBER <name> adds " +
    "a member to the system\nADD BOOK <filename> <serialNumber> adds a book to the system\n\nADD COLLECTION <filename> adds a " +
    "collection of books to the system\nSAVE COLLECTION <filename> saves the system to a csv file\n\nCOMMON <memberNumber1> <memberNumber2> ... outputs " +
    "the common books in members\' history";

    public String argbuilder(int idx, String[] line) {
        String retval = "";
        for (int i = idx; i < line.length; i++) {
            if (i == line.length - 1) {
                retval += line[i];
            } else {
                retval += line[i] + " ";
            }
        }
        return retval;
    }

    public void addBook(String filename, String serialNumber) {
        for (Book book : books) {
            if (book.getSerialNumber().equals(serialNumber)) {
                System.out.println("Book already exists in system.");
                return; 
            }
        }
        Book value = Book.readBook(filename, serialNumber);
        if (!(value == null)) { 
            books.add(value); 
            System.out.printf("Successfully added: %s.\n", value.shortString()); 
        }
    }

    public void addCollection(String filename) {
        if (filename == null || Book.readBookCollection(filename) == null) { 
            System.out.println("No such collection."); 
            return; 
        } else {
            if (books.size() == 0) {
                for (Book book : Book.readBookCollection(filename)) {
                    books.add(book);
                }
                System.out.printf("%d books successfully added.\n", books.size());
                return; 
            } else if (books.size() > 0) {
                int count = 0;
                for (Book book : Book.readBookCollection(filename)) {
                    boolean canadd = true; 
                    // check if the book is a duplicate or not 
                    for (Book b : books) {
                        // is duplicate 
                        if ( b.getSerialNumber().equals(book.getSerialNumber()) ) {
                            canadd = false;
                        }
                    }
                    if (canadd) { books.add(book); count += 1; }
                }
                if (count <= 0) { 
                    System.out.println("No books have been added to the system."); 
                } else { 
                    System.out.printf("%d books successfully added.\n", count); 
                }
            }
        }
    }
    public void addMember(String name) {
        if (members.size() == 0) {
            members.add(new Member(name, "100000"));
            System.out.println("Success.");
        } else {
            Integer memnum = Integer.parseInt(members.get(members.size() - 1).getMemberNumber());
            memnum += 1;
            members.add(new Member(name, String.valueOf(memnum)));
            System.out.println("Success.");
        }
    }
    public void bookHistory(String serialNumber) {
        if (serialNumber == null || books.size() == 0) { 
            System.out.println("No such book in system."); 
        }  else {
            // does book exist in the collection?
            for (Book bo : books) {
                if (bo.getSerialNumber().equals(serialNumber)) { // book exists
                    if (bo.renterHistory().size() == 0) { 
                        System.out.println("No rental history."); 
                        return; 
                    }
                    // if this runs, then book has a rental history
                    for (Member previous : bo.renterHistory()) { 
                        System.out.printf("%s\n", previous.getMemberNumber()); 
                    }
                    return;
                }
            }
            // if this runs, then no matching book was found in the Library books
            System.out.println("No such book in system.");
        }
    }

    public void getAllBooks(boolean fullString) {
        // no books
        if (books.size() == 0) { System.out.println("No books in system."); }

        for (int i = 0; i < books.size(); i++) {

            if (fullString) {
                if (i == books.size() - 1) { 
                    System.out.println(books.get(i).longString());
                } else { 
                    System.out.println(books.get(i).longString() + "\n"); 
                }
            } else { 
                System.out.println(books.get(i).shortString()); 
            }
        }
    }

    public void getAvailableBooks(boolean fullString) {
        if (books.size() == 0) { System.out.println("No books in system."); return; }
        // count available books 
        int count = 0;
        if (fullString) {
            for (int i = 0; i < books.size(); i++) {
                if (!(books.get(i) == null) && !(books.get(i).isRented())) {
                    ++count;
                    if (i == books.size() - 1) { 
                        System.out.println(books.get(i).longString()); 
                    } else { 
                        System.out.println(books.get(i).longString() + "\n"); 
                    }
                }
            }
        }

        else {
            for (Book book : books) {
                if (!(book == null) && !(book.isRented())) {
                    ++count;
                    System.out.println(book.shortString());
                }
            }
        }
        
        if (count <= 0) { System.out.println("No books available."); }
    }

    public void getCopies() {
        // no books at all 
        if (books.size() == 0) { 
            System.out.println("No books in system."); 
            return; 
        }
        // check if books available 
        int count = 0;
        for (Book book : books) {
            if ( !(book.isRented()) ) { count++; }
        }
        if (count == 0) { 
            System.out.println("No books in system."); 
            return; 
        }
        // sort books 
        // tree-map maps titles to the respective short string. use tree-map since it's a d/struct already in sorted order
        // key = titles, value = short string. 
        TreeMap<String, String> titleToShort = new TreeMap<String, String>();
        // map shortstring to book count 
        HashMap<String, Integer> hmap = new HashMap<String, Integer>();
        // count books 
        Integer ct;
        for (Book book : books) {
            ct = 0;
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).shortString().equals(book.shortString())) { ct++; }
            }
            if ( !(hmap.containsKey(book.shortString())) ) {
                hmap.put(book.shortString(), ct);
                titleToShort.put(book.getTitle(), book.shortString());
            }
        }
        for (String str : titleToShort.keySet()) {
            System.out.println(titleToShort.get(str) + ": " + hmap.get(titleToShort.get(str)));
        }
    }

    public void getGenres() {
        if (books.size() == 0) { System.out.println("No books in system."); return; }
        
        ArrayList<String> output = new ArrayList<>();

        for (Book b : books) {
            if (!(b == null)) {
                if ( !(output.contains(b.getGenre())) ) { output.add(b.getGenre()); }
            }
        }
        // sort output genre collection
        Collections.sort(output);
        for (String s : output) { 
            System.out.println(s); 
        }
    }

    public void getAuthors() {
        if (books.size() == 0) { System.out.println("No books in system."); return; }
        ArrayList<String> output = new ArrayList<>();
        for (Book b : books) {
            if (!(b == null)) {
                if (!(output.contains(b.getAuthor()))) { output.add(b.getAuthor()); }
            }
        }
        // sort the output author collection 
        Collections.sort(output);
        for (String s : output) { System.out.println(s); }
    }
    public void getBook(String serialNumber, boolean fullString) {
        if (serialNumber == null) { 
            System.out.println("No such book in system."); 
        } else if (books.size() == 0) { 
            System.out.println("No books in system."); 
        } else {
            for (Book book : books) {
                // book can be printed 
                if ( book.getSerialNumber().equals(serialNumber) ) {
                    if (fullString) {
                        System.out.println(book.longString());
                        return;
                    }
                    else if (!(fullString)) {
                        System.out.println(book.shortString());
                        return;
                    }
                }
            }
            System.out.println("No such book in system.");
        }
    }

    // print short string of books with author spec'd, and sort by serial number 
    public void getBooksByAuthor(String author) {
        if (books.size() == 0) { 
            System.out.println("No books in system."); 
            return; 
        }
        // numbers is like a "pivoting" collection of serial numbers
        // sort this collection, and then match it to the required books stored 
        // in relevantBooks arraylist  - this will be obtained by calling book.filterAuthor
        List<Book> relevantBooks = Book.filterAuthor(books, author);
        ArrayList<String> numbers = new ArrayList<>();
        if (relevantBooks == null) { 
            System.out.printf("No books by %s.\n", author); 
        } else if (relevantBooks.size() == 0) { 
            System.out.printf("No books by %s.\n", author); 
        } else {
            // get serial nums of relevant books 
            for (Book b : relevantBooks) { numbers.add(b.getSerialNumber()); }
            // sort serial nums 
            Collections.sort(numbers);
            for (String s : numbers) {
                for (Book b : relevantBooks) {
                    if (b.getSerialNumber().equals(s)) { 
                        System.out.println(b.shortString()); 
                    }
                }
            }
        }
    }

    public void getBooksByGenre(String genre) {
        if (books.size() == 0) { 
            System.out.println("No books in system."); 
            return; 
        }
        List<Book> relevantBooks = Book.filterGenre(books, genre); // filter the genre - call Book method 
        if (relevantBooks == null) { 
            System.out.printf("No books with genre %s.\n", genre); 
        } else if (relevantBooks.size() == 0) { 
            System.out.printf("No books with genre %s.\n", genre); 
        } else {
            for (Book b : relevantBooks) { 
                System.out.println(b.shortString()); 
            }
        }
    }

    public void getMemberBooks(String memberNumber) {
        if (memberNumber == null) { 
            System.out.println("No such member in system."); 
        } else if (members.size() == 0) { 
            System.out.println("No members in system.");
        } else {
            for (Member m : members) {
                if (m.getMemberNumber().equals(memberNumber)) {
                    if (m.renting().size() == 0) { 
                        System.out.println("Member not currently renting."); 
                        return; 
                    } else {
                        // output shortstrings in order 
                        for (Book bk : m.renting()) { 
                            System.out.println(bk.shortString()); 
                        }
                        return; 
                    }
                }
            }
            // if this runs, then no short string was ever printed, which means there was no such member 
            System.out.println("No such member in system.");
        }
    }

    public void memberRentalHistory(String memberNumber) {
        if (memberNumber == null) { 
            System.out.println("No such member in system."); 
        } else if (members.size() == 0) { 
            System.out.println("No members in system."); 
        } else {
            for (Member mem : members) {
                // found the member 
                if (mem.getMemberNumber().equals(memberNumber)) { 
                    // member hist empty?
                    if (mem.history().size() == 0) { 
                        System.out.println("No rental history for member."); 
                        return; 
                    }
                    // non-empty member hist 
                    else {
                        for (Book book : mem.history()) { System.out.println(book.shortString()); }
                        return;
                    }
                }
            }
            // non-null number, non-matching number forAll members 
            System.out.println("No such member in system.");
        }
    }

    public void relinquishAll(String memberNumber) {
        if (memberNumber == null) { 
            System.out.println("No such member in system."); 
        } else if (members.size() == 0) { 
            System.out.println("No members in system."); 
        } else {
            for (Member member : members) {
                // member does exist 
                if (member.getMemberNumber().equals(memberNumber)) {
                    // call the member relinquishAll method 
                    member.relinquishAll();
                    System.out.println("Success.");
                    return;
                }
            }
            System.out.println("No such member in system.");
        }
    }
    public void relinquishBook(String memberNumber, String serialNumber) {
        // nulls and basic checks 
        if (members.size() == 0) { 
            System.out.println("No members in system."); 
        } else if (books.size() == 0) { 
            System.out.println("No books in system."); 
        } else if (memberNumber == null) { 
            System.out.println("No such member in system."); 
        } else if (serialNumber == null) { 
            System.out.println("No such book in system."); 
        } else {
            boolean hasBook = false;
            // check if book in book collection 
            for (Book book : books) {
                if (book.getSerialNumber().equals(serialNumber)) {
                    hasBook = true;
                }
            }
            // no book found? 
            if (!(hasBook)) { 
                System.out.println("No such book in system."); 
            } else {
                for (Member m : members) {
                    if (m.getMemberNumber().equals(memberNumber)) {
                        // iterate thru Book objects in member renting book
                        for (Book book : m.renting()) {
                            // found matching book 
                            if (book.getSerialNumber().equals(serialNumber)) { 
                                // found matching member 
                                if (book.isRented() && book.getCurrentMember().getMemberNumber().equals(m.getMemberNumber())) {
                                    // do the relinquishing stuff
                                    book.renterHistory().add(m); 
                                    book.setCurrentMember(null); 
                                    m.history().add(book); 
                                    m.renting().remove(book);
                                    System.out.println("Success."); 
                                    return; 
                                }
                            }
                        }
                        // if this line runs, then it means the member was found, but the book wasn't rented by the given member
                        System.out.println("Unable to return book.");
                        return;
                    }
                }
                // if this line runs, no member had the same membernumber, and thus nonexistent member 
                System.out.println("No such member in system.");
            }
        }
    }
    public void rentBook(String memberNumber, String serialNumber) {
        // nulls & basic chex 
        if (members.size() == 0) { 
            System.out.println("No members in system."); 
        } else if (books.size() == 0) { 
            System.out.println("No books in system."); 
        } else if (memberNumber == null) { 
            System.out.println("No such member in system."); 
        } else if (serialNumber == null) { 
            System.out.println("No such book in system."); 
        } else {
            boolean hasBook = false;
            // check if book in book collection 
            for (Book book : books) {
                if (book.getSerialNumber().equals(serialNumber)) {
                    hasBook = true;
                }
            }
            if (!(hasBook)) { 
                System.out.println("No such book in system."); 
            } else {
                for (Member member : members) {
                    if (member.getMemberNumber().equals(memberNumber)) {
                        for (Book currentBook : books) {
                            if (currentBook.getSerialNumber().equals(serialNumber)) {
                                // if renting is a sucess
                                if (member.rent(currentBook)) { 
                                    System.out.println("Success."); 
                                    return; 
                                } else {
                                    System.out.println("Book is currently unavailable."); 
                                    return; 
                                }
                            }
                        }
                        // this running = no book with serial no. found 
                        System.out.println("No such book in system.");
                        return;
                    }
                }
                // this running = no member with the given member number could be found 
                System.out.println("No such member in system.");
            }
        }
    }
    public void saveCollection(String filename) {
        if (books.size() == 0) { 
            System.out.println("No books in system."); 
        } else {
            Book.saveBookCollection(filename, books);
            System.out.println("Success."); 
        }
    }

    public void common(String[] memberNumbers) {
        /* error handling */
        if (members.size() == 0) { 
            System.out.println("No members in system."); 
        } else if (books.size() == 0) { 
            System.out.println("No books in system."); 
        } else if (memberNumbers == null) { 
            System.out.println("No such member in system."); 
        } else {
            // relevant holds the relevant members to be passed to Member.commonBooks method()
            ArrayList<Member> relevant = new ArrayList<Member>();
            // iterate thru mem nums 
            for (String memnum : memberNumbers) {
                // iterate thru members 
                for (Member mem : members) {
                    if (mem == null) { 
                        System.out.println("No such member in system."); 
                        return; 
                    } else if ( mem.getMemberNumber().equals(memnum) ) { // there exists a member with current member number 
                        // prevent duplicates being added in 
                        if ( !(relevant.contains(mem)) ) {
                            relevant.add(mem);
                            break;
                        } else { 
                            System.out.println("Duplicate members provided."); 
                            return; 
                        }
                    }
                }
            }
            // at least 1 member not found
            if (relevant.size() != memberNumbers.length) { 
                System.out.println("No such member in system."); 
                return; 
            }
            // call the member method
            List<Book> commons = Member.commonBooks(relevant.toArray(new Member[relevant.size()])); 
            if (commons.size() == 0) { 
                System.out.println("No common books."); 
                return; 
            }
            for (Book book : commons) {
                System.out.println(book.shortString());
            }
        }
    }
    public void getMember(String memberNumber) {
        if (memberNumber == null) { 
            System.out.println("No such member in system."); 
        } else if (members.size() == 0) { 
            System.out.println("No members in system."); 
        } else {
            for (Member mem : members) {
                if (mem.getMemberNumber().equals(memberNumber)) { 
                    System.out.printf("%s: %s\n", mem.getMemberNumber(), mem.getName());
                    return;
                }
            }
            System.out.println("No such member in system.");
        }
    }

    public void run() {
        Scanner keyboard = new Scanner (System.in);
        while (true) {
            System.out.print("user: ");
            String input = keyboard.nextLine();
            String[] kwords = input.split(" ");
            // exit 
            if (input.toUpperCase().equals("EXIT")) {
                break;
            }
            // help string 
            else if (input.toUpperCase().equals("COMMANDS")) {
                System.out.println(Library.HELP_STRING);
            }
            // adders
            else if (kwords[0].toUpperCase().equals("ADD")) {
                // add book [filename] [serialnum] -> addbook(str bookfile, str serialnum)
                if (kwords[1].toUpperCase().equals("BOOK")) {
                    // String fname = "";
                    // int i = 2;
                    // for ( ; i < kwords.length - 1; i++) {
                    //     if (i == kwords.length - 2) {
                    //         fname += kwords[i];
                    //     } else{
                    //         fname += kwords[i] + " ";
                    //     }
                    // }
                    // String snumber = kwords[i + 1];
                    // addBook(fname, snumber);
                    addBook(kwords[2], kwords[3]);
                }
                // add collection [filename]
                else if (kwords[1].toUpperCase().equals("COLLECTION")) {
                    String filename = argbuilder(2, kwords);
                    addCollection(filename);
                }
                // add member [name]
                else if (kwords[1].toUpperCase().equals("MEMBER")) {
                    String addName = argbuilder(2, kwords);
                    addMember(addName);
                }
            }
            // book history [serialnum]
            else if (kwords[0].toUpperCase().equals("BOOK") && kwords[1].toUpperCase().equals("HISTORY")) {
                bookHistory(kwords[2]);
            }
            // common [member1] [member2] ...
            else if (kwords[0].toUpperCase().equals("COMMON")) {
                // create the array of member numbers 
                String[] memnums = new String[kwords.length - 1];
                int j = 0;
                for (int i = 1; i < kwords.length; i++) {
                    memnums[j] = kwords[i];
                    j += 1;
                }
                common(memnums);
            }

            // listers:
            else if (kwords[0].toUpperCase().equals("LIST")) {
                // list all (long)
                if (kwords[1].toUpperCase().equals("ALL")) {
                    if (kwords.length == 3 && kwords[2].toUpperCase().equals("LONG")) {
                        getAllBooks(true);
                    }
                    else { getAllBooks(false); }
                }
                // list authors
                else if (kwords[1].toUpperCase().equals("AUTHORS")) {
                    getAuthors();
                }
                // list available 
                else if (kwords[1].toUpperCase().equals("AVAILABLE")) {
                    if (kwords.length == 3 && kwords[2].toUpperCase().equals("LONG")) {
                        getAvailableBooks(true);
                    }
                    else { getAvailableBooks(false); }
                }
                // list genres
                else if (kwords[1].toUpperCase().equals("GENRES")) { getGenres(); }
            }
            // book [serialnum] (long)
            else if (kwords[0].toUpperCase().equals("BOOK")) {
                if (kwords.length == 3) {
                    if (kwords[2].toUpperCase().equals("LONG")) {
                        getBook(kwords[1], true);
                    }
                }
                else { getBook(kwords[1], false); }
            }
            // author [author]
            else if (kwords[0].toUpperCase().equals("AUTHOR")) {
                String checkauthor = argbuilder(1, kwords);
                getBooksByAuthor(checkauthor);
            }
            // genre [genre]
            else if (kwords[0].toUpperCase().equals("GENRE")) {
                String checkgenre = argbuilder(1, kwords);
                getBooksByGenre(checkgenre);
            }
            // numbers copies
            else if (kwords[0].toUpperCase().equals("NUMBER") && kwords[1].toUpperCase().equals("COPIES")) {
                getCopies();
            }
            // member related:
            else if (kwords[0].toUpperCase().equals("MEMBER")) {
                // member [membernumber] -> get member info 
                if (kwords.length == 2) {
                    getMember(kwords[1]);
                }
                // member history [memnum] - print out renting history of member 
                else if (kwords.length == 3 && kwords[1].toUpperCase().equals("HISTORY")) {
                    memberRentalHistory(kwords[2]);
                }
                // member books [memnum] - print all books member is renting 
                else if (kwords.length == 3 && kwords[1].toUpperCase().equals("BOOKS")) {
                    getMemberBooks(kwords[2]);
                }
            }
            // relinquishes 
            else if (kwords[0].toUpperCase().equals("RELINQUISH")) {
                // relinquish all [memnum]
                if (kwords[1].toUpperCase().equals("ALL")) {
                    relinquishAll(kwords[2]);
                }
                // relinquish [memnum] [serialnum]
                else { relinquishBook(kwords[1], kwords[2]); }
            }
            // rent [memnum] [serialnum]
            else if (kwords[0].toUpperCase().equals("RENT") && kwords.length == 3) {
                rentBook(kwords[1], kwords[2]);
            }
            // save collection [filename]
            else if (kwords[0].toUpperCase().equals("SAVE") && kwords[1].toUpperCase().equals("COLLECTION")) {
                String filename = argbuilder(2, kwords);
                saveCollection(filename);
            }
            System.out.println();
        }
        System.out.println("Ending Library process.");
    }
    public static void main(String[] args) {
        Library testlib = new Library();
        testlib.run();
    }
}
