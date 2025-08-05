# Money Talks: FinOps for LLMs - Cost‑Cutting, Confidentiality, and the Right Hardware

Tokens aren’t free and neither is trust, so we will discuss Unit Economy. Whether you’re streaming prompts through a cloud API at a few cents per‑K or running a quantized beast on your own GPU stack, every design call is a budget line. This chapter breaks down price calculus (token vs. bandwidth vs. latency), shows when local inference beats API providers on both wallet and privacy (and why it is so hard to achieve their level of service), and maps hardware tiers - all these gamer cards to NVidia clusters - to the model traits they actually unlock (context window, throughput, precision, first-token latency). 


### Check the Vibe
If you can’t answer “What did we spend on tokens yesterday, by service, by model?” you’re not doing FinOps - you’re flying blind. Also “Flat” pricing isn’t flat - hit a spend threshold and the provider silently swaps to a weaker model.

### Street Rule
No metrics, no mercy: log tokens, retries, and tier‑switches or brace for surprise invoices. Model cost can stockpile.

### Move to Make
Set up a simple observability stack - track token usage, cost per request, and inference latency. Add alerts when costs spike or performance dips, then compare API vs. local runs with real numbers, not hunches.


----

https://simonwillison.net/2025/Aug/4/llm-openrouter-usage/