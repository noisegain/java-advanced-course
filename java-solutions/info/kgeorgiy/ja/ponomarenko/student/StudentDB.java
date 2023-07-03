package info.kgeorgiy.ja.ponomarenko.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {

    private static final Comparator<Student> NAME_ORDER = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).reversed()
            .thenComparingInt(Student::getId);

    private static <T, R> R map(List<Student> students, Function<Student, T> func, Collector<T, ?, R> collector) {
        return students.stream().map(func).collect(collector);
    }

    private static <T> List<T> mapToList(List<Student> students, Function<Student, T> func) {
        return map(students, func, Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapToList(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return map(students, Student::getFirstName, Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Comparator.naturalOrder()).map(Student::getFirstName).orElse("");
    }

    private static List<Student> sortedBy(Collection<Student> students, Comparator<Student> cmp) {
        return students.stream().sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedBy(students, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedBy(students, NAME_ORDER);
    }

    private static <T> List<Student> filterBy(Collection<Student> students, T field, Function<Student, T> extractor) {
        return students.stream()
                .filter(student -> extractor.apply(student).equals(field))
                .sorted(NAME_ORDER)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterBy(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterBy(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterBy(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(Comparator.naturalOrder())));
    }
}
