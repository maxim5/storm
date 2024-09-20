package io.spbx.orm.api.query;

/**
 * Represents a DDL (Data Definition Language) query.
 * Includes {@code CREATE}, {@code DROP}, {@code ALTER} and {@code TRUNCATE} query families.
 */
public interface DataDefinitionQuery extends Representable, HasArgs {
}
