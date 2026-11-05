// Headless Chrome can be slow to start when several modules run their browser tests at once, or
// on a busy CI runner, so Karma waits up to 2 minutes for it rather than 30 seconds.
config.set({
  captureTimeout: 120000,
  browserNoActivityTimeout: 120000,
  browserDisconnectTimeout: 120000,
});
