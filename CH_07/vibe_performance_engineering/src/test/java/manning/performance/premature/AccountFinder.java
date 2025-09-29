package manning.performance.premature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class AccountFinder {
    private List<Account> accounts;
    private ExecutorService executor;
    private Map<Integer, Account> accountMap;

    public AccountFinder(List<Account> accounts) {
        this.accounts = accounts;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // Initialize HashMap for O(1) lookup performance
        this.accountMap = new HashMap<>(accounts.size() * 4 / 3 + 1); // Optimal initial capacity
        for (Account account : accounts) {
            accountMap.put(account.getId(), account);
        }
    }

    // Original single-threaded version
    public Optional<Account> account(Integer id) {
        return accounts.stream().filter(v -> v.getId().equals(id)).findAny();
    }

    // LLM generated code below

    // Parallel stream version - uses multiple threads automatically
    public Optional<Account> accountParallel(Integer id) {
        return accounts.parallelStream().filter(v -> v.getId().equals(id)).findAny();
        // note proper usage of findAny()!
    }

    // Concurrent version using CompletableFuture for more control
    public Optional<Account> accountConcurrent(Integer id) {
        int chunkSize = Math.max(1, accounts.size() / Runtime.getRuntime().availableProcessors());

        List<CompletableFuture<Optional<Account>>> futures =
                accounts.stream()
                        .collect(Collectors.groupingBy(account -> accounts.indexOf(account) / chunkSize))
                        .values()
                        .stream()
                        .map(chunk -> CompletableFuture.supplyAsync(() ->
                                chunk.stream().filter(v -> v.getId().equals(id)).findAny(), executor))
                        .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    // Optimized parallel version with custom thread pool
    public Optional<Account> accountOptimized(Integer id) {
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            return customThreadPool.submit(() ->
                    accounts.parallelStream().filter(v -> v.getId().equals(id)).findAny()).get();
        } catch (Exception e) {
            throw new RuntimeException("Error in parallel processing", e);
        } finally {
            customThreadPool.shutdown();
        }
    }

    // Batch processing for multiple IDs concurrently
    public List<Optional<Account>> findMultipleAccounts(List<Integer> ids) {
        return ids.parallelStream()
                .map(this::accountParallel)
                .collect(Collectors.toList());
    }

    // HashMap-based O(1) lookup - optimal for production
    public Optional<Account> accountHashMap(Integer id) {
        return Optional.ofNullable(accountMap.get(id));
    }

    // HashMap-based batch lookup - more efficient than multiple individual lookups
    public List<Optional<Account>> findMultipleAccountsHashMap(List<Integer> ids) {
        return ids.stream()
                .map(this::accountHashMap)
                .collect(Collectors.toList());
    }

    // Check if account exists without retrieving it (faster than accountHashMap)
    public boolean accountExists(Integer id) {
        return accountMap.containsKey(id);
    }

    // Get account count
    public int getAccountCount() {
        return accountMap.size();
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}