package database;

import dao.ChildDAO;
import dao.exception.DatabaseException;
import model.Child;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ChildService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Mock demo for child flow")
class ChildDBMockTest {

    @Mock
    private ChildDAO childDAO;

    @InjectMocks
    private ChildService childService;

    @Test
    @DisplayName("Mock: search by first name delegates to DAO with normalized input")
    void searchChildrenByFirstNameShouldNormalizeAndDelegate() throws SQLException {
        when(childDAO.findChildrenByFirstNamePart("pet")).thenReturn(List.of(
                new Child(1L, "Petr", "Petrov", LocalDate.of(2026, 5, 20))
        ));

        List<Child> result = childService.searchChildrenByFirstName("  pet  ");

        assertEquals(1, result.size());
        assertEquals("Petr", result.get(0).firstName());
        verify(childDAO, times(1)).findChildrenByFirstNamePart("pet");
    }

    @Test
    @DisplayName("Mock: search by last name delegates to DAO with normalized input")
    void searchChildrenByLastNameShouldNormalizeAndDelegate() throws SQLException {
        when(childDAO.findChildrenByLastNamePart("pet")).thenReturn(List.of(
                new Child(1L, "Petr", "Petrov", LocalDate.of(2026, 5, 20))
        ));

        List<Child> result = childService.searchChildrenByLastName("  pet  ");

        assertEquals(1, result.size());
        assertEquals("Petrov", result.get(0).lastName());
        verify(childDAO, times(1)).findChildrenByLastNamePart("pet");
    }

    @Test
    @DisplayName("Mock: search by birth date delegates to DAO")
    void searchChildrenByBirthDateShouldDelegate() throws SQLException {
        LocalDate birthdate = LocalDate.of(2000, 1, 15);
        when(childDAO.findChildrenByBirthdate(birthdate)).thenReturn(List.of(
                new Child(1L, "Petr", "Petrov", birthdate)
        ));

        List<Child> result = childService.searchChildrenByBirthdate(birthdate);

        assertEquals(1, result.size());
        assertEquals(birthdate, result.get(0).birthDate());
        verify(childDAO, times(1)).findChildrenByBirthdate(birthdate);
    }

    @Test
    @DisplayName("Mock: search throws IllegalArgumentException for null/blank input")
    void searchChildrenShouldValidateInput() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> childService.searchChildrenByFirstName(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> childService.searchChildrenByFirstName("   ")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> childService.searchChildrenByLastName(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> childService.searchChildrenByLastName("   ")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> childService.searchChildrenByBirthdate(null))
        );
    }

    @Test
    @DisplayName("Mock: search translates SQLException to DatabaseException")
    void searchChildrenShouldTranslateException() throws SQLException {
        when(childDAO.findChildrenByFirstNamePart(any())).thenThrow(new SQLException("DB error"));
        when(childDAO.findChildrenByLastNamePart(any())).thenThrow(new SQLException("DB error"));
        when(childDAO.findChildrenByBirthdate(any())).thenThrow(new SQLException("DB error"));

        assertAll(
                () -> assertThrows(DatabaseException.class,
                        () -> childService.searchChildrenByFirstName("test")),
                () -> assertThrows(DatabaseException.class,
                        () -> childService.searchChildrenByLastName("test")),
                () -> assertThrows(DatabaseException.class,
                        () -> childService.searchChildrenByBirthdate(LocalDate.of(2000, 1, 15)))
        );
    }

    @Test
    @DisplayName("Mock: rename delegates update with expected id and title")
    void renameChildShouldCallUpdate() throws SQLException {
        when(childDAO.updateChild(any(Child.class))).thenReturn(true);

        boolean renamed = childService.renameChild(7L, "firstname", "lastname", LocalDate.of(2006, 5, 20));

        assertEquals(true, renamed);
        verify(childDAO).updateChild(new Child(7L, "firstname", "lastname", LocalDate.of(2006, 5, 20)));
    }

    @Test
    @DisplayName("Mock: rename strips whitespace before delegating to DAO")
    void renameChildShouldStripAndDelegate() throws SQLException {
        when(childDAO.updateChild(any(Child.class))).thenReturn(true);

        childService.renameChild(7L, "  firstname  ", "  lastname  ", LocalDate.of(2006, 5, 20));

        verify(childDAO).updateChild(new Child(7L, "firstname", "lastname", LocalDate.of(2006, 5, 20)));
    }

    @Test
    @DisplayName("Mock: rename validates null ID and blank title")
    void renameChildShouldValidateInput() throws SQLException {
        assertAll(
                () -> {
                    IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                            () -> childService.renameChild(null, "firstname", "lastname", LocalDate.of(2006, 5, 20)));
                    assertEquals("Child ID cannot be null", ex1.getMessage());
                },
                () -> {

                    IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                            () -> childService.renameChild(7L, null, "lastname", LocalDate.of(2006, 5, 20)));
                    assertEquals("New firstName cannot be null or blank", ex2.getMessage());
                },
                () -> {

                    IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                            () -> childService.renameChild(7L, "firstname", null, LocalDate.of(2006, 5, 20)));
                    assertEquals("New lastName cannot be null or blank", ex3.getMessage());
                },
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> childService.renameChild(7L, "   ", "lastname", LocalDate.of(2006, 5, 20)));
                    assertEquals("New firstName cannot be null or blank", ex.getMessage());
                },
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> childService.renameChild(7L, "firstname", "   ", LocalDate.of(2006, 5, 20)));
                    assertEquals("New lastName cannot be null or blank", ex.getMessage());
                }
        );
        verify(childDAO, never()).updateChild(any(Child.class));
    }

    @Test
    @DisplayName("Mock: rename throws DatabaseException when update returns false")
    void renameChildShouldThrowWhenNotFound() throws SQLException {
        when(childDAO.updateChild(any())).thenReturn(false);

        assertThrows(dao.exception.DatabaseException.class,
                () -> childService.renameChild(100L, "Fname", "Lname", LocalDate.of(2006, 5, 20)));
    }

    @Test
    @DisplayName("Mock: addChild happy path")
    void addChildShouldDelegateToDao() throws SQLException {
        Child input = new Child(null, "Fname", "Lname", LocalDate.of(2006, 5, 20));
        Child expected = new Child(100L, "Fname", "Lname", LocalDate.of(2006, 5, 20));
        when(childDAO.addChild(input)).thenReturn(expected);

        Child result = childService.addChild(input);

        assertEquals(100L, result.id());
        assertEquals("Fname", result.firstName());
        assertEquals("Lname", result.lastName());
        assertEquals(LocalDate.of(2006, 5, 20), result.birthDate());
        verify(childDAO).addChild(input);
    }

    @Test
    @DisplayName("Mock: addChild validation")
    void addChildShouldValidateInput() {
        assertThrows(IllegalArgumentException.class, () -> childService.addChild(null));
        assertThrows(IllegalArgumentException.class, () -> childService.addChild(new Child(null, null, null)));
    }
}