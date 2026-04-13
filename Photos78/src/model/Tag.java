package model;

import java.io.Serializable;

/**
 * shows a tag on a photo, consisting of a name-value pair.
 * 
 * @author Mihail Bogdanoski, Kim Do
 */
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    /**
     * makes a Tag with a given name and value.
     * @param name  the tag type name 
     * @param value the tag value 
     */
    public Tag(String name, String value) {
        this.name = name.toLowerCase().trim();
        this.value = value.toLowerCase().trim();
    }

    public String getName()  { return name; }
    public String getValue() { return value; }

    /**
     * Two tags are the same if both name and value are the same.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        return name.equals(other.name) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode(); //generates hash code based on name and value
                                                        //equal tags have the same hash code
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
