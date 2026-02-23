package org.example.config;

/**
 * XYZ Bank URL fragments. Base URL is in config.properties.
 * Views: #/login | #/manager/addCust | #/manager/openAccount | #/manager/list | #/customer | #/account | #/listTx
 */
public final class AppUrls {

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
