package org.githhub.ankurpathak.lld.splitwise;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class User {
    private static int idCounter = 0;
    int id;
    String name;
    UserExpenseBalanceSheet userExpenseBalanceSheet;
    Map<Integer, Group> groups;

    public User(String name) {
        this.id = ++idCounter;
        this.name = name;
        userExpenseBalanceSheet = new UserExpenseBalanceSheet();
        groups = new HashMap<>();
    }

    void addFriend(User friend){
        userExpenseBalanceSheet.friendsBalance.put(friend, new Balance());
        friend.userExpenseBalanceSheet.friendsBalance.put(this, new Balance());
    }

    void removeFriend(User friend){
        userExpenseBalanceSheet.friendsBalance.remove(friend);
        friend.userExpenseBalanceSheet.friendsBalance.remove(this);
    }

    void addGroup(Group group){
        groups.put(group.id, group);
    }

    void removeGroup(Group group){
        groups.remove(group.id);
    }



}

class Balance {
    BigDecimal owe = BigDecimal.ZERO;
    BigDecimal getBack = BigDecimal.ZERO;
}

class UserExpenseBalanceSheet{
    BigDecimal totalExpense = BigDecimal.ZERO;
    BigDecimal totalPayment =  BigDecimal.ZERO;
    BigDecimal totalOwe = BigDecimal.ZERO;
    BigDecimal totalGetBack = BigDecimal.ZERO;

    Map<User, Balance> friendsBalance =  new HashMap<>();

    @Override
    public String toString() {
        return "UserExpenseBalanceSheet{" +
                "totalExpense=" + totalExpense +
                ", totalPayment=" + totalPayment +
                ", totalOwe=" + totalOwe +
                ", totalGetBack=" + totalGetBack +
                ", friendsBalance=" + frinedsBalanceToString()
                +
                '}';


    }

    public String frinedsBalanceToString() {
        StringBuilder bdr = new StringBuilder();
        for(User friend: friendsBalance.keySet()){
            bdr.append(friend.name)
                    .append(" owe: ")
                    .append(friendsBalance.get(friend).owe)
                    .append(" getBack: ")
                    .append(friendsBalance.get(friend).getBack)
                    .append("   ");
        }

        return bdr.toString();
    }
}

class UserExpenseBalanceSheetManager {
    public void updateUserExpenseBalanceSheet(Expense expense){
        User paidBy = expense.paidBy;
        UserExpenseBalanceSheet userExpenseBalanceSheet = paidBy.userExpenseBalanceSheet;
        BigDecimal paid = expense.amount;
        userExpenseBalanceSheet.totalPayment = userExpenseBalanceSheet.totalPayment.add(paid);
        for(Split split: expense.splits){
            if(split.contributor == paidBy){
                userExpenseBalanceSheet.totalExpense = userExpenseBalanceSheet.totalOwe.add(split.contribution);
            }else{
                userExpenseBalanceSheet.totalGetBack = userExpenseBalanceSheet.totalGetBack.add(split.contribution);
                userExpenseBalanceSheet.friendsBalance.get(split.contributor).getBack = userExpenseBalanceSheet.friendsBalance.get(split.contributor).getBack.add(split.contribution);

                split.contributor.userExpenseBalanceSheet.totalOwe = split.contributor.userExpenseBalanceSheet.totalOwe.add(split.contribution);
                split.contributor.userExpenseBalanceSheet.friendsBalance.get(paidBy).owe = split.contributor.userExpenseBalanceSheet.friendsBalance.get(paidBy).owe.add(split.contribution);
                split.contributor.userExpenseBalanceSheet.totalExpense = split.contributor.userExpenseBalanceSheet.totalExpense.add(split.contribution);
            }
        }
    }


}

class UserManager {
    Map<Integer, User> users = new HashMap<>();

    void addUser(User user) {
        users.put(user.id, user);
    }

    void removeUser(User user) {
        users.remove(user.id);
    }
}

class Group {
    int id;
    String name;
    ExpenseManager expenseManager;
    Map<Integer, User> users;
    Map<Integer, Expense> expenses;
    private static int idCounter = 0;
    public Group(String name, ExpenseManager expenseManager){
        this.id = ++idCounter;
        this.name = name;
        this.expenseManager = expenseManager;
        users = new HashMap<>();
        expenses = new HashMap<>();
    }

    void addUser(User user) {
        users.put(user.id, user);
        user.addGroup(this);
    }

    void removeUser(User user) {
        users.remove(user.id);
        user.removeGroup(this);
    }

    void addExpense(Expense expense) {
        expenses.put(expense.id, expense);
    }

    void removeExpense(Expense expense) {
        expenses.remove(expense.id);
    }


    public Expense createExpense(String description, BigDecimal amount, User paidBy, SplitType splitType, List<Split> splits) {
        return expenseManager.createExpense(description, amount, paidBy, splitType, splits);
    }
}

class GroupManager {
    Map<Integer, Group> groups = new HashMap<>();

    void addGroup(Group group) {
        groups.put(group.id, group);
    }

    void removeGroup(Group group) {
        groups.remove(group.id);
    }

    List<Group> getUserGroups(User user) {
        return groups.values().stream().filter(group -> group.users.containsKey(user.id)).toList();
    }

}
@AllArgsConstructor
class Split {
    User contributor;
    BigDecimal contribution = BigDecimal.ZERO;
}

