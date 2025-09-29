package manning.performance.wordservice;

import manning.performance.premature.Account;

import java.util.List;

public interface SupportedAccountsLoader {
    List<Account> accounts();
}
