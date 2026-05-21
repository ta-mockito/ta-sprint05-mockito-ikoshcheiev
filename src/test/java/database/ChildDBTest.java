package database;

import dao.ChildDB;
import model.Child;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.DBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Child Database Operations Tests")
class ChildDBTest {

    private final ChildDB db;

    ChildDBTest() {
        db = new ChildDB();
    }

    @AfterEach
    void tearDown() throws Exception {
        db.close();
    }

    @BeforeAll
    static void beforeAll() throws SQLException, IOException {
        System.out.println("[DEBUG_LOG] Executing init.sql");
        new DBUtil().executeFile("init.sql");

        // Check if a child table is empty after initialization
        String query = "SELECT COUNT(*) FROM child";
        try (Connection conn = DBUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            rs.next();
            int count = rs.getInt(1);
            System.out.println("[DEBUG_LOG] Child count after initialization: " + count);
        }
    }

    @Test
    @DisplayName("Should add a child and return it with an ID")
    void addShouldAddChildAndReturnWithId() throws SQLException {
        // Arrange
        String firstName = "John";
        String lastName = "Doe";
        LocalDate birthDate = LocalDate.of(2010, 1, 1);
        Child child = new Child(firstName, lastName, birthDate);

        // Act
        Child addedChild = db.addChild(child);
        System.out.println("[DEBUG_LOG] Added child ID: " + addedChild.id());

        // Assert
        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertEquals(firstName, addedChild.firstName(), "First name should match");
        assertEquals(lastName, addedChild.lastName(), "Last name should match");
        assertEquals(birthDate, addedChild.birthDate(), "Birth date should match");
    }

    @Test
    @DisplayName("Should add a child with null birth date")
    void addShouldHandleNullBirthDate() throws SQLException {
        // Arrange
        String firstName = "NoBirth";
        String lastName = "Date";
        Child child = new Child(firstName, lastName, null);

        // Act
        Child addedChild = db.addChild(child);

        // Assert
        assertNotNull(addedChild.id(), "Child ID should not be null");
        assertNull(addedChild.birthDate(), "Birth date should be null");
        assertEquals(firstName, addedChild.firstName());
    }

    @Test
    @DisplayName("Should update an existing child")
    void updateShouldUpdateExistingChild() throws SQLException {
        // Arrange - Add a child first
        Child child = new Child("Initial", "Name", LocalDate.of(2015, 1, 1));
        Child addedChild = db.addChild(child);
        
        // Create updated child
        Child updatedInfo = new Child(addedChild.id(), "Updated", "Name", LocalDate.of(2015, 2, 2));

        // Act
        boolean result = db.updateChild(updatedInfo);

        // Assert
        assertTrue(result, "Update should be successful");
        
        // Verify the update by querying the database (using list to find the child)
        List<Child> children = db.findChildrenWithMinimumAge(0);
        Child found = children.stream()
                .filter(c -> c.id().equals(addedChild.id()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(found);
        assertEquals("Updated", found.firstName());
        assertEquals(LocalDate.of(2015, 2, 2), found.birthDate());
    }


    @Test
    @DisplayName("Should delete an existing child")
    void deleteShouldDeleteExistingChild() throws SQLException {
        // Arrange - Add a child first
        Child child = new Child("To Be", "Deleted", LocalDate.of(2010, 5, 5));
        Child addedChild = db.addChild(child);

        // Act
        boolean result = db.deleteChild(addedChild.id());

        // Assert
        assertTrue(result, "Delete should be successful");
        
        // Verify the deletion
        List<Child> children = db.findChildrenWithMinimumAge(0);
        boolean exists = children.stream().anyMatch(c -> c.id().equals(addedChild.id()));
        assertFalse(exists, "Child should no longer exist in database");
    }


    @Test
    @DisplayName("Should return children with at least the specified age")
    void findChildrenWithMinimumAgeShouldReturnChildrenWithMinimumAge() throws SQLException {
        // Arrange - Add children with different ages
        LocalDate today = LocalDate.now();
        // Child 1 - 10 years old
        db.addChild(new Child("Ten", "YearOld", today.minusYears(10)));
        // Child 2 - 5 years old
        db.addChild(new Child("Five", "YearOld", today.minusYears(5)));
        // Child 3 - 15 years old
        db.addChild(new Child("Fifteen", "YearOld", today.minusYears(15)));

        // Act - Get all children at least 10 years old
        List<Child> children = db.findChildrenWithMinimumAge(10);

        // Assert
        // We expect at least 2 children (10 and 15 years old) + maybe some from init.sql
        // Let's check specifically for the ones we added
        boolean foundTen = children.stream().anyMatch(c -> "Ten".equals(c.firstName()));
        boolean foundFifteen = children.stream().anyMatch(c -> "Fifteen".equals(c.firstName()));
        boolean foundFive = children.stream().anyMatch(c -> "Five".equals(c.firstName()));

        assertTrue(foundTen, "Should find 10 year old");
        assertTrue(foundFifteen, "Should find 15 year old");
        assertFalse(foundFive, "Should not find 5 year old");
    }

    @Test
    @DisplayName("Should return children with null birth date")
    void findChildrenWithoutBirthDateShouldReturnChildrenWithNullBirthDate() throws SQLException {
        // Arrange - Add children with and without birth dates
        // Child with birth date
        db.addChild(new Child("With", "Birthday", LocalDate.of(2010, 1, 1)));

        // Child without birth date
        db.addChild(new Child("Without", "Birthday", null));

        // Act
        List<Child> children = db.findChildrenWithoutBirthDate();

        // Assert
        boolean foundWithout = children.stream().anyMatch(c -> "Without".equals(c.firstName()));
        boolean foundWith = children.stream().anyMatch(c -> "With".equals(c.firstName()));

        assertTrue(foundWithout, "Should find child without birth date");
        assertFalse(foundWith, "Should not find child with birth date");
    }

    @Test
    @DisplayName("Should throw exception when adding null child")
    void addChildShouldThrowExceptionForNullChild() {
        assertThrows(IllegalArgumentException.class, () -> db.addChild(null));
    }

    @Test
    @DisplayName("Should throw exception when adding child with empty first name")
    void addChildShouldThrowExceptionForEmptyFirstName() {
        Child child = new Child("", "Doe", LocalDate.of(2010, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> db.addChild(child));
    }

    @Test
    @DisplayName("Should throw exception when updating null child")
    void updateChildShouldThrowExceptionForNullChild() {
        assertThrows(IllegalArgumentException.class, () -> db.updateChild(null));
    }

    @Test
    @DisplayName("Should throw exception when deleting with null ID")
    void deleteChildShouldThrowExceptionForNullId() {
        assertThrows(IllegalArgumentException.class, () -> db.deleteChild(null));
    }

    @Test
    @DisplayName("Should throw exception for negative age")
    void findChildrenWithMinimumAgeShouldThrowExceptionForNegativeAge() {
        assertThrows(IllegalArgumentException.class, () -> db.findChildrenWithMinimumAge(-1));
    }
}

