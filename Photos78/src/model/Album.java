package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents an album containing a collection of photos.
 * 
 * @author Mihail Bogdanoski, Kim Do
 */
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    private String      name;
    private List<Photo> photos;

    /**
     * Constructs an empty album
     * @param name the album name
     */
    public Album(String name) {
        this.name   = name;
        this.photos = new ArrayList<>();
    }

    public String      getName()   { return name; }
    public List<Photo> getPhotos() { return photos; }
    public int         getSize()   { return photos.size(); }

    public void setName(String name) { this.name = name; }

    /**
     * Adds a photo if it's not already in
     * @param photo the photo to add
     * @return true if added, false if duplicate
     */
    public boolean addPhoto(Photo photo) {
        if (photos.contains(photo)) return false;
        photos.add(photo);
        return true;
    }

    /**
     * Removes a photo from this album
     * @param photo -> the photo to remove
     */
    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }

    /**
     * Returns the oldest date among all photos
     * @return earliest Calendar date
     */
    public Calendar getEarliestDate() {
        if (photos.isEmpty()) return null;
        Calendar earliest = photos.get(0).getDateTaken();
        for (Photo p : photos) {
            if (p.getDateTaken().before(earliest)) {
                earliest = p.getDateTaken();
            }
        }
        return earliest;
    }

    /**
     * Returns the newest date among all photos
     * @return latest Calendar date
     */
    public Calendar getLatestDate() {
        if (photos.isEmpty()) return null;
        Calendar latest = photos.get(0).getDateTaken();
        for (Photo p : photos) {
            if (p.getDateTaken().after(latest)) {
                latest = p.getDateTaken();
            }
        }
        return latest;
    }

    @Override
    public String toString() {
        return name + " (" + photos.size() + " photos)";
    }
}