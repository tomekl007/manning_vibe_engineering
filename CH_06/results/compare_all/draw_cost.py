# Update cost mapping now that we have 20 queries as well
cost_mapping = {0: 4, 1: 5, 2: 5, 5: 6, 10: 7, 20: 10}

# Add cost column to merged dataframe
merged_df["cost_cents"] = merged_df["last_queries"].map(cost_mapping)

# Plot p50 vs cost again with 20 included
plt.figure(figsize=(8,6))
for difficulty, group in merged_df.groupby("difficulty"):
    avg_vals = group.groupby("cost_cents")["p50_overlap_similarity"].mean()
    plt.plot(avg_vals.index, avg_vals.values, marker="o", label=difficulty)
plt.title("p50 Overlap Similarity vs Cost of Running", fontsize=14)
plt.xlabel("Cost (cents)", fontsize=12)
plt.ylabel("p50_overlap_similarity", fontsize=12)
plt.legend(title="Difficulty", fontsize=10)
plt.grid(True)
plt.tight_layout()
plt.show()
