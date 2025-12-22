package com.yowyob.feedback.entity;

/**
 * Enumeration representing the type of user in the system.
 *
 * Users can be either individuals (PERSON) or organizations (ORGANIZATION).
 * This determines which subtype table (person or organization) contains
 * additional information about the user.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
public enum UserType {

    /**
     * Individual person user.
     */
    PERSON,

    /**
     * Organization user.
     */
    ORGANIZATION
}