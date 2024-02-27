package mid;

import java.util.ArrayList;

public class Value {
    protected final ArrayList<User> useList = new ArrayList<>();

    public void addUser(User user) {
        useList.add(user);
    }

    public ArrayList<User> getUseList() {
        return useList;
    }
}
