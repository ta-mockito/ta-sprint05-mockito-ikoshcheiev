package dao;

import model.Child;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for Child entities.
 * Provides methods to perform CRUD operations on children.
 */
public interface ChildDAO {
    
    /**
     * Adds a new child to the database.
     *
     * @param child The child to add
     * @return The added child with its ID
     * @throws SQLException If a database access error occurs
     * @throws IllegalArgumentException If a child is null or has invalid data
     */
    Child addChild(Child child) throws SQLException;
    
    /**
     * Updates an existing child in the database.
     *
     * @param child The child to update
     * @return true if the child was updated, false otherwise
     * @throws SQLException If a database access error occurs
     * @throws IllegalArgumentException If a child is null or has invalid data
     */
    boolean updateChild(Child child) throws SQLException;
    
    /**
     * Deletes a child from the database.
     *
     * @param id The ID of the child to delete
     * @return true if the child was deleted, false otherwise
     * @throws SQLException If a database access error occurs
     * @throws IllegalArgumentException If id is null
     */
    boolean deleteChild(Long id) throws SQLException;
    
    /**
     * Finds all children with at least the specified age.
     *
     * @param age The minimum age of children to find
     * @return A list of children with at least the specified age
     * @throws SQLException If a database access error occurs
     * @throws IllegalArgumentException If age is negative
     */
    List<Child> findChildrenWithMinimumAge(int age) throws SQLException;
    
    /**
     * Finds all children without a birthdate.
     *
     * @return A list of children without a birthdate
     * @throws SQLException If a database access error occurs
     */
    List<Child> findChildrenWithoutBirthDate() throws SQLException;

    /**
     * Finds children whose first names contain the given string (case-insensitive).
     *
     * @param firstName The string to search for in children first names
     * @return A list of children whose first name contains the given string
     * @throws SQLException             If a database access error occurs
     * @throws IllegalArgumentException If firstName is null
     */
    List<Child> findChildrenByFirstNamePart(String firstName) throws SQLException;

    /**
     * Finds children whose last names contain the given string (case-insensitive).
     *
     * @param lastName The string to search for in children last names
     * @return A list of children whose last name contains the given string
     * @throws SQLException             If a database access error occurs
     * @throws IllegalArgumentException If lastName is null
     */
    List<Child> findChildrenByLastNamePart(String lastName) throws SQLException;

    /**
     * Finds children born on the given date.
     *
     * @param birthdate The birthdate to search for, in "yyyy-MM-dd" format
     * @return A list of children born on the specified date
     * @throws SQLException             If a database access error occurs
     * @throws IllegalArgumentException If birthdate is null or has invalid format
     */
    List<Child> findChildrenByBirthdate(LocalDate birthdate) throws SQLException;

}