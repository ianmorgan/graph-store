package ianmorgan.graphstore.checker;

import java.util.Objects;

/**
 * A many relationship
 */
public class OneOf implements Relationship {
    private Object theType;

    public OneOf(Object theType) {
        this.theType = theType;
    }

    @Override
    public Object theType() {
        return theType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OneOf oneOf = (OneOf) o;
        return Objects.equals(theType, oneOf.theType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theType);
    }
}
