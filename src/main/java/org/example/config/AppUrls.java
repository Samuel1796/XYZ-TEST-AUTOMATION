package org.example.config;

/**
 * URL hash fragments for the XYZ Bank AngularJS app. The base URL (e.g. https://.../BankingProject/)
 * is configured in the test setup (BaseTest.Config); these constants are appended for navigation (e.g. base + LOGIN).
 * <p>
 * Flow: LOGIN → MANAGER_HOME / CUSTOMER_LOGIN → MANAGER_ADD_CUSTOMER, MANAGER_OPEN_ACCOUNT, MANAGER_CUSTOMERS_LIST,
 * or CUSTOMER_ACCOUNT, CUSTOMER_TRANSACTIONS.
 * </p>
 */
public final class AppUrls {

    /** Prevent instantiation; all members are static constants. */
    private AppUrls() {}

    /** Home / Login page – Customer Login, Bank Manager Login, Home */
    public static final String LOGIN = "#/login";

    /** Manager area root (after Bank Manager Login) */
    public static final String MANAGER_HOME = "#/manager";

    /** Add Customer form – First Name, Last Name, Postal Code, Add Customer submit */
    public static final String MANAGER_ADD_CUSTOMER = "#/manager/addCust";

    /** Open Account – Customer dropdown, Currency dropdown, Process */
    public static final String MANAGER_OPEN_ACCOUNT = "#/manager/openAccount";

    /** Customers list – table, Delete buttons */
    public static final String MANAGER_CUSTOMERS_LIST = "#/manager/list";

    /** Customer login – dropdown to select customer, Login button */
    public static final String CUSTOMER_LOGIN = "#/customer";

    /** Customer account – Balance, Deposit, Withdraw, Transactions (form changes on Deposit/Withdraw click) */
    public static final String CUSTOMER_ACCOUNT = "#/account";

    /** Customer transactions – transaction history table */
    public static final String CUSTOMER_TRANSACTIONS = "#/listTx";
}
