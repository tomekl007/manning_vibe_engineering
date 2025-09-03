# Merge all dataframes into one with extra column `last_queries`
merged_df = pd.DataFrame()
for key, df in dataframes.items():
    if isinstance(df, pd.DataFrame):
        df_copy = df.copy()
        df_copy["last_queries"] = int(key)
        merged_df = pd.concat([merged_df, df_copy], ignore_index=True)

# Save to CSV
output_path = "/mnt/data/comparison_last_queries.csv"
merged_df.to_csv(output_path, index=False)

# Now let's prepare plots for comparison
import matplotlib.pyplot as plt

# Plot function: grouped by difficulty and quantiles
def plot_similarity(df, quantile_col, title):
    plt.figure(figsize=(10,6))
    for difficulty, group in df.groupby("difficulty"):
        avg_vals = group.groupby("last_queries")[quantile_col].mean()
        plt.plot(avg_vals.index, avg_vals.values, marker="o", label=difficulty)
    plt.title(title, fontsize=14)
    plt.xlabel("Number of last queries", fontsize=12)
    plt.ylabel(quantile_col, fontsize=12)
    plt.legend(title="Difficulty", fontsize=10)
    plt.grid(True)
    plt.tight_layout()
    return plt

plots = {}
for quantile in ["p50_overlap_similarity", "p90_overlap_similarity", "p99_overlap_similarity"]:
    plots[quantile] = plot_similarity(merged_df, quantile, f"Comparison of {quantile} vs last_queries")
