# 💊 Drug Data Science: The Vibe Engineering Approach

Welcome to the **Drug Data Science** project. This repository contains a comprehensive end-to-end journey through a dataset of 250,000+ medicines, using the "Vibe Engineering" philosophy.

Instead of just running algorithms, we decode the "vibe" of the pharmaceutical market—from identifying market giants to building a "Digital Guardian Angel" that detects dangerous drug interactions.

---

## 🚀 Project Roadmap

The analysis is divided into 7 logical parts, progressing in technical complexity:

1.  **Checking the Market Vibe:** Statistical analysis of price and manufacturer dominance. 
    Show importance of data parsing and analysis - and that AI results needs to be carefully checked because it impacts the rest of the more sophisticated models.
2.  **The Alchemy of Dosage:** Feature engineering to extract active ingredients ("Salts") and dosage values from messy text.
3.  **The Price Crystal Ball:** Machine Learning (Random Forest) to predict drug prices and identify cost drivers.
4.  **Digital Guardian Angel:** Deep Learning (NLP/GRU) to classify high-risk drug interactions.
5.  **Semantic Vibe Search:** Transformers (Sentence-BERT) for meaning-based search across medical descriptions.
6.  **Glitch in the Matrix:** Unsupervised Learning (Isolation Forest) to detect overpriced anomalies.
7.  **The Scare Scale:** Sentiment Analysis to correlate side-effect severity with market value.

---

## 🛠 Setup & Configuration

This project requires a specific environment to handle both traditional Machine Learning and modern Deep Learning. We use **Conda** for environment management.

### 1. Prerequisites
- [Miniconda](https://docs.conda.io/en/latest/miniconda.html) or [Anaconda](https://www.anaconda.com/) installed.
- The dataset file: `updated_indian_medicine_data.csv`.

### 2. Environment Creation
Open your terminal and run the following commands:

```bash
# Create the environment
conda create -n drug_analysis python=3.10 -y

# Activate the environment
conda activate drug_analysis

# Install core Data Science stack
conda install pandas numpy matplotlib seaborn tabulate scikit-learn -y

# Install Deep Learning and Transformer libraries
pip install tensorflow sentence-transformers
pip install tf-keras

# Setup Jupyter Kernel
conda install ipykernel -y
conda install -c conda-forge notebook -y
```


### 3. Launching the Analysis
When you environment is set up, launch Jupyter Notebook:

1. Run `conda activate drug_analysis`
2. Run `jupyter notebook` 
3. Open drug analysis.ipynb.


### Important: Hardware Stability Fix
Deep Learning libraries (TensorFlow) can sometimes clash with specific hardware (like M1/M2 chips or XLA optimizers). 
The notebook includes a Stability Fix at the top. Ensure this cell runs first to prevent NotFoundError glitches:
```python
import os
os.environ["TF_XLA_FLAGS"] = "--tf_xla_enable_xla_devices=false"
import tensorflow as tf
tf.config.optimizer.set_jit(False)
```


### Key Insight: The "Salt" Philosophy
In this project, we prioritize the Salt Name (Active Ingredient) over the Brand Name.

- Brand Name: The marketing vibe (e.g., Augmentin). 
- Salt Name: The chemical reality (e.g., Amoxycillin).

Our models show that the Salt is the primary driver of both price and risk, proving that AI is most effective when it looks past the label and into the core data.