enum SplitType {
    EQUAL, PERCENTAGE, EXACT
}

class Expense {
    private static int idCounter = 0;
    int id;
    String description;
    BigDecimal amount = BigDecimal.ZERO;
    User paidBy;
    SplitType splitType = SplitType.EQUAL;
    List<Split> splits;

    public Expense(String description, BigDecimal amount, User paidBy, SplitType splitType, List<Split> splits) {
        this.id = ++idCounter;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.splitType = splitType;
        this.splits = splits;
    }
}

interface ExpenseSplitsValidator {
    boolean validateSplits(BigDecimal amount, List<Split> splits);
}

class EqualExpenseSplitsValidator implements ExpenseSplitsValidator {
    @Override
    public boolean validateSplits(BigDecimal amount, List<Split> splits) {
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        BigDecimal equalSplitAmount = amount.divide(new BigDecimal(splits.size()), ExpenseManager.mathContext);
        BigDecimal total = BigDecimal.ZERO;
        for (Split split : splits) {
            if (split.contribution.compareTo(equalSplitAmount) != 0) {
                return false;
            }
            total = total.add(split.contribution);
        }
        return total.compareTo(amount) == 0;
    }
}

class ExactExpenseSplitsValidator implements ExpenseSplitsValidator {

    @Override
    public boolean validateSplits(BigDecimal amount, List<Split> splits) {
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Split split : splits) {
            if(split.contribution.compareTo(BigDecimal.ZERO) < 0) {
                return false;
            }
            total = total.add(split.contribution);
        }
        return total.compareTo(amount) == 0;
    }
}

class ExpenseSplitsValidatorSimpleFactory {
    public static ExpenseSplitsValidator getExpenseSplitsValidator(SplitType splitType) {
        return switch (splitType) {
            case EQUAL -> new EqualExpenseSplitsValidator();
            case PERCENTAGE-> new PercentageExpenseSplitsValidator();
            case EXACT-> new ExactExpenseSplitsValidator();
        };
    }
}

class PercentageExpenseSplitsValidator implements ExpenseSplitsValidator {
    @Override
    public boolean validateSplits(BigDecimal amount, List<Split> splits) {
        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Split split : splits) {
            if(split.contribution.compareTo(BigDecimal.ZERO) < 0 || split.contribution.compareTo(BigDecimal.valueOf(100)) > 0) {
                return false;
            }
            total = total.add(split.contribution);
        }
        return total.compareTo(new BigDecimal(100)) == 0;
    }
}

class ExpenseManager {
    Map<Integer, Expense> expenses = new HashMap<>();
    public static  final MathContext mathContext = new MathContext(3, RoundingMode.HALF_UP);
    UserExpenseBalanceSheetManager userExpenseBalanceSheetManager = new UserExpenseBalanceSheetManager();

    void addExpense(Expense expense) {
        expenses.put(expense.id, expense);
    }
    void removeExpense(Expense expense) {
        expenses.remove(expense.id);
    }

    public Expense createExpense(String description, BigDecimal amount, User paidBy, SplitType splitType, List<Split> splits) {
        ExpenseSplitsValidator validator = ExpenseSplitsValidatorSimpleFactory.getExpenseSplitsValidator(splitType);
        if(!validator.validateSplits(amount, splits)){
            throw new IllegalArgumentException("Invalid expense request");
        }
        Expense expense = new Expense(description, amount, paidBy, splitType, splits);
        addExpense(expense);
        userExpenseBalanceSheetManager.updateUserExpenseBalanceSheet(expense);
        return expense;
    }
}

class SplitWise {
    UserManager userManager;
    ExpenseManager expenseManager;
    GroupManager groupManager;
    ObjectMapper mapper = new ObjectMapper();


    public SplitWise() {
        userManager = new UserManager();
        expenseManager = new ExpenseManager();
        groupManager = new GroupManager();
    }

    void initialize(){
        User ankur = new User("Ankur");
        User pradeep = new User("Pradeep");
        User prateek = new User("Prateek");

        userManager.addUser(ankur);
        userManager.addUser(pradeep);
        userManager.addUser(prateek);

        Group group = new Group("dsa", expenseManager);
        groupManager.addGroup(group);

        group.addUser(ankur);
        group.addUser(pradeep);
        group.addUser(prateek);

        ankur.addFriend(pradeep);
        ankur.addFriend(prateek);
        prateek.addFriend(pradeep);



        Expense expense = group.createExpense("dsa lld party", BigDecimal.valueOf(1000), ankur, SplitType.EXACT, List.of(
                new Split(ankur, BigDecimal.valueOf(400)),
                new Split(pradeep, BigDecimal.valueOf(300)),
                new Split(prateek, BigDecimal.valueOf(300))
        ));


        System.out.println(ankur.name + " " + ankur.userExpenseBalanceSheet);
        System.out.println(ankur.name + " " + prateek.userExpenseBalanceSheet);
        System.out.println(ankur.name + " " + pradeep.userExpenseBalanceSheet);

    }

    public static void main(String[] args) {
        SplitWise splitWise = new SplitWise();
        splitWise.initialize();

    }


}

