# OmniRouter AI — Change Log

This file tracks all significant modifications, model integrations, API key updates, routing logic enhancements, and core architecture changes in OmniRouter AI.

---

## [v1.0.0] - 2026-08-21 (Initial Release & Core Architecture)

### 1. AI Model Integrations Added
- **OpenRouter Unified Hub**:
  - Integrated OpenRouter API (`https://openrouter.ai/api/v1/chat/completions`) for multi-model access.
  - Added support for Claude 3.7 Sonnet, Claude 3.5 Sonnet, GPT-4o, GPT-5 Preview, DeepSeek R1, DeepSeek V3, Llama 3.3 70B, and Qwen 2.5.
- **OpenAI Integration**:
  - Added direct OpenAI API support (`https://api.openai.com/v1/chat/completions`) for GPT-4o, GPT-4o-mini, o1, and o3-mini.
- **Anthropic Integration**:
  - Added Anthropic Messages API (`https://api.anthropic.com/v1/messages`) for Claude 3.7 Sonnet, Claude 3.5 Haiku, and Claude 3 Opus.
- **Google AI / Gemini Integration**:
  - Added Gemini REST API (`https://generativelanguage.googleapis.com/v1beta/models/...`) for Gemini 2.5 Flash, Gemini 2.5 Pro, and Gemini 1.5 Flash.
- **Local AI & Custom Endpoints**:
  - Integrated local Ollama endpoint (`http://10.0.2.2:11434/v1/chat/completions`), LM Studio, and generic OpenAI-compatible custom endpoints with cleartext HTTP support.

### 2. Intelligent Model Routing System
- **Real-time Task Intent Classifier**:
  - Rule-based & heuristic analysis engine for incoming prompts.
  - Detects 6 distinct task classes:
    1. `CODING` (Syntax, bug-fixing, algorithms, SQL, refactoring) -> Routes to High-Quality models (Claude 3.7 / GPT-5 / DeepSeek Coder).
    2. `REASONING` (Step-by-step logic, math proofs, deep problem-solving) -> Routes to o1 / DeepSeek R1.
    3. `RESEARCH` (Literature synthesis, long-context analysis) -> Routes to Gemini 2.5 Pro / Claude 3.5 Sonnet.
    4. `FAST_QUERY` (Quick definitions, trivia, translations, lightweight lookups) -> Routes to Gemini 2.5 Flash / GPT-4o-mini.
    5. `CASUAL_CHAT` (Conversational banter, light advice) -> Routes to Llama 3.3 / Gemini Flash.
    6. `CREATIVE_WRITING` (Storytelling, copy, poetry) -> Routes to Claude / GPT-4o.
- **Quality vs Cost Optimization Modes**:
  - Supported policies: `AUTO` (Intelligent workload balancing), `HIGH_QUALITY` (Always best model), `BALANCED`, `COST_SAVER` (Prioritize cheap/free models), `LOCAL_ONLY` (Privacy-first offline local models).
- **Transparency & Override**:
  - Transparent routing reason logged with every request.
  - Manual model override available on chat bar before sending prompt.

### 3. Model Management Dashboard
- **Provider Settings**:
  - Encrypted local key storage with test connection ping for OpenRouter, OpenAI, Anthropic, Google AI, and Local Endpoints.
- **Model Catalog**:
  - 25+ curated models with capabilities, context limits, and token pricing rates ($/1M tokens).
- **Default Routing Matrix**:
  - Configurable primary and fallback models per task category.
- **Analytics & Cost Tracker**:
  - Real-time token usage, latency tracking, and cumulative cost calculation.

### 4. Automatic Change Log System
- **In-App Change Logger**:
  - Automatically records all additions/removals of models, API key changes, routing policy updates, and system events.
  - Interactive Markdown viewer and exporter for `change_log.md`.
