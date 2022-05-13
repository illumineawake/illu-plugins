package net.runelite.client.plugins.itestplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskSet {
    public List<Task> taskList = new ArrayList<>();

    public TaskSet(Task... tasks) {
        taskList.addAll(Arrays.asList(tasks));
    }

    public void addAll(Task... tasks) {
        taskList.addAll(Arrays.asList(tasks));
    }

    public void clear() {
        taskList.clear();
    }

    /**
     * Iterates through all the tasks in the set and returns
     * the highest priority valid task.
     *
     * @return The first valid task from the task list or null if no valid task.
     */
    public Task getValidTask() {
        for (Task task : this.taskList) {
            if (task.validate()) {
                return task;
            }
        }
        return null;
    }
}
