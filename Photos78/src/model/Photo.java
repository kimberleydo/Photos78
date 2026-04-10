package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * shows a single photo with a file path, caption and date, and tags.
 * 
 * @author Mihail Bogdanoski, Kim Do
 */
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String  filePath;
    private String  caption;
    private Calendar dateTaken;
    private List<Tag> tags;

    /**
     * makes a Photo with the given file path.
     * Date is picked from the file's last-modified time.
     * @param filePath path to the photo file on disk
     */
    public Photo(String filePath) {
        this.filePath  = filePath;
        this.caption   = "";
        this.tags      = new ArrayList<>();

        // Set date from file's last-modified time
        java.io.File file = new java.io.File(filePath);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(file.lastModified());
        cal.set(Calendar.MILLISECOND, 0); 
        this.dateTaken = cal;
    }

    // Getters
    public String   getFilePath()  { return filePath; }
    public String   getCaption()   { return caption; }
    public Calendar getDateTaken() { return dateTaken; }
    public List<Tag> getTags()     { return tags; }

    // Setters
    public void setCaption(String caption) { this.caption = caption; }

    /**
     * Adds a tag if it doesn't exist
     * @param tag the tag to add
     * @return true if added, false if duplicate
     */
    public boolean addTag(Tag tag) {
        if (tags.contains(tag)) return false;
        tags.add(tag);
        return true;
    }

    /**
     * Removes a tag from this photo.
     * @param tag the tag to remove
     * @return true if removed, false if not found
     */
    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    /**
     * Checks if this photo has a tag matching what the given name and value aready is
     * @param name  tag name to search
     * @param value tag value to search
     */
    public boolean hasTag(String name, String value) {
        return tags.contains(new Tag(name, value));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Photo)) return false;
        Photo other = (Photo) obj;
        return filePath.equals(other.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public String toString() {
        return filePath + " | " + caption;
    }
}