package rw.ac.auca.wedplan.beans;

import rw.ac.auca.wedplan.dao.TaskDao;
import rw.ac.auca.wedplan.dao.UserDao;
import rw.ac.auca.wedplan.model.Status;
import rw.ac.auca.wedplan.model.Task;
import rw.ac.auca.wedplan.model.User;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Named("taskBean")
@SessionScoped
public class TaskBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TaskDao taskDao = new TaskDao();
    private final UserDao userDao = new UserDao();

    private Task task = new Task();
    private Long selectedUserId;
    private List<Task> tasks = new ArrayList<>();
    private String searchQuery = "";
    private String statusFilter = "ALL";
    private String categoryFilter = "ALL";

    public String save() {
        try {
            if (selectedUserId == null) {
                addError("Please select an assigned user.");
                return null;
            }

            User user = userDao.findById(selectedUserId);
            if (user == null) {
                addError("Selected user does not exist.");
                return null;
            }

            task.setAssignedUser(user);

            if (task.getId() == null) {
                taskDao.save(task);
                addInfo("Task '" + task.getTitle() + "' successfully created.");
            } else {
                taskDao.update(task);
                addInfo("Task '" + task.getTitle() + "' successfully updated.");
            }
            resetForm();
            return "taskList?faces-redirect=true";
        } catch (Exception ex) {
            addError("Failed to save task: " + ex.getMessage());
            return null;
        }
    }

    public String prepareCreate() {
        resetForm();
        return "taskForm?faces-redirect=true";
    }

    public String prepareEdit(Task selected) {
        this.task = selected;
        if (selected != null && selected.getAssignedUser() != null) {
            this.selectedUserId = selected.getAssignedUser().getId();
        }
        return "taskForm?faces-redirect=true";
    }

    public String delete(Task selected) {
        if (selected == null || selected.getId() == null) {
            addError("Invalid task selected for deletion.");
            return null;
        }

        try {
            taskDao.delete(selected.getId());
            addInfo("Task '" + selected.getTitle() + "' deleted successfully.");
        } catch (Exception ex) {
            addError("Error deleting task: " + ex.getMessage());
        }
        return "taskList?faces-redirect=true";
    }

    public void resetFilters() {
        this.searchQuery = "";
        this.statusFilter = "ALL";
        this.categoryFilter = "ALL";
    }

    private void resetForm() {
        this.task = new Task();
        this.task.setDeadline(new Date());
        this.selectedUserId = null;
    }

    public List<Task> getTasks() {
        try {
            this.tasks = taskDao.findAll();
        } catch (Exception e) {
            this.tasks = new ArrayList<>();
        }
        return this.tasks;
    }

    public List<Task> getFilteredTasks() {
        List<Task> list = getTasks();
        return list.stream()
                .filter(t -> {
                    boolean matchesSearch = searchQuery == null || searchQuery.trim().isEmpty() ||
                            t.getTitle().toLowerCase().contains(searchQuery.toLowerCase().trim()) ||
                            t.getCategory().toLowerCase().contains(searchQuery.toLowerCase().trim()) ||
                            (t.getAssignedUser() != null && t.getAssignedUser().getName().toLowerCase().contains(searchQuery.toLowerCase().trim()));

                    boolean matchesStatus = "ALL".equalsIgnoreCase(statusFilter) ||
                            t.getStatus().name().equalsIgnoreCase(statusFilter);

                    boolean matchesCategory = "ALL".equalsIgnoreCase(categoryFilter) ||
                            t.getCategory().equalsIgnoreCase(categoryFilter);

                    return matchesSearch && matchesStatus && matchesCategory;
                })
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        try {
            return userDao.findAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Status[] getAvailableStatuses() {
        return Status.values();
    }

    public List<String> getAvailableCategories() {
        return getTasks().stream()
                .map(Task::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    public int getTotalTasksCount() {
        return getTasks().size();
    }

    public long getPendingTasksCount() {
        return getTasks().stream().filter(t -> t.getStatus() == Status.PENDING).count();
    }

    public long getInProgressTasksCount() {
        return getTasks().stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
    }

    public long getCompletedTasksCount() {
        return getTasks().stream().filter(t -> t.getStatus() == Status.COMPLETED).count();
    }

    public boolean isOverdue(Task t) {
        if (t == null || t.getDeadline() == null || t.getStatus() == Status.COMPLETED) {
            return false;
        }
        return t.getDeadline().before(new Date());
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(Long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }
}
