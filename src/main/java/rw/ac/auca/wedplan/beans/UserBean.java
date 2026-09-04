package rw.ac.auca.wedplan.beans;

import rw.ac.auca.wedplan.dao.UserDao;
import rw.ac.auca.wedplan.model.Role;
import rw.ac.auca.wedplan.model.Side;
import rw.ac.auca.wedplan.model.User;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSF Managed Bean for User management.
 */
@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UserDao userDao = new UserDao();

    private User user = new User();
    private List<User> users = new ArrayList<>();
    private String searchQuery = "";
    private String roleFilter = "ALL";
    private String sideFilter = "ALL";

    public String save() {
        try {
            if (userDao.isEmailRegistered(user.getEmail(), user.getId())) {
                addError("Email '" + user.getEmail() + "' is already registered in the system.");
                return null;
            }

            if (user.getId() == null) {
                userDao.save(user);
                addInfo("User '" + user.getName() + "' successfully registered.");
            } else {
                userDao.update(user);
                addInfo("User '" + user.getName() + "' successfully updated.");
            }
            this.user = new User();
            return "userList?faces-redirect=true";
        } catch (Exception ex) {
            addError("Failed to save user: " + ex.getMessage());
            return null;
        }
    }

    public String prepareCreate() {
        this.user = new User();
        return "userForm?faces-redirect=true";
    }

    public String prepareEdit(User selected) {
        this.user = selected;
        return "userForm?faces-redirect=true";
    }

    public String delete(User selected) {
        if (selected == null || selected.getId() == null) {
            addError("Invalid user selected for deletion.");
            return null;
        }

        try {
            if (userDao.hasAssignedTasks(selected.getId())) {
                addError("Cannot delete user '" + selected.getName() + "' because they have assigned tasks. Reassign or delete their tasks first.");
                return null;
            }

            userDao.delete(selected.getId());
            addInfo("User '" + selected.getName() + "' deleted successfully.");
        } catch (Exception ex) {
            addError("Error deleting user: " + ex.getMessage());
        }
        return "userList?faces-redirect=true";
    }

    public void resetFilters() {
        this.searchQuery = "";
        this.roleFilter = "ALL";
        this.sideFilter = "ALL";
    }

    public List<User> getUsers() {
        try {
            this.users = userDao.findAll();
        } catch (Exception e) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    public List<User> getFilteredUsers() {
        List<User> list = getUsers();
        return list.stream()
                .filter(u -> {
                    boolean matchesSearch = searchQuery == null || searchQuery.trim().isEmpty() ||
                            u.getName().toLowerCase().contains(searchQuery.toLowerCase().trim()) ||
                            u.getEmail().toLowerCase().contains(searchQuery.toLowerCase().trim());

                    boolean matchesRole = "ALL".equalsIgnoreCase(roleFilter) ||
                            u.getRole().name().equalsIgnoreCase(roleFilter);

                    boolean matchesSide = "ALL".equalsIgnoreCase(sideFilter) ||
                            u.getSide().name().equalsIgnoreCase(sideFilter);

                    return matchesSearch && matchesRole && matchesSide;
                })
                .collect(Collectors.toList());
    }

    public Role[] getAvailableRoles() {
        return Role.values();
    }

    public Side[] getAvailableSides() {
        return Side.values();
    }

    public int getTotalUsersCount() {
        return getUsers().size();
    }

    public long getBrideSideCount() {
        return getUsers().stream().filter(u -> u.getSide() == Side.BRIDE).count();
    }

    public long getGroomSideCount() {
        return getUsers().stream().filter(u -> u.getSide() == Side.GROOM).count();
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getRoleFilter() {
        return roleFilter;
    }

    public void setRoleFilter(String roleFilter) {
        this.roleFilter = roleFilter;
    }

    public String getSideFilter() {
        return sideFilter;
    }

    public void setSideFilter(String sideFilter) {
        this.sideFilter = sideFilter;
    }
}
