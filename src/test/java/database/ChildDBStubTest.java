package database;

import dao.ChildDAO;
import model.Child;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.ChildService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stub demo for Child flow")
class ChildDBStubTest {

    private ChildDAO stubDao;
    private ChildService ChildService;
    private static int inMemoryChildrenCount = 3;

    @BeforeEach
    void setUp() {
        stubDao = new InMemoryChildDAOStub();
        ChildService = new ChildService(stubDao);
    }

    @Test
    @DisplayName("Stub: search returns deterministic in-memory data")
    void searchShouldReturnExpectedItemsFromStub() {
        List<Child> result = ChildService.searchChildrenByFirstName("name");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.firstName().toLowerCase().contains("name")));
    }

    @Test
    @DisplayName("Stub: rename by name updates item in the in-memory store")
    void renameByNameShouldUpdateChildInStub() {
        boolean updated = ChildService.renameChild(1L, "One", "Lname", LocalDate.of(2010, 1, 1));
        List<Child> result = ChildService.searchChildrenByFirstName("One");

        assertTrue(updated);
        assertEquals(1, result.size());
        assertEquals("One", result.get(0).firstName());
    }

    @Test
    @DisplayName("Stub: delete returns false for unknown id")
    void deleteShouldReturnFalseForUnknownId() throws SQLException {
        boolean deleted = stubDao.deleteChild(999L);

        assertFalse(deleted);
    }

    @Test
    @DisplayName("Stub: add child assigns new id automatically")
    void addShouldAssignNewId() throws SQLException {
        Child newChild = new Child("New", "Child", LocalDate.of(2015, 5, 5));
        Child added = stubDao.addChild(newChild);

        assertNotNull(added.id());
        assertTrue(added.id() > inMemoryChildrenCount);
        assertEquals("New", added.firstName());
    }

    @Test
    @DisplayName("Stub: findChildrenWithoutBirthDate returns only null-date children")
    void findWithoutBirthDateShouldReturnOnlyNullDates() throws SQLException {
        stubDao.addChild(new Child("NoDate", "Child", null));

        List<Child> result = stubDao.findChildrenWithoutBirthDate();

        assertTrue(result.stream().allMatch(c -> c.birthDate() == null));
        assertTrue(result.stream().anyMatch(c -> "NoDate".equals(c.firstName())));
    }

    @Test
    @DisplayName("Stub: findChildrenWithMinimumAge excludes younger children")
    void findWithMinimumAgeShouldExcludeYoungerChildren() throws SQLException {
        stubDao.addChild(new Child("Young", "Child", LocalDate.now().minusYears(3)));

        List<Child> result = stubDao.findChildrenWithMinimumAge(10);

        assertFalse(result.stream().anyMatch(c -> "Young".equals(c.firstName())));
    }

    @Test
    @DisplayName("Stub: findChildrenByLastNamePart searches by last name not first")
    void findByLastNameShouldSearchByLastName() throws SQLException {
        List<Child> result = stubDao.findChildrenByLastNamePart("Lname");

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(c -> c.lastName().toLowerCase().contains("lname")));
    }

    @Test
    @DisplayName("Stub: update returns false for non-existing id")
    void updateShouldReturnFalseForUnknownId() throws SQLException {
        Child ghost = new Child(999L, "Ghost", "Child", LocalDate.of(2010, 1, 1));
        boolean updated = stubDao.updateChild(ghost);

        assertFalse(updated);
    }

    @Test
    @DisplayName("Stub: delete removes child so it cannot be found afterwards")
    void deleteShouldRemoveChildFromStore() throws SQLException {
        boolean deleted = stubDao.deleteChild(1L);
        List<Child> result = stubDao.findChildrenByFirstNamePart("Fname");

        assertTrue(deleted);
        assertTrue(result.isEmpty());
    }

    static class InMemoryChildDAOStub implements ChildDAO {
        private final Map<Long, Child> children = new HashMap<>();

        InMemoryChildDAOStub() {
            children.put(1L, new Child(1L, "Fname", "Lname", LocalDate.of(2010, 1, 1))); //#1
            children.put(2L, new Child(2L, "First-name", "Last-name", LocalDate.of(2010, 1, 1))); //#2
            children.put(3L, new Child(3L, "Fk", "Lk", LocalDate.of(2010, 1, 1))); //#3
            inMemoryChildrenCount = 3;
        }

        @Override
        public Child addChild(Child child) {
            long nextId = children.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
            Child created = new Child(nextId, child.firstName(), child.lastName(), child.birthDate());
            children.put(nextId, created);
            return created;
        }

        @Override
        public boolean updateChild(Child Child) {
            if (!children.containsKey(Child.id())) {
                return false;
            }
            children.put(Child.id(), Child);
            return true;
        }

        @Override
        public boolean deleteChild(Long id) {
            return children.remove(id) != null;
        }

        @Override
        public List<Child> findChildrenByFirstNamePart(String firstName) {
            String lowered = firstName.toLowerCase();
            return children.values().stream()
                    .filter(c -> c.firstName().toLowerCase().contains(lowered))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Child> findChildrenByLastNamePart(String lastName) {
            String lowered = lastName.toLowerCase();
            return children.values().stream()
                    .filter(c -> c.lastName().toLowerCase().contains(lowered))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Child> findChildrenByBirthdate(LocalDate birthDate) {
            return children.values().stream()
                    .filter(c -> c.birthDate().equals(birthDate))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Child> findChildrenWithoutBirthDate() {
            return children.values().stream()
                    .filter(c -> c.birthDate() == null)
                    .collect(Collectors.toList());

        }

        @Override
        public List<Child> findChildrenWithMinimumAge(int age) {
            return children.values().stream()
                    .filter(c -> (LocalDate.now().getYear() - c.birthDate().getYear()) >= age)
                    .collect(Collectors.toList());
        }
    }
}