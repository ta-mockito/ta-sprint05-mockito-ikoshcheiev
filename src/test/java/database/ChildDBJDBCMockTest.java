package database;

import dao.ChildDB;
import model.Child;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JDBC-level mock tests for ChildDB")
class ChildDBJDBCMockTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSet generatedKeys;

    private ChildDB childDB;

    @BeforeEach
    void setUp() {
        childDB = new ChildDB(connection);
    }

    @Test
    @DisplayName("addChild: inserts child and returns with generated ID")
    void addChildShouldInsertAndReturnWithId() throws SQLException {
        Child input = new Child(null, "First", "Last", LocalDate.of(2010, 1, 1));

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(77L);

        Child result = childDB.addChild(input);

        assertAll(
                () -> assertEquals(77L, result.id()),
                () -> assertEquals("First", result.firstName()),
                () -> assertEquals("Last", result.lastName()),
                () -> assertEquals(LocalDate.of(2010, 1, 1), result.birthDate())
        );
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addChild: inserts new if not found")
    void addChildShouldInsertWhenNotFound() throws SQLException {
        String firstName = "First-new";
        String lastName = "Last-new";
        LocalDate birthDate = LocalDate.of(2010, 1, 1);
        Child input = new Child(null, firstName, lastName, birthDate);

        // call insert
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Mocking getGeneratedKeys
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true); // Generated key exists
        when(generatedKeys.getLong(1)).thenReturn(77L);

        Child result = childDB.addChild(input);

        assertAll(
                () -> assertEquals(77L, result.id()),
                () -> assertEquals("First-new", result.firstName()),
                () -> assertEquals("Last-new", result.lastName()),
                () -> assertEquals(birthDate, result.birthDate())
        );
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addChild: throws SQLException when generated keys missing")
    void addChildShouldThrowWhenKeysMissing() throws SQLException {
        Child input = new Child(null, "First", "Last", LocalDate.of(2010, 1, 1));

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(false); // ключ не пришёл

        assertThrows(SQLException.class, () -> childDB.addChild(input));
    }

    @Test
    @DisplayName("updateChild: returns true if affected rows == 1")
    void updateChildShouldReturnTrue() throws SQLException {
        Child input = new Child(1L, "firstname", "lastname", LocalDate.of(2010, 1, 1));
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean updated = childDB.updateChild(input);

        assertTrue(updated);
    }

    @Test
    @DisplayName("updateChild: returns false if affected rows == 0")
    void updateChildShouldReturnFalse() throws SQLException {
        Child input = new Child(1L, "firstname", "lastname", LocalDate.of(2010, 1, 1));
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean updated = childDB.updateChild(input);

        assertFalse(updated);
    }

    // How to write tests for all fields? assertAll? or ParametrizedTest? or one by one
    @Test
    @DisplayName("findChildrenByFirstName: returns matching Children")
    void findChildrenByFirstNamePartShouldReturnList() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // 2 results
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("first_name")).thenReturn("First", "First-new");
        when(resultSet.getString("last_name")).thenReturn("Last", "Last-new");
        when(resultSet.getDate("birth_date")).thenReturn(Date.valueOf(LocalDate.of(2010, 1, 1)), Date.valueOf(LocalDate.of(2012, 2, 2)));

        List<Child> results = childDB.findChildrenByFirstNamePart("First");

        assertEquals(2, results.size());
        assertAll(
                () -> assertEquals(1L, results.getFirst().id()),
                () -> assertEquals("First", results.getFirst().firstName()),
                () -> assertEquals("Last", results.getFirst().lastName()),
                () -> assertEquals(LocalDate.of(2010, 1, 1), results.getFirst().birthDate())
        );
    }

    @Test
    @DisplayName("Validation: addChild(null) throws exception")
    void addChildShouldValidateNull() {
        assertThrows(IllegalArgumentException.class, () -> childDB.addChild(null));
    }
}