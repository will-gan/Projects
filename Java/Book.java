import java.util.*;
import java.io.*;

public class Book {
    private String title, author, genre, serialNumber;
    private Member currentMember = null;
    private ArrayList<Member> memberHist = new ArrayList<>();
    // constructor
    public Book(String t, String auth, String gen, String sNum) {
        title = t; 
        author = auth; 
        genre = gen; 
        serialNumber = sNum;
    }
    // getters 
    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setCurrentMember(Member m) {
        currentMember = m;
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public String getGenre() {
        return genre;
    }
    // other 
    public String longString() {
        if (currentMember != null) {
            // serial number: title (author, genre) ... renter number 
            String ret = String.format("%s: %s (%s, %s)\nRented by: %s.", serialNumber, title, author, genre, currentMember.getMemberNumber());
            return ret; 
        }
        
        String ret = String.format("%s: %s (%s, %s)\nCurrently available.", serialNumber, title, author, genre);
        return ret; 
    }

    public String shortString() {
        String ret = String.format("%s (%s)", title, author);
        return ret; 
    }

    public boolean isRented() {
        if (currentMember != null) { 
            return true; 
        }
        return false;
    }
    /*
    Creates a new list containing books by the specified author.

    If the list or author does not exist, return null.
    If they do exist, create a new list with all the books written by the given author, 
    sort by serial number, and return the result.
    */
    public static List<Book> filterAuthor(List<Book> books, String auth) {
        /* error-handling here */
        if (books == null || auth == null) { 
            return null; 
        }
        List<Book> newBooks = new ArrayList<>();
        ArrayList<String> serials = new ArrayList<>();
        // non-error 
        for (Book b : books) {
            if (b.getAuthor().equals(auth) && !(newBooks.contains(b)) ) {
                serials.add(b.getSerialNumber());
                newBooks.add(b);
            }
        }
        Collections.sort(serials);
        for (int j = 0; j < serials.size(); j++) {
            for (int i = 0; i < newBooks.size(); i++) {
                if (newBooks.get(i).getSerialNumber().equals(serials.get(j))) {
                    newBooks.set(j, newBooks.get(i));
                }
            }
        }
        return newBooks;
    }
    /*Creates a new list containing books by the specified genre.

    If the list or genre does not exist, return null.
    If they do exist, create a new list with all the books in the specified genre, sort by serial number, and return the result.
    */
    public static List<Book> filterGenre(List<Book> books, String gen) {
        if (books == null || gen == null) { 
            return null; 
        }
        // non-error section 
        List<Book> newBooks = new ArrayList<>();
        ArrayList<String> serials = new ArrayList<>();
        for (Book b : books) {
            if (b == null) { 
                continue; 
            } else if (b.getGenre().equals(gen) && !(newBooks.contains(b)) ) {
                serials.add(b.getSerialNumber());
                newBooks.add(b);
            }
        }
        Collections.sort(serials); // sort section 
        for (int j = 0; j < serials.size(); j++) {
            for (int i = 0; i < newBooks.size(); i++) {
                if (newBooks.get(i).getSerialNumber().equals(serials.get(j))) { // get serial number in sorted serials list & compare if eq. 
                    newBooks.set(j, newBooks.get(i)); // sort order accordingly 
                }
            }
        }
        return newBooks;
    }
    
    public static void saveBookCollection(String filename, Collection<Book> books) {
        if (filename == null || books == null) { 
            return; 
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename);
        }
        catch (IOException io) { 
            return; 
        }
        pw.println("serialNumber,title,author,genre");
        for (Book b : books) {
            if (b == null) { 
                continue; 
            }
            pw.printf("%s,%s,%s,%s\n", b.getSerialNumber(), b.getTitle(), b.getAuthor(), b.getGenre());
            pw.flush(); // flush buffer - ion rlly wanna lose data man
        }

        pw.close(); // close that sh1t s0n
    }
    public ArrayList<Member> renterHistory() {
        return memberHist;
    }

    public static Book readBook(String filename, String serialNumber) {
        if (filename == null) { 
            System.out.println("No such file."); 
            return null; 
        } 
        else if (serialNumber == null) { 
            System.out.println("No such book in file."); 
            return null; 
        }
        Scanner s = null;

        try {
            s = new Scanner(new File(filename));
        }
        catch (FileNotFoundException e) { 
            System.out.println("No such file.");
            return null; 
        }

        Book retval = null;
        // read in file 
        while (s.hasNextLine()) {
            // serial number, title, author, genre - CSV line format 
            String[] row = s.nextLine().split(",");
            if (row[0].equals(serialNumber)) { 
                retval = new Book(row[1], row[2], row[3], row[0]);
                break;
            }
        }
        // if retval is null, then clearly the serial number was never found
        // and thus, it doesn't exist in file 
        if (retval == null) { 
            System.out.println("No such book in file."); 
        }
        s.close(); // close stream 
        return retval; 
    }

    public static ArrayList<Book> readBookCollection(String filename) {
        if (filename == null) { return null; }
        Scanner s = null;
        try {
            s = new Scanner(new File(filename));
        }
        catch (FileNotFoundException e) { return null; }

        ArrayList<Book> retval = new ArrayList<>();
        
        while (s.hasNextLine()) {
            // serial number, title, author, genre - CSV line format 
            String[] row = s.nextLine().split(",");
            if (row.length != 4) { 
                continue; 
            } // ignore the header 
            else if (row[0].equals("serialNumber")) { 
                continue; 
            } else {
                Book value = new Book(row[1], row[2], row[3], row[0]);
                retval.add(value);
            }
        }
        s.close();
        return retval;
    }

    public boolean rent(Member member) {
        if (member == null || isRented() ) { 
            return false; 
        }
        currentMember = member; // set renter
        return true;

    }
    
    public boolean relinquish(Member member) {
        if (member == null) { 
            return false; 
        } // if rented, and renter is eq. to given member 
        else if (isRented() && currentMember.getMemberNumber().equals(member.getMemberNumber())) {
            memberHist.add(member); // add the member to the renter history 
            currentMember = null;
            return true; // return true 
        }
        // member does not exist, or isn't the current renter 
        else { 
            return false; 
        }
    }
}
