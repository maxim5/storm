package io.spbx.orm.api;

/**
 * Represents the scope of foreign key referencing in read operations.
 */
public enum ReadFollow {
    /**
     * Does not follow foreign keys at all.
     * Only own table columns are fetched and foreign entities usually get only an id.
     */
    NO_FOLLOW,
    /**
     * Follow only immediate foreign keys of this table and fetch immediate entities on reads.
     */
    FOLLOW_ONE_LEVEL,
    /**
     * Follow all foreign keys recursively from this table onwards.
     * Should be used with care because might lead to a halt.
     */
    FOLLOW_ALL,
}
