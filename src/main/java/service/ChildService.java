package service;

import dao.ChildDAO;
import dao.exception.DatabaseException;
import model.Child;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for Child operations.
 * Handles business logic, input validation, and exception translation.
 */
public class ChildService {
    private final ChildDAO childDAO;

    public ChildService(ChildDAO childDAO) {
        if (childDAO == null) {
            throw new IllegalArgumentException("ChildDAO cannot be null");
        }
        this.childDAO = childDAO;
    }

    /**
     * Adds a new child.
     *
     * @param child the child to add
     * @return the added child
     * @throws IllegalArgumentException if child is null
     * @throws DatabaseException if a database error occurs
     */
    public Child addChild(Child child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        if (child.firstName() == null || child.firstName().strip().isBlank()) {
            throw new IllegalArgumentException("Child first name cannot be null or empty");
        }
        if (child.lastName() == null || child.lastName().strip().isBlank()) {
            throw new IllegalArgumentException("Child last name cannot be null or empty");
        }
        try {
            return childDAO.addChild(child);
        } catch (SQLException e) {
            throw new DatabaseException("Error adding child: " + child.firstName() + " " + child.lastName(), e);
        }
    }

    /**
     * Updates an existing child's information.
     *
     * @param child the child info to update
     * @return true if update was successful
     * @throws IllegalArgumentException if child or child ID is null
     * @throws DatabaseException if a database error occurs or child not found
     */
    public boolean updateChild(Child child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        if (child.id() == null) {
            throw new IllegalArgumentException("Child ID cannot be null for update");
        }
        try {
            boolean updated = childDAO.updateChild(child);
            if (!updated) {
                throw new DatabaseException("Child with ID " + child.id() + " not found or could not be updated");
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating child with ID: " + child.id(), e);
        }
    }

    /**
     * Renames an existing child.
     *
     * @param id          the ID of the child to rename
     * @param firstName   the first name for the child
     * @param lastName    the last name for the child
     * @param dateOfBirth the date of birth for the child
     * @return true if the rename was successful
     * @throws IllegalArgumentException if id is null or firstName/lastName is null/blank
     * @throws DatabaseException        if a database error occurs or child is not found
     */
    public boolean renameChild(Long id, String firstName, String lastName, LocalDate dateOfBirth) {
        if (id == null) {
            throw new IllegalArgumentException("Child ID cannot be null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("New firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("New lastName cannot be null or blank");
        }

        try {
            Child childToUpdate = new Child(id, firstName.strip(), lastName.strip(), dateOfBirth);
            boolean updated = childDAO.updateChild(childToUpdate);
            if (!updated) {
                throw new DatabaseException("Child with ID " + id + " not found or could not be updated");
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error renaming child with ID: " + id, e);
        }
    }

    /**
     * Deletes a child by ID.
     *
     * @param id the ID of the child to delete
     * @return true if deletion was successful
     * @throws IllegalArgumentException if id is null
     * @throws DatabaseException if a database error occurs
     */
    public boolean deleteChild(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Child ID cannot be null");
        }
        try {
            boolean deleted = childDAO.deleteChild(id);
            if (!deleted) {
                throw new DatabaseException("Child with ID " + id + " not found or could not be deleted");
            }
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting child with ID: " + id, e);
        }
    }

    /**
     * Finds children who are at least a certain age.
     *
     * @param minAge the minimum age
     * @return list of children
     * @throws IllegalArgumentException if minAge is negative
     * @throws DatabaseException if a database error occurs
     */
    public List<Child> findOlderChildren(int minAge) {
        if (minAge < 0) {
            throw new IllegalArgumentException("Minimum age cannot be negative");
        }
        try {
            return childDAO.findChildrenWithMinimumAge(minAge);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children with minimum age: " + minAge, e);
        }
    }

    /**
     * Finds children who don't have a birth date specified.
     *
     * @return list of children
     * @throws DatabaseException if a database error occurs
     */
    public List<Child> findChildrenMissingBirthDate() {
        try {
            return childDAO.findChildrenWithoutBirthDate();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children without birth date", e);
        }
    }

    /**
     * Searches for children born on the given date.
     *
     * @param birthDate the birthdate to search for
     * @return a list of children born on the specified date
     * @throws IllegalArgumentException if birthDate is null
     * @throws DatabaseException        if a database error occurs
     */
    public List<Child> searchChildrenByBirthdate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("birthDate must not be null");
        }
        try {
            return childDAO.findChildrenByBirthdate(birthDate);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children by birth date", e);
        }
    }

    /**
     * Searches for children whose first names contain the given string (case-insensitive).
     *
     * @param firstName the part of the first name to search for
     * @return a list of children whose first name contains the given string
     * @throws IllegalArgumentException if firstName is null or empty
     * @throws DatabaseException        if a database error occurs
     */
    public List<Child> searchChildrenByFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must not be null or empty");
        }
        try {
            return childDAO.findChildrenByFirstNamePart(firstName.strip());
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children by first name part", e);
        }
    }

    /**
     * Searches for children whose last names contain the given string (case-insensitive).
     *
     * @param lastName the part of the last name to search for
     * @return a list of children whose last name contains the given string
     * @throws IllegalArgumentException if lastName is null or empty
     * @throws DatabaseException        if a database error occurs
     */
    public List<Child> searchChildrenByLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName must not be null or empty");
        }
        try {
            return childDAO.findChildrenByLastNamePart(lastName.strip());
        } catch (SQLException e) {
            throw new DatabaseException("Error finding children by last name part", e);
        }
    }
}
