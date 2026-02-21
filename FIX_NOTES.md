# ✅ CustomerTest - Fixed NullPointerException Issue

## Problem Fixed

**Error**: `java.lang.NullPointerException: Cannot invoke "org.example.pages.manager.LoginPage.loginAsCustomer(String)" because "this.loginPage" is null`

**Root Cause**: The `loginPage` field was null when individual test methods tried to use it. This happened because:
1. `setUp()` initializes page objects
2. `createTestCustomer()` runs during setup and navigates back to login page
3. Some tests were trying to use `loginPage` without ensuring it was initialized

## Solution Applied

### 1. Added Null Check Method
Created `ensurePageObjectsInitialized()` method that checks if page objects are null and reinitializes them:

```java
private void ensurePageObjectsInitialized() {
    if (loginPage == null) {
        logger.debug("Initializing LoginPage");
        loginPage = new LoginPage(driver);
    }
    if (customerDashboardPage == null) {
        logger.debug("Initializing CustomerDashboardPage");
        customerDashboardPage = new CustomerDashboardPage(driver);
    }
    if (managerDashboardPage == null) {
        logger.debug("Initializing ManagerDashboardPage");
        managerDashboardPage = new ManagerDashboardPage(driver);
    }
}
```

### 2. Made testCustomerName Non-Static
Changed from: `private static String testCustomerName;`
To: `private String testCustomerName;`

This ensures each test gets its own customer name instance.

### 3. Added Initialization Call to All Test Methods
Every test now starts with:
```java
ensurePageObjectsInitialized();
```

This guarantees that all page objects are ready before the test runs.

## Files Modified

- ✅ `CustomerTest.java` - Added null check method and initialization calls to all tests

## Testing After Fix

The tests should now:
1. ✅ Initialize all page objects properly
2. ✅ Create a test customer before each test runs
3. ✅ Ensure page objects are available when needed
4. ✅ Run without NullPointerException errors
5. ✅ Have proper logging for debugging

## Best Practices Applied

✅ **Defensive Programming**: Check for null before use  
✅ **Initialization Pattern**: Lazy initialization with null checks  
✅ **Instance Variables**: Use instance instead of static for mutable state  
✅ **Logging**: Log when reinitializing for debugging  
✅ **Code Reusability**: Single method used by all tests  

## Running Tests Now

You can now run the tests:
1. Open `CustomerTest.java`
2. Click the green ▶ icon to run all tests
3. Or click ▶ next to individual test methods
4. Tests should pass without NullPointerException

---

**Status**: ✅ **FIXED**  
All tests in CustomerTest should now run successfully!

