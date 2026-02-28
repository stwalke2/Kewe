# Kewe Dev Configuration

## AI Agent supplier search

Set environment variables before starting backend:

- `KEWE_PLAYWRIGHT_ENABLED` (`true`/`false`, default `true`)
- `KEWE_SEARCH_PROVIDER` (`none` | `serpapi` | `bing`, default `none`)
- `KEWE_SERPAPI_KEY` (required when provider is `serpapi`)
- `KEWE_BING_KEY` (required when provider is `bing`)
- `KEWE_BING_ENDPOINT` (optional, default `https://api.bing.microsoft.com/v7.0/search`)

Example:

```bash
export KEWE_PLAYWRIGHT_ENABLED=true
export KEWE_SEARCH_PROVIDER=serpapi
export KEWE_SERPAPI_KEY=your_key_here
./gradlew bootRun
```
