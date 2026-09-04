package rw.ac.auca.wedplan.model;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Task entity representing wedding planning tasks.
 * Has a @ManyToOne relationship with User (FK: assigned_user_id).
 * Enforces Validation Type #1: JSR-380 Bean Validation Annotations.
 */
@Entity
@Table(name = "tasks")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "event_id", nullable = false)
    private int eventId = 1;

    // Validation Type #1: JSR-380 Bean Validation Annotation
    @NotBlank(message = "Task title is required.")
    @Size(min = 2, max = 100, message = "Task title must be between 2 and 100 characters.")
    @Column(name = "title", nullable = false)
    private String title;

    // Validation Type #1: JSR-380 Bean Validation Annotation
    @NotNull(message = "Task deadline is required.")
    @FutureOrPresent(message = "Task deadline must be today or a future date.")
    @Temporal(TemporalType.DATE)
    @Column(name = "deadline", nullable = false)
    private Date deadline;

    // Validation Type #1: JSR-380 Bean Validation Annotation
    @NotNull(message = "Task status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "category")
    private String category = "General";

    // Real Foreign Key Relationship: Task ManyToOne User
    // Validation Type #1: JSR-380 Bean Validation Annotation
    @NotNull(message = "An assigned user must be selected for the task.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    public Task() {
    }

    public Task(int eventId, String title, Date deadline, Status status, String category, User assignedUser) {
        this.eventId = eventId;
        this.title = title;
        this.deadline = deadline;
        this.status = status;
        this.category = category;
        this.assignedUser = assignedUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
