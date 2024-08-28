package io.spbx.orm.api.query;

/**
 * Represents a DDL (Data Definition Language) query.
 * Includes <code>CREATE</code>, <code>DROP</code>, <code>ALTER</code> and <code>TRUNCATE</code> query families.
 */
public interface DataDefinitionQuery extends Representable, HasArgs {
}
