import java.util.*;

public class Member { 
    private String num, name;
    private ArrayList<Book> currentlyRenting = new ArrayList<>();
    private ArrayList<Book> bookhist = new ArrayList<>();

    public Member(String n, String number) {
        name = n;
        num = number;
    }
    public String getMemberNumber() {
        return num;
    }
    public String getName() {
        return name;
    }

    public ArrayList<Book> history() {
        return bookhist;
    }

    public ArrayList<Book> renting() {
        return currentlyRenting;
    }

    public boolean rent(Book book) {
        if (book == null) { 
            return false; 
        } else if (!(book.isRented())) {
            currentlyRenting.add(book);
            // set renter of the book to instance of this member
            book.setCurrentMember(this);
            return true; 
        } else { 
            return false; 
        }
    }

    public boolean relinquish(Book book) {
        if (book == null) { 
            return false; 
        } else if ( !(book.isRented()) ) { 
            return false; 
        } // equal member numbers = member renting
        else if ( book.isRented() && book.getCurrentMember().getMemberNumber().equals(num)) {
            bookhist.add(book); // add book into book history
            currentlyRenting.remove(book); // remove book from currentlyrenting
            book.setCurrentMember(null); // set current book mem = null 
            return true;
        } else { 
            return false; 
        }
    }

    public void relinquishAll() {
        for (Book b : currentlyRenting) {
            bookhist.add(b); // add book to book history 
            b.renterHistory().add(this); // add to each book rented, this member into the renter history 
            b.setCurrentMember(null); // set the current member of books to be null 
        }
        currentlyRenting.clear(); // remove all books from currently renting collection 
    }
    
    public static List<Book> commonBooks(Member[] members) {
        /* error-handling section */
        if (members == null) { 
            return null; 
        } else if (members.length == 1) { return members[0].history(); } 
        // null checking - duplicates handled in common() for Library 
        for (int i = 0; i < members.length; i++) {
            if (members[i] == null) { return null; } // check for null member 
        }
        
        /* Non-error section  - Member[] size >= 1*/ 
        Member pivot = members[0];
        List<Book> retval = new ArrayList<>();
        ArrayList<String> serials = new ArrayList<>();
        for (Book book : pivot.history()) { // take the pivot member, and go thru book-by-book in history
            int matching = 1; // count matches amongst other members 
            for (int i = 1; i < members.length; i++) { // go thru each member 
                for (Book bo : members[i].history()) { // look thru current subsequent member's history 
                    if ( bo.getSerialNumber().equals(book.getSerialNumber()) ) { // where the book serials are eq.
                        matching++; // increase the matching count
                        break; // break so no need for any unnecessary traversal in the current member's collection
                    }
                }
            }
            if ( matching == members.length && !(serials.contains(book.getSerialNumber())) ) { 
                // matching eq. to members.length means it's a common intersect for all members 
                retval.add(book);
                serials.add(book.getSerialNumber());
            }
        }
        // sorting 
        Collections.sort(serials);
        int i = 0;
        for (String str : serials) {
            for (Book bo : retval) {
                if (bo.getSerialNumber().equals(str)) {
                    retval.set(i, bo); // move books into required order 
                    i++;
                }
            }
        }
        return retval;
    }
}
